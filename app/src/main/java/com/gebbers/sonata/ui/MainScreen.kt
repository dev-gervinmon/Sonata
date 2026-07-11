package com.gebbers.sonata.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.gebbers.sonata.ui.home.HomeScreen
import com.gebbers.sonata.ui.library.LibraryScreen
import com.gebbers.sonata.ui.library.LibraryViewModel
import com.gebbers.sonata.ui.library.SearchScreen
import com.gebbers.sonata.ui.equalizer.EqualizerViewModel
import com.gebbers.sonata.ui.settings.SettingsViewModel
import com.gebbers.sonata.ui.playback.MiniPlayer
import com.gebbers.sonata.ui.playback.PlayerScreen
import com.gebbers.sonata.ui.equalizer.EqualizerScreen
import com.gebbers.sonata.ui.settings.SettingsScreen

sealed class Screen(val label: String, val icon: ImageVector) {
    object Home : Screen("Home", Icons.Default.Home)
    object Search : Screen("Search", Icons.Default.Search)
    object Library : Screen("Library", Icons.Default.LibraryMusic)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    libraryViewModel: LibraryViewModel,
    equalizerViewModel: EqualizerViewModel,
    settingsViewModel: SettingsViewModel
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    
    val currentSong by libraryViewModel.currentSong.collectAsState()
    val isPlaying by libraryViewModel.isPlaying.collectAsState()
    val isPlayerVisible by libraryViewModel.isPlayerSheetVisible.collectAsState()
    val isEqualizerVisible by libraryViewModel.isEqualizerVisible.collectAsState()
    val isSettingsVisible by libraryViewModel.isSettingsVisible.collectAsState()
    
    val currentPosition by libraryViewModel.currentPosition.collectAsState()
    val duration by libraryViewModel.duration.collectAsState()
    val shuffleModeEnabled by libraryViewModel.shuffleModeEnabled.collectAsState()
    val repeatMode by libraryViewModel.repeatMode.collectAsState()
    val sleepTimerMillisLeft by libraryViewModel.sleepTimerMillisLeft.collectAsState()
    val playbackSpeed by libraryViewModel.playbackSpeed.collectAsState()
    val playbackPitch by libraryViewModel.playbackPitch.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (isEqualizerVisible) {
        EqualizerScreen(
            viewModel = equalizerViewModel,
            onNavigateBack = { libraryViewModel.hideEqualizer() }
        )
    } else if (isSettingsVisible) {
        SettingsScreen(
            viewModel = settingsViewModel,
            onNavigateBack = { libraryViewModel.hideSettings() }
        )
    } else {
        Scaffold(
            bottomBar = {
                Column {
                    MiniPlayer(
                        song = currentSong,
                        isPlaying = isPlaying,
                        onTogglePlayPause = { libraryViewModel.togglePlayPause() },
                        onNext = { libraryViewModel.skipToNext() },
                        onPrevious = { libraryViewModel.skipToPrevious() },
                        onClick = { libraryViewModel.showPlayer() }
                    )
                    NavigationBar {
                        val screens = listOf(Screen.Home, Screen.Search, Screen.Library)
                        screens.forEach { screen ->
                            NavigationBarItem(
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen },
                                icon = { Icon(screen.icon, contentDescription = null) },
                                label = { Text(screen.label) }
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                when (currentScreen) {
                    Screen.Home -> HomeScreen(
                        viewModel = libraryViewModel,
                        onSongClick = { libraryViewModel.playSong(it) }
                    )
                    Screen.Search -> SearchScreen(
                        viewModel = libraryViewModel,
                        onSongClick = { libraryViewModel.playSong(it) }
                    )
                    Screen.Library -> LibraryScreen(
                        viewModel = libraryViewModel,
                    )
                }
            }
        }
    }

    if (isPlayerVisible) {
        ModalBottomSheet(
            onDismissRequest = { libraryViewModel.hidePlayer() },
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
                onTogglePlayPause = { libraryViewModel.togglePlayPause() },
                onToggleShuffle = { libraryViewModel.toggleShuffle() },
                onToggleRepeatMode = { libraryViewModel.toggleRepeatMode() },
                onSkipNext = { libraryViewModel.skipToNext() },
                onSkipPrevious = { libraryViewModel.skipToPrevious() },
                onSeekForward = { libraryViewModel.seekForward() },
                onSeekBack = { libraryViewModel.seekBack() },
                onSeek = { libraryViewModel.seekTo(it) },
                onSetSleepTimer = { libraryViewModel.setSleepTimer(it) },
                onCancelSleepTimer = { libraryViewModel.cancelSleepTimer() },
                onSetPlaybackSpeed = { libraryViewModel.setPlaybackSpeed(it) },
                onSetPlaybackPitch = { libraryViewModel.setPlaybackPitch(it) },
                onToggleFavorite = { libraryViewModel.toggleFavorite(it) },
                onOpenEqualizer = {
                    libraryViewModel.hidePlayer()
                    libraryViewModel.showEqualizer()
                },
                onClose = { libraryViewModel.hidePlayer() }
            )
        }
    }
}
