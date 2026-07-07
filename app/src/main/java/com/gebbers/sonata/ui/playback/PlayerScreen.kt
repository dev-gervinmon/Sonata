package com.gebbers.sonata.ui.playback

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gebbers.sonata.domain.model.Song

@Composable
fun PlayerScreen(
    song: Song?,
    isPlaying: Boolean,
    shuffleModeEnabled: Boolean,
    repeatMode: Int,
    currentPosition: Long,
    duration: Long,
    sleepTimerMillisLeft: Long?,
    playbackSpeed: Float,
    playbackPitch: Float,
    onTogglePlayPause: () -> Unit,
    onToggleShuffle: () -> Unit,
    onToggleRepeatMode: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekBack: () -> Unit,
    onSeek: (Long) -> Unit,
    onSetSleepTimer: (Int) -> Unit,
    onCancelSleepTimer: () -> Unit,
    onSetPlaybackSpeed: (Float) -> Unit,
    onSetPlaybackPitch: (Float) -> Unit,
    onOpenEqualizer: () -> Unit,
    onClose: () -> Unit
) {
    if (song == null) return

    var showSleepTimerDialog by remember { mutableStateOf(false) }
    var showPlaybackSettingsDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Close")
            }
            Row {
                IconButton(onClick = { showSleepTimerDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Sleep Timer",
                        tint = if (sleepTimerMillisLeft != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { showPlaybackSettingsDialog = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Playback Settings")
                }
                IconButton(onClick = onOpenEqualizer) {
                    Icon(Icons.Default.Equalizer, contentDescription = "Equalizer")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Album Art Placeholder
        Surface(
            modifier = Modifier
                .size(300.dp)
                .aspectRatio(1f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.MusicNote,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                // When we have real art URIs:
                // AsyncImage(model = artUri, contentDescription = null)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = song.title,
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Progress Bar
        Slider(
            value = currentPosition.toFloat(),
            onValueChange = { onSeek(it.toLong()) },
            valueRange = 0f..duration.toFloat().coerceAtLeast(1f),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatDuration(currentPosition), style = MaterialTheme.typography.bodySmall)
            Text(text = formatDuration(duration), style = MaterialTheme.typography.bodySmall)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onToggleShuffle) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (shuffleModeEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onSkipPrevious, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = onSeekBack, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Replay10, contentDescription = "Rewind 10s", modifier = Modifier.size(32.dp))
            }
            FilledIconButton(
                onClick = onTogglePlayPause,
                modifier = Modifier.size(72.dp),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = Modifier.size(40.dp)
                )
            }
            IconButton(onClick = onSeekForward, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.Forward10, contentDescription = "Forward 10s", modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = onSkipNext, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(32.dp))
            }
            IconButton(onClick = onToggleRepeatMode) {
                Icon(
                    imageVector = when (repeatMode) {
                        androidx.media3.common.Player.REPEAT_MODE_ONE -> Icons.Default.RepeatOne
                        androidx.media3.common.Player.REPEAT_MODE_ALL -> Icons.Default.Repeat
                        else -> Icons.Default.Repeat
                    },
                    contentDescription = "Repeat",
                    tint = if (repeatMode != androidx.media3.common.Player.REPEAT_MODE_OFF) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
    if (showSleepTimerDialog) {
        SleepTimerDialog(
            currentMillisLeft = sleepTimerMillisLeft,
            onDismiss = { showSleepTimerDialog = false },
            onSetTimer = {
                onSetSleepTimer(it)
                showSleepTimerDialog = false
            },
            onCancelTimer = {
                onCancelSleepTimer()
                showSleepTimerDialog = false
            }
        )
    }

    if (showPlaybackSettingsDialog) {
        PlaybackSettingsDialog(
            speed = playbackSpeed,
            pitch = playbackPitch,
            onDismiss = { showPlaybackSettingsDialog = false },
            onSetSpeed = onSetPlaybackSpeed,
            onSetPitch = onSetPlaybackPitch
        )
    }
}

@Composable
fun PlaybackSettingsDialog(
    speed: Float,
    pitch: Float,
    onDismiss: () -> Unit,
    onSetSpeed: (Float) -> Unit,
    onSetPitch: (Float) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Playback Settings") },
        text = {
            Column {
                Text("Speed: ${"%.2f".format(speed)}x", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = speed,
                    onValueChange = onSetSpeed,
                    valueRange = 0.5f..2.0f,
                    steps = 15
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Pitch: ${"%.2f".format(pitch)}x", style = MaterialTheme.typography.bodyMedium)
                Slider(
                    value = pitch,
                    onValueChange = onSetPitch,
                    valueRange = 0.5f..2.0f,
                    steps = 15
                )
                
                TextButton(
                    onClick = {
                        onSetSpeed(1.0f)
                        onSetPitch(1.0f)
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Reset")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
fun SleepTimerDialog(
    currentMillisLeft: Long?,
    onDismiss: () -> Unit,
    onSetTimer: (Int) -> Unit,
    onCancelTimer: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sleep Timer") },
        text = {
            Column {
                if (currentMillisLeft != null) {
                    Text(
                        text = "Time remaining: ${formatDuration(currentMillisLeft)}",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                Text("Turn off playback in:")
                Spacer(modifier = Modifier.height(8.dp))
                val options = listOf(5, 15, 30, 60)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    options.forEach { minutes ->
                        OutlinedButton(onClick = { onSetTimer(minutes) }) {
                            Text("$minutes m")
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (currentMillisLeft != null) {
                TextButton(onClick = onCancelTimer) {
                    Text("Cancel Timer")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
