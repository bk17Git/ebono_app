package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.BookDatabase
import com.example.data.BookRepository
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {

    // Setup Room Database & Repository
    private val database by lazy { BookDatabase.getDatabase(applicationContext, lifecycleScope) }
    private val repository by lazy { BookRepository(database.bookDao()) }

    // Setup ViewModel
    private val viewModel: BookViewModel by viewModels {
        BookViewModelFactory(application, repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsState by viewModel.userSettings.collectAsState()
            val isDarkMode = settingsState?.isDarkMode ?: false
            MyApplicationTheme(darkTheme = isDarkMode) {
                MainAppScreen(viewModel)
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: BookViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Tabs that should display the bottom navigation bar
    val mainTabs = listOf("home", "library", "forums", "store", "account")
    val showBottomBar = currentRoute in mainTabs

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.22f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Home
                            val isHome = currentRoute == "home"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (!isHome) {
                                            navController.navigate("home") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (isHome) Icons.Filled.Home else Icons.Outlined.Home,
                                        contentDescription = "Home",
                                        tint = if (isHome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Home",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isHome) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isHome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // 2. Library
                            val isLibrary = currentRoute == "library"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (!isLibrary) {
                                            navController.navigate("library") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (isLibrary) Icons.Filled.AutoStories else Icons.Outlined.AutoStories,
                                        contentDescription = "Library Catalog",
                                        tint = if (isLibrary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Library",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isLibrary) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isLibrary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // 3. Forums
                            val isForums = currentRoute == "forums"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (!isForums) {
                                            navController.navigate("forums") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (isForums) Icons.Filled.Forum else Icons.Outlined.Forum,
                                        contentDescription = "Community Forums",
                                        tint = if (isForums) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Forums",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isForums) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isForums) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // 4. Bookstore (Shop)
                            val isStore = currentRoute == "store"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (!isStore) {
                                            navController.navigate("store") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (isStore) Icons.Filled.LocalMall else Icons.Outlined.LocalMall,
                                        contentDescription = "Bookstore & Cart",
                                        tint = if (isStore) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Store",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isStore) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isStore) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // 5. Account Center
                            val isAccount = currentRoute == "account"
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        if (!isAccount) {
                                            navController.navigate("account") {
                                                popUpTo("home") { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                    .padding(vertical = 6.dp, horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = if (isAccount) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                                        contentDescription = "Account Center",
                                        tint = if (isAccount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Account",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isAccount) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isAccount) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            // tab 1: Home Dashboard
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToReader = { navController.navigate("reader") },
                    onNavigateToAudioPlayer = { navController.navigate("audioplayer") },
                    onNavigateToLibrary = { navController.navigate("library") },
                    onNavigateToFocus = {
                        navController.navigate("focus") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAccount = {
                        navController.navigate("account") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToAiChat = {
                        navController.navigate("aichat")
                    }
                )
            }

            // tab 2: Search Library
            composable("library") {
                LibraryScreen(
                    viewModel = viewModel,
                    onNavigateToReader = { navController.navigate("reader") },
                    onNavigateToAudioPlayer = { navController.navigate("audioplayer") }
                )
            }

            // tab 3: Forums Community
            composable("forums") {
                ForumScreen(viewModel = viewModel)
            }

            // tab 4: Paperback Store
            composable("store") {
                StoreScreen(viewModel = viewModel)
            }

            // tab 5: Focus timer space
            composable("focus") {
                FocusScreen(viewModel = viewModel)
            }

            // tab 5.5: Account Center
            composable("account") {
                AccountScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.navigate("home") {
                            popUpTo("home") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            // nested 6: Distraction-free E-Reader
            composable("reader") {
                ReaderScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToFocusMode = { bookId ->
                        navController.navigate("focus") {
                            popUpTo("home") { saveState = true }
                        }
                    }
                )
            }

            // nested 7: Full-screen Audiobook Player
            composable("audioplayer") {
                AudioPlayerScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // nested 8: Ebono AI Assistant
            composable("aichat") {
                AiChatScreen(
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
