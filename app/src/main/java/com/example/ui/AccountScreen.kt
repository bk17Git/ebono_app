package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.UserSettings

@Composable
fun AccountScreen(
    viewModel: BookViewModel,
    onNavigateBack: () -> Unit = {}
) {
    val books by viewModel.allBooks.collectAsState(initial = emptyList())
    val settingsState by viewModel.userSettings.collectAsState()
    val settings = settingsState ?: UserSettings()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            AccountHubContent(
                viewModel = viewModel,
                settings = settings,
                books = books,
                onDismiss = onNavigateBack
            )
        }
    }
}
