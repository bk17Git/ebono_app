package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GeminiService
import com.example.data.UserSettings
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatPersona(
    val id: String,
    val name: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val model: String,
    val description: String,
    val systemInstruction: String,
    val enableSearchGrounding: Boolean = false,
    val isPremium: Boolean = false
)

val Personas = listOf(
    ChatPersona(
        id = "free_companion",
        name = "Free Companion",
        icon = Icons.Default.Chat,
        model = "gemini-3.1-flash-lite-preview",
        description = "Standard book buddy and helpful brainstorming partner.",
        systemInstruction = "You are a friendly book lover assistant. Discuss book plots, characters, and help brainstorm general book suggestions in a warm, welcoming tone.",
        isPremium = false
    ),
    ChatPersona(
        id = "scholar",
        name = "Literary Scholar",
        icon = Icons.Default.School,
        model = "gemini-3.1-pro-preview",
        description = "Deep literary comparisons and critical style analysis.",
        systemInstruction = "You are an elite literary scholar. Analyze books, styles, prose, and authors with deep academic vocabulary and rich intellectual comparisons. Keep it highly educational and inspiring.",
        isPremium = true
    ),
    ChatPersona(
        id = "summarizer",
        name = "Speed Summarizer",
        icon = Icons.Default.Bolt,
        model = "gemini-3.1-flash-lite-preview",
        description = "Summarizes plots, themes, and arcs at lightning-fast speeds.",
        systemInstruction = "You are a speed-reading summaries assistant. Summarize book plots, key character arcs, major themes, and takeaway lessons at lightning speed. Use concise bold lists and very direct bullet points.",
        isPremium = true
    ),
    ChatPersona(
        id = "grounded",
        name = "Grounded Researcher",
        icon = Icons.Default.Language,
        model = "gemini-3.5-flash",
        description = "Uses Google Search to retrieve real-time bibliographies and facts.",
        systemInstruction = "You are a research bibliographer with real-time access to Google Search. Answer questions about books, authors, publisher backgrounds, current news, awards, and release dates with precise grounded facts.",
        enableSearchGrounding = true,
        isPremium = true
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: BookViewModel,
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "Ebono AI Assistant",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Conversational Literary Intelligence",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ActiveChatInterface(viewModel = viewModel)
        }
    }
}

