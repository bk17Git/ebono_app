package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ForumPost
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForumScreen(viewModel: BookViewModel) {
    val posts by viewModel.forumPosts.collectAsState()
    val selectedPost by viewModel.selectedPost.collectAsState()
    val comments by viewModel.selectedPostComments.collectAsState()

    var activeTagFilter by remember { mutableStateOf("All") }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    // Create post states
    var createTitle by remember { mutableStateOf("") }
    var createContent by remember { mutableStateOf("") }
    var createTag by remember { mutableStateOf("Discussion") }

    // Comment states
    var commentInputText by remember { mutableStateOf("") }

    // Social share simulation state
    var shareNotificationText by remember { mutableStateOf<String?>(null) }

    val tags = listOf("All", "Discussion", "Recommendations", "Author Spotlight", "Milestone")

    val filteredPosts = if (activeTagFilter == "All") posts else posts.filter { it.tag == activeTagFilter }

    if (selectedPost != null) {
        // Detailed Post and Comments view
        val post = selectedPost!!
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(post.tag, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    navigationIcon = {
                        IconButton(onClick = { viewModel.selectPost(null) }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back to forums")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                // Comment Reply Input bar
                Surface(
                    tonalElevation = 4.dp,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = commentInputText,
                            onValueChange = { commentInputText = it },
                            placeholder = { Text("Write a supportive reply...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("comment_reply_input"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.background,
                                unfocusedContainerColor = MaterialTheme.colorScheme.background,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true
                        )

                        IconButton(
                            onClick = {
                                if (commentInputText.isNotBlank()) {
                                    viewModel.addForumComment(post.id, commentInputText)
                                    commentInputText = ""
                                }
                            },
                            modifier = Modifier.testTag("submit_comment_button")
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send Reply", tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // The Main Post card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AvatarPlaceholder(post.authorAvatarRes)
                                Column {
                                    Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(post.authorRole, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                post.content,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { viewModel.likeForumPost(post.id) }) {
                                        Icon(
                                            imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Like",
                                            tint = if (post.isLikedByMe) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                    Text("${post.likesCount} Likes", fontSize = 12.sp)
                                }

                                IconButton(onClick = {
                                    shareNotificationText = "Cozy discussion shared! Link copied to clipboard."
                                }) {
                                    Icon(Icons.Default.Share, contentDescription = "Share discussion", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Replies (${comments.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // List of Comments
                if (comments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No replies yet. Be the first to start the discussion!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    items(comments) { comment ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                AvatarPlaceholder(comment.authorAvatarRes)
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(
                                            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(comment.timestamp)),
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(comment.content, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Forums overview list
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreatePostDialog = true },
                    modifier = Modifier.padding(bottom = 64.dp).testTag("add_post_fab"),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.background
                ) {
                    Icon(Icons.Default.AddComment, contentDescription = "Create Forum Topic")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Heading
                Text(
                    text = "Ebono Forums",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Discuss favorite authors, share notes, and review stories with readers worldwide.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tag filters
                ScrollableTabRow(
                    selectedTabIndex = tags.indexOf(activeTagFilter).coerceAtLeast(0),
                    edgePadding = 0.dp,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = {}
                ) {
                    tags.forEach { tag ->
                        Tab(
                            selected = activeTagFilter == tag,
                            onClick = { activeTagFilter = tag },
                            text = {
                                Text(
                                    text = tag,
                                    fontSize = 13.sp,
                                    fontWeight = if (activeTagFilter == tag) FontWeight.Bold else FontWeight.Normal,
                                    color = if (activeTagFilter == tag) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // List of posts
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 90.dp, top = 4.dp)
                ) {
                    items(filteredPosts) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.selectPost(post) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AvatarPlaceholder(post.authorAvatarRes)
                                        Column {
                                            Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Text(post.authorRole, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(post.tag, fontSize = 9.sp, fontWeight = FontWeight.Bold) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(post.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    post.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = null,
                                                tint = if (post.isLikedByMe) Color.Red else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${post.likesCount}", fontSize = 11.sp)
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Comment, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("${post.commentsCount}", fontSize = 11.sp)
                                        }
                                    }

                                    // Quick share simulation
                                    IconButton(
                                        onClick = {
                                            shareNotificationText = "Forum link copied! Share with friends."
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Share confirmation dialog
    shareNotificationText?.let { msg ->
        AlertDialog(
            onDismissRequest = { shareNotificationText = null },
            confirmButton = {
                TextButton(onClick = { shareNotificationText = null }) {
                    Text("OK", color = MaterialTheme.colorScheme.primary)
                }
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Social Share")
                }
            },
            text = { Text(msg) },
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Create Post Dialog Compose
    if (showCreatePostDialog) {
        AlertDialog(
            onDismissRequest = { showCreatePostDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (createTitle.isNotBlank() && createContent.isNotBlank()) {
                            viewModel.addForumPost(createTitle, createContent, createTag)
                            createTitle = ""
                            createContent = ""
                            showCreatePostDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_post_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Publish Post")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePostDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            },
            title = { Text("Compose Discussion Topic") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TextField(
                        value = createTitle,
                        onValueChange = { createTitle = it },
                        label = { Text("Discussion Title *") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    TextField(
                        value = createContent,
                        onValueChange = { createContent = it },
                        label = { Text("Write your thoughts... *") },
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Tag choice
                    Text("Choose Topic Tag:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        listOf("Discussion", "Recommendations", "Milestone").forEach { tag ->
                            val active = createTag == tag
                            FilterChip(
                                selected = active,
                                onClick = { createTag = tag },
                                label = { Text(tag, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
fun AvatarPlaceholder(avatarRes: String) {
    val (color, label) = when (avatarRes) {
        "avatar_1" -> Pair(Color(0xFF8D6E63), "SJ")
        "avatar_2" -> Pair(Color(0xFF5C6BC0), "MW")
        "avatar_3" -> Pair(Color(0xFF26A69A), "LV")
        "avatar_me" -> Pair(Color(0xFF42A5F5), "ME")
        "avatar_lumina" -> Pair(Color(0xFFD4AF37), "LM")
        else -> Pair(Color(0xFF78909C), "RD")
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(label, fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
