package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReaderScreen(
    viewModel: BookViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToFocusMode: (String) -> Unit
) {
    val book by viewModel.selectedBook.collectAsState()
    val fontSize by viewModel.readerFontSize.collectAsState()
    val pageColor by viewModel.readerPageColor.collectAsState()

    if (book == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Active E-Book",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Select an e-book from your library directory to read.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Go to Library")
                }
            }
        }
        return
    }

    val activeBook = book!!

    // Eye comfort configuration mappings
    val (bgColor, textColor, fontType) = when (pageColor) {
        "Paper" -> Triple(Color(0xFFFCFAF2), Color(0xFF1C1A17), FontFamily.Serif)
        "Sepia" -> Triple(Color(0xFFF4ECD8), Color(0xFF5C4033), FontFamily.Serif)
        "Dark" -> Triple(Color(0xFF26292B), Color(0xFFE2E2E2), FontFamily.Default)
        "Midnight" -> Triple(Color(0xFF0F1112), Color(0xFF909A9C), FontFamily.Monospace)
        else -> Triple(Color(0xFFFCFAF2), Color(0xFF1C1A17), FontFamily.Serif)
    }

    var currentPage by remember(activeBook.id) { mutableStateOf(activeBook.currentPage.coerceAtLeast(1)) }

    // Simulate pages based on description
    val chapterText = remember(activeBook.id, currentPage) {
        generateChapterText(activeBook, currentPage)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            activeBook.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            "By ${activeBook.author}",
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    // Start Focus mode shortcut
                    IconButton(
                        onClick = { onNavigateToFocusMode(activeBook.id) },
                        modifier = Modifier.testTag("reader_focus_shortcut")
                    ) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = "Distraction-free Focus", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        bottomBar = {
            // Reader Navigation Bar (Next/Prev, size, themes)
            Column(
                modifier = Modifier
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                // Page indicator progress bar
                val progressFraction = currentPage.toFloat() / activeBook.totalPages.coerceAtLeast(1)
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp),
                    color = if (pageColor == "Paper" || pageColor == "Sepia") Color(0xFF7D4E2F) else Color(0xFFEED5B7),
                    trackColor = textColor.copy(alpha = 0.12f)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Font Size Adjuster
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { viewModel.adjustReaderFontSize(-1.5f) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("A-", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                        Text(
                            "${fontSize.toInt()}sp",
                            fontSize = 12.sp,
                            color = textColor,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = { viewModel.adjustReaderFontSize(1.5f) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Text("A+", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = textColor)
                        }
                    }

                    // Eye Comfort Theme selection
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemePill(name = "Paper", active = pageColor == "Paper", activeColor = Color(0xFF7D4E2F), textColor = textColor) {
                            viewModel.setReaderPageColor("Paper")
                        }
                        ThemePill(name = "Sepia", active = pageColor == "Sepia", activeColor = Color(0xFF7D4E2F), textColor = textColor) {
                            viewModel.setReaderPageColor("Sepia")
                        }
                        ThemePill(name = "Midnight", active = pageColor == "Midnight", activeColor = Color(0xFFEED5B7), textColor = textColor) {
                            viewModel.setReaderPageColor("Midnight")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Flipping Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            if (currentPage > 1) {
                                currentPage -= 1
                                viewModel.updateReadingProgress(activeBook.id, currentPage)
                            }
                        },
                        enabled = currentPage > 1,
                        colors = ButtonDefaults.textButtonColors(contentColor = textColor)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Prev Page")
                    }

                    Text(
                        text = "Page $currentPage of ${activeBook.totalPages}",
                        fontSize = 12.sp,
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )

                    TextButton(
                        onClick = {
                            if (currentPage < activeBook.totalPages) {
                                currentPage += 1
                                viewModel.updateReadingProgress(activeBook.id, currentPage)
                            }
                        },
                        enabled = currentPage < activeBook.totalPages,
                        colors = ButtonDefaults.textButtonColors(contentColor = textColor)
                    ) {
                        Text("Next Page")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }
            }
        },
        containerColor = bgColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgColor)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Elegant chapter heading
            Text(
                text = "Chapter ${(currentPage + 3) / 4}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = textColor,
                fontFamily = fontType,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            )

            // Book content body
            Text(
                text = chapterText,
                fontSize = fontSize.sp,
                color = textColor,
                fontFamily = fontType,
                lineHeight = (fontSize * 1.5f).sp,
                textAlign = TextAlign.Justify,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reader_content_body")
            )

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun ThemePill(
    name: String,
    active: Boolean,
    activeColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(
                color = if (active) activeColor.copy(alpha = 0.15f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) activeColor else textColor.copy(alpha = 0.6f)
        )
    }
}

// Generate realistic simulated book content based on description
private fun generateChapterText(book: Book, page: Int): String {
    val seedText = book.description
    val sentences = seedText.split(". ")
    val sb = java.lang.StringBuilder()

    sb.append("Indeed, as we find ourselves on page $page, the saga continues. ")
    
    if (sentences.isNotEmpty()) {
        val index1 = (page * 3) % sentences.size
        val index2 = (page * 7 + 1) % sentences.size
        val index3 = (page * 13 + 2) % sentences.size
        
        sb.append(sentences[index1].trim().removeSuffix(".")).append(". ")
        sb.append("This quiet observation reminds us that there is a deep, unwritten history here. ")
        sb.append(sentences[index2].trim().removeSuffix(".")).append(". ")
        sb.append("In the quiet spaces of our intellect, we hear the soft, rustling pages of memory. ")
        sb.append(sentences[index3].trim().removeSuffix(".")).append(". ")
    }

    sb.append("\n\n")
    sb.append("The light from the window begins to dim, and the room takes on the amber, silent shadows of the early evening. Every book is a door, every page a path. We turn the page not out of haste, but out of a deep and beautiful curiosity, seeking what lies beyond our current horizons.")
    
    return sb.toString()
}