@Composable
fun ActiveChatInterface(
    viewModel: BookViewModel
) {
    val settingsState by viewModel.userSettings.collectAsState()
    val settings = settingsState ?: UserSettings()

    var selectedPersona by remember { mutableStateOf(Personas[0]) }
    var chatInputText by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }

    var showUpgradeDialog by remember { mutableStateOf(false) }
    var attemptedPersona by remember { mutableStateOf<ChatPersona?>(null) }

    // Chat threads mapped by persona ID
    val conversations = remember { mutableStateMapOf<String, List<ChatMessage>>() }

    val currentMessages = conversations[selectedPersona.id] ?: listOf(
        ChatMessage(
            role = "model",
            text = "Hello! I am your ${selectedPersona.name} assistant. " +
                    "I am powered by Google's ${selectedPersona.model} engine. " +
                    if (selectedPersona.enableSearchGrounding) "Google Search grounding is ACTIVE to fetch up-to-date facts!" 
                    else "Ask me any complex questions or requests you have!"
        )
    )

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // Upgrade Dialog if unsubscribed tries to use premium
    if (showUpgradeDialog && attemptedPersona != null) {
        AlertDialog(
            onDismissRequest = { showUpgradeDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text("Unlock ${attemptedPersona?.name}")
                }
            },
            text = {
                Text(
                    "The ${attemptedPersona?.name} bot uses advanced intelligence (${attemptedPersona?.model}) and features like " +
                    (if (attemptedPersona?.enableSearchGrounding == true) "live Google Search grounding." else "deep academic literary analysis.") +
                    "\n\nUpgrade to Ebono Premium to unlock this and more exclusive features!"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.subscribeUser()
                        showUpgradeDialog = false
                        selectedPersona = attemptedPersona!!
                    }
                ) {
                    Text("Upgrade to Premium")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUpgradeDialog = false }) {
                    Text("Maybe Later")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dynamic banner showing whether they are on Free or Premium tier
        if (!settings.isSubscribed) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Upgrade Info",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ebono Free Tier Active",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Upgrade to unlock advanced Literary Scholar AI and live Google Search grounding!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                    Button(
                        onClick = { viewModel.subscribeUser() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("Upgrade", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Persona Selector chips
        Text(
            text = "SELECT AI BOT INSTANCE",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Personas.forEach { persona ->
                val isSelected = selectedPersona.id == persona.id
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (!settings.isSubscribed && persona.isPremium) {
                                attemptedPersona = persona
                                showUpgradeDialog = true
                            } else {
                                selectedPersona = persona
                            }
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                         else MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = persona.icon,
                                contentDescription = persona.name,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(22.dp)
                            )
                            if (persona.isPremium && !settings.isSubscribed) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Locked",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.TopEnd)
                                )
                            }
                        }
                        Text(
                            text = persona.name.split(" ").last(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Selected Bot HUD Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                    contentColor = MaterialTheme.colorScheme.secondary
                ) {
                    Text(
                        text = selectedPersona.model,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (selectedPersona.enableSearchGrounding) {
                    Badge(
                        containerColor = Color(0xFF1976D2).copy(alpha = 0.15f),
                        contentColor = Color(0xFF1976D2)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(10.dp))
                            Text(
                                text = "GOOGLE SEARCH ACTIVE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text(
                    text = selectedPersona.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
            }
        }

        // Scrollable Chat Thread Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentMessages, key = { it.id }) { message ->
                    val isModel = message.role == "model"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isModel) Alignment.Start else Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(bottom = 2.dp)
                        ) {
                            if (isModel) {
                                Icon(
                                    imageVector = selectedPersona.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = selectedPersona.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "You",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.timestamp)),
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }

                        // Message Bubble
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isModel) MaterialTheme.colorScheme.surface
                                                 else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isModel) 2.dp else 16.dp,
                                bottomEnd = if (isModel) 16.dp else 2.dp
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isModel) MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                                        else Color.Transparent
                            ),
                            modifier = Modifier
                                .widthIn(max = 280.dp)
                                .testTag("chat_bubble_${if (isModel) "bot" else "user"}")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = message.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isModel) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onPrimary,
                                    lineHeight = 20.sp
                                )

                                if (isModel && message.text != "No response text") {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(
                                            onClick = {
                                                clipboardManager.setText(AnnotatedString(message.text))
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy message",
                                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isSending) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Synthesizing AI output...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // Chat Input Section
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = chatInputText,
                onValueChange = { chatInputText = it },
                placeholder = {
                    Text(
                        text = "Query bot instance...",
                        fontSize = 14.sp,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_chat_input_field"),
                textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                maxLines = 3,
                singleLine = false
            )

            FloatingActionButton(
                onClick = {
                    if (chatInputText.isNotBlank() && !isSending) {
                        val prompt = chatInputText
                        chatInputText = ""
                        isSending = true

                        // Build history
                        val nextHistory = currentMessages + ChatMessage(role = "user", text = prompt)
                        conversations[selectedPersona.id] = nextHistory

                        scope.launch {
                            // Scroll to bottom
                            listState.animateScrollToItem(nextHistory.size - 1)

                            // Prepare payload for Gemini API
                            // Convert history list to Pair(role, text) format
                            val messagesPayload = nextHistory.map {
                                Pair(it.role, it.text)
                            }

                            val response = GeminiService.callGeminiChat(
                                modelName = selectedPersona.model,
                                messages = messagesPayload,
                                systemInstruction = selectedPersona.systemInstruction,
                                enableSearchGrounding = selectedPersona.enableSearchGrounding
                            )

                            val updatedHistory = nextHistory + ChatMessage(role = "model", text = response)
                            conversations[selectedPersona.id] = updatedHistory
                            isSending = false

                            // Scroll to bottom
                            listState.animateScrollToItem(updatedHistory.size - 1)
                        }
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .testTag("send_chat_message_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
