package com.gebbers.sonata.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gebbers.sonata.ui.playback.MiniPlayer
import com.gebbers.sonata.ui.playback.PlayerScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val shuffleModeEnabled by viewModel.shuffleModeEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val isPlayerVisible by viewModel.isPlayerSheetVisible.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sonata Music") }
            )
        },
        bottomBar = {
            MiniPlayer(
                song = currentSong,
                isPlaying = isPlaying,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onClick = { viewModel.showPlayer() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search songs, artists, albums...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                    unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is LibraryUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is LibraryUiState.Success -> {
                        SongList(
                            songs = state.songs,
                            onSongClick = { viewModel.playSong(it) }
                        )
                    }
                    is LibraryUiState.Empty -> {
                        Text(
                            text = "No music found on device",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is LibraryUiState.NoResults -> {
                        Text(
                            text = "No results found for \"$searchQuery\"",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is LibraryUiState.PermissionDenied -> {
                        Text(
                            text = "Permission denied. Please grant storage access.",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is LibraryUiState.Error -> {
                        Text(
                            text = "Error: ${state.message}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }

    if (isPlayerVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hidePlayer() },
            sheetState = sheetState,
            dragHandle = null,
            modifier = Modifier.fillMaxSize()
        ) {
            PlayerScreen(
                song = currentSong,
                isPlaying = isPlaying,
                shuffleModeEnabled = shuffleModeEnabled,
                repeatMode = repeatMode,
                currentPosition = currentPosition,
                duration = duration,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeatMode = { viewModel.toggleRepeatMode() },
                onSkipNext = { viewModel.skipToNext() },
                onSkipPrevious = { viewModel.skipToPrevious() },
                onSeek = { viewModel.seekTo(it) },
                onClose = { viewModel.hidePlayer() }
            )
        }
    }
}
