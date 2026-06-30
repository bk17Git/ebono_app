package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Book
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayerScreen(
    viewModel: BookViewModel,
    onNavigateBack: () -> Unit
) {
    val activeAudiobook by viewModel.activeAudiobook.collectAsState()
    val isPlaying by viewModel.audioIsPlaying.collectAsState()
    val playSpeed by viewModel.audioPlaySpeed.collectAsState()
    val sleepMinutes by viewModel.audioSleepMinutesRemaining.collectAsState()

    var showSleepTimerMenu by remember { mutableStateOf(false) }

    if (activeAudiobook == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Audiobook Playing",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Select an audiobook from your library to start listening.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onNavigateBack,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Go to Library")
                }
            }
        }
        return
    }

    val book = activeAudiobook!!

    // Formatting helpers
    fun formatTime(seconds: Int): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format("%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format("%02d:%02d", mins, secs)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize player", tint = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = "NOW PLAYING",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
            IconButton(onClick = { showSleepTimerMenu = true }) {
                Icon(
                    imageVector = if (sleepMinutes != null) Icons.Default.AlarmOn else Icons.Default.Alarm,
                    contentDescription = "Sleep Timer",
                    tint = if (sleepMinutes != null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        // Audiobook Artwork Cover (Aesthetic & premium layout)
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.secondary,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                // Animated soundwave when active!
                AnimatedSoundwave(isPlaying = isPlaying)
            }
        }

        // Info details
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Narrated by ${book.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }

        // Sleep timer indicator if active
        if (sleepMinutes != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Sleep Timer: $sleepMinutes mins left",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cancel",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { viewModel.cancelSleepTimer() }
                    )
                }
            }
        }

        // Slider and Progress Timer Timestamps
        Column(modifier = Modifier.fillMaxWidth()) {
            val progressFraction = book.progressSeconds.toFloat() / book.durationSeconds.coerceAtLeast(1)
            Slider(
                value = progressFraction,
                onValueChange = { /* Simulated scrubber */ },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.secondary,
                    activeTrackColor = MaterialTheme.colorScheme.secondary,
                    inactiveTrackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(book.progressSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                )
                Text(
                    text = "-" + formatTime(book.durationSeconds - book.progressSeconds),
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp
                )
            }
        }

        // Audio controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Speed controller
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val nextSpeed = when (playSpeed) {
                            1.0f -> 1.25f
                            1.25f -> 1.5f
                            1.5f -> 2.0f
                            else -> 1.0f
                        }
                        viewModel.setAudioPlaySpeed(nextSpeed)
                    }
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "${playSpeed}x",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Skip Backward
            IconButton(
                onClick = { viewModel.seekAudio(-15) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.FastRewind, contentDescription = "Rewind 15 seconds", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.secondary)
            }

            // Play/Pause FAB
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.secondary, CircleShape)
                    .clickable { viewModel.toggleAudioPlay() }
                    .testTag("audio_play_pause_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.background,
                    modifier = Modifier.size(36.dp)
                )
            }

            // Skip Forward
            IconButton(
                onClick = { viewModel.seekAudio(15) },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.FastForward, contentDescription = "Forward 15 seconds", modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.secondary)
            }

            // Audio Bookmarks bookmark
            IconButton(onClick = {
                viewModel.addForumPost(
                    title = "My favorite quote from ${book.title}",
                    content = "Just bookmarked an incredible quote at ${formatTime(book.progressSeconds)} in the audiobook version of '${book.title}'. Highly recommend giving this a listen!",
                    tag = "Discussion",
                    bookId = book.id
                )
            }) {
                Icon(Icons.Default.BookmarkBorder, contentDescription = "Save Audio Forum Bookmark", tint = MaterialTheme.colorScheme.secondary)
            }
        }
    }

    // Sleep Timer Dropdown Dialog
    if (showSleepTimerMenu) {
        AlertDialog(
            onDismissRequest = { showSleepTimerMenu = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showSleepTimerMenu = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            },
            title = { Text("Sleep Timer") },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 15, 30, 45, 60).forEach { mins ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.startSleepTimer(mins)
                                    showSleepTimerMenu = false
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "$mins Minutes",
                                modifier = Modifier.padding(16.dp),
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyMedium
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
fun AnimatedSoundwave(isPlaying: Boolean) {
    val barCount = 5
    val animators = List(barCount) { i ->
        rememberInfiniteTransition(label = "audio_bars").animateFloat(
            initialValue = 0.1f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 300 + (i * 100),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "audio_bar_height_$i"
        )
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.height(40.dp)
    ) {
        for (i in 0 until barCount) {
            val hFraction = if (isPlaying) animators[i].value else 0.15f
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(hFraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
            )
        }
    }
}
