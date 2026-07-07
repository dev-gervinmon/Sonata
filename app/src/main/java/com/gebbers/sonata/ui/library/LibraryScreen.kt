package com.gebbers.sonata.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
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
import com.gebbers.sonata.ui.equalizer.EqualizerScreen
import com.gebbers.sonata.ui.equalizer.EqualizerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    equalizerViewModel: EqualizerViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val browsingMode by viewModel.browsingMode.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val shuffleModeEnabled by viewModel.shuffleModeEnabled.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()
    val currentPosition by viewModel.currentPosition.collectAsState()
    val duration by viewModel.duration.collectAsState()
    val sleepTimerMillisLeft by viewModel.sleepTimerMillisLeft.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()
    val playbackPitch by viewModel.playbackPitch.collectAsState()
    val isPlayerVisible by viewModel.isPlayerSheetVisible.collectAsState()
    val isEqualizerVisible by viewModel.isEqualizerVisible.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    BackHandler(enabled = browsingMode !is BrowsingMode.AllSongs) {
        viewModel.navigateBack()
    }

    if (isEqualizerVisible) {
        EqualizerScreen(
            viewModel = equalizerViewModel,
            onNavigateBack = { viewModel.hideEqualizer() }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (val mode = browsingMode) {
                                is BrowsingMode.FolderDetail -> mode.folder.name
                                else -> "Sonata Music"
                            }
                        )
                    }
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
                if (browsingMode !is BrowsingMode.FolderDetail && browsingMode !is BrowsingMode.PlaylistDetail) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        placeholder = { Text("Search library...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                            unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                        )
                    )

                    PrimaryTabRow(
                        selectedTabIndex = when (browsingMode) {
                            is BrowsingMode.AllSongs -> 0
                            is BrowsingMode.Folders -> 1
                            is BrowsingMode.Playlists -> 2
                            else -> 0
                        },
                        containerColor = MaterialTheme.colorScheme.background,
                        divider = {}
                    ) {
                        Tab(
                            selected = browsingMode is BrowsingMode.AllSongs,
                            onClick = { viewModel.setBrowsingMode(BrowsingMode.AllSongs) },
                            text = { Text("Songs") }
                        )
                        Tab(
                            selected = browsingMode is BrowsingMode.Folders,
                            onClick = { viewModel.setBrowsingMode(BrowsingMode.Folders) },
                            text = { Text("Folders") }
                        )
                        Tab(
                            selected = browsingMode is BrowsingMode.Playlists,
                            onClick = { viewModel.setBrowsingMode(BrowsingMode.Playlists) },
                            text = { Text("Playlists") }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        browsingMode is BrowsingMode.Folders && searchQuery.isEmpty() -> {
                            FolderList(
                                folders = folders,
                                onFolderClick = { viewModel.setBrowsingMode(BrowsingMode.FolderDetail(it)) }
                            )
                        }
                        browsingMode is BrowsingMode.Playlists && searchQuery.isEmpty() -> {
                            PlaylistList(
                                playlists = playlists,
                                onPlaylistClick = { viewModel.setBrowsingMode(BrowsingMode.PlaylistDetail(it)) },
                                onCreatePlaylist = { viewModel.createPlaylist(it) }
                            )
                        }
                        else -> {
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
                                        text = when (browsingMode) {
                                            is BrowsingMode.PlaylistDetail -> "This playlist is empty"
                                            else -> "No music found on device"
                                        },
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
                sleepTimerMillisLeft = sleepTimerMillisLeft,
                playbackSpeed = playbackSpeed,
                playbackPitch = playbackPitch,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeatMode = { viewModel.toggleRepeatMode() },
                onSkipNext = { viewModel.skipToNext() },
                onSkipPrevious = { viewModel.skipToPrevious() },
                onSeekForward = { viewModel.seekForward() },
                onSeekBack = { viewModel.seekBack() },
                onSeek = { viewModel.seekTo(it) },
                onSetSleepTimer = { viewModel.setSleepTimer(it) },
                onCancelSleepTimer = { viewModel.cancelSleepTimer() },
                onSetPlaybackSpeed = { viewModel.setPlaybackSpeed(it) },
                onSetPlaybackPitch = { viewModel.setPlaybackPitch(it) },
                onOpenEqualizer = {
                    viewModel.hidePlayer()
                    viewModel.showEqualizer()
                },
                onClose = { viewModel.hidePlayer() }
            )
        }
    }
}
