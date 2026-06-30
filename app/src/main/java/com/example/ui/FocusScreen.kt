package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Book

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusScreen(
    viewModel: BookViewModel,
    bookIdParam: String? = null
) {
    val downloadedBooks by viewModel.downloadedBooks.collectAsState()
    val secondsRemaining by viewModel.focusSecondsRemaining.collectAsState()
    val isTimerRunning by viewModel.focusTimerRunning.collectAsState()
    val activeFocusSound by viewModel.focusSound.collectAsState()

    var selectedBookId by remember { mutableStateOf(bookIdParam ?: downloadedBooks.firstOrNull()?.id ?: "") }
    var selectedMinutes by remember { mutableStateOf(15) } // Default 15 minutes focus session

    val selectedBook = downloadedBooks.find { it.id == selectedBookId }

    // Sound items
    val focusSounds = listOf("None", "Rain", "Forest", "White Noise")

    // Formatting duration to MM:SS
    fun formatMMSS(seconds: Int): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Heading Section
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Focus Sanctum",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enter a peaceful, distraction-free space to absorb literature.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }

        if (secondsRemaining > 0) {
            // Active countdown clock (Aesthetic, large negative space, monospaced typography)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // Large circular clock representation
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .border(4.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape)
                        .border(
                            width = 2.dp,
                            color = if (isTimerRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatMMSS(secondsRemaining),
                            fontSize = 48.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.testTag("focus_countdown_timer")
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isTimerRunning) "STAY FOCUSSED" else "PAUSED",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // High-tech sound oscillation visualizer wave
                FocusWaveVisualizer(
                    isPlaying = isTimerRunning && activeFocusSound != "None",
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(30.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Ambient waveform and label
                if (activeFocusSound != "None") {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Sound Stream: $activeFocusSound active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Active Controls
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quit button
                Button(
                    onClick = { viewModel.stopFocusSession() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.testTag("stop_focus_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop Session", tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Reset", color = MaterialTheme.colorScheme.primary)
                    }
                }

                // Pause / Play toggle FAB
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .clickable { viewModel.toggleFocusPause() }
                        .testTag("pause_focus_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = "Pause / Resume focus session",
                        tint = MaterialTheme.colorScheme.background,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

        } else {
            // Configuration Setup screen
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Book picker
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "1. Select Book to Dedicate Session To",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (downloadedBooks.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            modifier = Modifier.fillMaxWidth().clickable { /* Optional shortcut */ }
                        ) {
                            Text(
                                "No books saved offline. Session will be logged under general reading.",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    } else {
                        // Horizontal list of downloadable books
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(downloadedBooks) { b ->
                                val active = b.id == selectedBookId
                                Card(
                                    modifier = Modifier
                                        .width(130.dp)
                                        .clickable { selectedBookId = b.id },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (active) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    border = if (active) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = b.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = b.author,
                                            fontSize = 9.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // 2. Duration pickers
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "2. Select Session Duration",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 10, 15, 20, 25, 30).forEach { mins ->
                            val active = selectedMinutes == mins
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable { selectedMinutes = mins }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${mins}m",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // 3. Ambient noise selector
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "3. Select Ambient Environment Sound",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        focusSounds.forEach { sound ->
                            val active = activeFocusSound == sound
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable { viewModel.setFocusSound(sound) }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sound,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Start button
                Button(
                    onClick = {
                        val bookIdToLog = if (selectedBookId.isNotEmpty()) selectedBookId else null
                        viewModel.startFocusSession(selectedMinutes, bookIdToLog)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .testTag("start_timer_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.HourglassEmpty, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Begin Distraction-Free Reading Session")
                    }
                }
            }
        }
    }
}

@Composable
fun FocusWaveVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "wave_anim")
    val waveOffset1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset1"
    )
    val waveOffset2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset2"
    )

    val color = MaterialTheme.colorScheme.primary

    androidx.compose.foundation.Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val path1 = androidx.compose.ui.graphics.Path()
        val path2 = androidx.compose.ui.graphics.Path()

        val midY = height / 2f
        val amplitude = if (isPlaying) 15.dp.toPx() else 2.dp.toPx()

        path1.moveTo(0f, midY)
        path2.moveTo(0f, midY)

        for (x in 0..width.toInt() step 4) {
            val relativeX = x.toFloat() / width
            val y1 = midY + amplitude * kotlin.math.sin(relativeX * 3f * Math.PI.toFloat() + waveOffset1)
            val y2 = midY + amplitude * kotlin.math.cos(relativeX * 2.5f * Math.PI.toFloat() - waveOffset2)

            path1.lineTo(relativeX * width, y1)
            path2.lineTo(relativeX * width, y2)
        }

        drawPath(
            path = path1,
            color = color.copy(alpha = 0.4f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
        )
        drawPath(
            path = path2,
            color = color.copy(alpha = 0.2f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
        )
    }
}
