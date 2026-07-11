package com.gebbers.sonata.ui.library

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.gebbers.sonata.domain.model.Song

@Composable
fun SongList(
    modifier: Modifier = Modifier,
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onMoreClick: (Song) -> Unit = {},
    showTrackNumbers: Boolean = false,
    canReorder: Boolean = false,
) {
    if (canReorder) {
        ReorderableSongList(
            songs = songs,
            onSongClick = onSongClick,
            onMoreClick = onMoreClick,
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(songs, key = { it.mediaStoreId }) { song ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    SongItem(
                        song = song,
                        onClick = { onSongClick(song) },
                        onMoreClick = { onMoreClick(song) },
                        showTrackNumber = showTrackNumbers
                    )
                }
            }
        }
    }
}

@Composable
fun ReorderableSongList(
    songs: List<Song>,
    onSongClick: (Song) -> Unit,
    onMoreClick: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    
    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        itemsIndexed(songs, key = { _, song -> song.mediaStoreId }) { _, song ->
            SongItem(
                song = song,
                onClick = { onSongClick(song) },
                onMoreClick = { onMoreClick(song) },
                showTrackNumber = false,
                trailingContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { onMoreClick(song) }) {
                            Icon(Icons.Default.MoreVert, contentDescription = null)
                        }
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Reorder",
                            modifier = Modifier
                                .padding(8.dp)
                                .pointerInput(Unit) {
                                    detectDragGesturesAfterLongPress(
                                        onDrag = { change, _ ->
                                            change.consume()
                                        }
                                    )
                                }
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun SongItem(
    modifier: Modifier = Modifier,
    song: Song,
    onClick: () -> Unit,
    onMoreClick: () -> Unit,
    showTrackNumber: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        headlineContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showTrackNumber && song.trackNumber != null) {
                    Text(
                        text = "${song.trackNumber}.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(end = 8.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = song.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        supportingContent = {
            Text(
                text = "${song.artist} • ${song.album}",
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingContent = {
            AsyncImage(
                model = song.albumArtUri,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop,
                error = rememberVectorPainter(Icons.Default.MusicNote)
            )
        },
        trailingContent = trailingContent ?: {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatDuration(song.duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        modifier = modifier
            .clickable(onClick = onClick)
            .clip(MaterialTheme.shapes.medium)
    )
}

private fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}
