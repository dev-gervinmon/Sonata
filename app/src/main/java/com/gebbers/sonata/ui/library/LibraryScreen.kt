package com.gebbers.sonata.ui.library

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gebbers.sonata.ui.playback.MiniPlayer
import com.gebbers.sonata.ui.playback.PlayerScreen
import com.gebbers.sonata.ui.equalizer.EqualizerScreen
import com.gebbers.sonata.ui.equalizer.EqualizerViewModel
import com.gebbers.sonata.ui.settings.SettingsScreen
import com.gebbers.sonata.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    equalizerViewModel: EqualizerViewModel,
    settingsViewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val browsingMode by viewModel.browsingMode.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val artists by viewModel.artists.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val years by viewModel.years.collectAsState()
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
    val isSettingsVisible by viewModel.isSettingsVisible.collectAsState()

    var selectedSongForMenu by remember { mutableStateOf<com.gebbers.sonata.domain.model.Song?>(null) }
    var songForTagEditing by remember { mutableStateOf<com.gebbers.sonata.domain.model.Song?>(null) }
    var isAddingToPlaylist by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val playlistSheetState = rememberModalBottomSheetState()

    BackHandler(enabled = browsingMode !is BrowsingMode.AllSongs || isEqualizerVisible || isSettingsVisible) {
        when {
            isEqualizerVisible -> viewModel.hideEqualizer()
            isSettingsVisible -> viewModel.hideSettings()
            else -> viewModel.navigateBack()
        }
    }

    if (isEqualizerVisible) {
        EqualizerScreen(
            viewModel = equalizerViewModel,
            onNavigateBack = { viewModel.hideEqualizer() }
        )
    } else if (isSettingsVisible) {
        SettingsScreen(
            viewModel = settingsViewModel,
            onNavigateBack = { viewModel.hideSettings() }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = when (val mode = browsingMode) {
                                is BrowsingMode.FolderDetail -> mode.folder.name
                                is BrowsingMode.ArtistDetail -> mode.artist.name
                                is BrowsingMode.AlbumDetail -> mode.album.name
                                is BrowsingMode.GenreDetail -> mode.genre
                                is BrowsingMode.YearDetail -> mode.year.toString()
                                is BrowsingMode.PlaylistDetail -> mode.playlist.name
                                BrowsingMode.RecentlyAdded -> "Recently Added"
                                BrowsingMode.RecentlyPlayed -> "Recently Played"
                                BrowsingMode.MostPlayed -> "Most Played"
                                else -> "Sonata Music"
                            }
                        )
                    },
                    actions = {
                        IconButton(onClick = { viewModel.showSettings() }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    }
                )
            },
            bottomBar = {
                MiniPlayer(
                    song = currentSong,
                    isPlaying = isPlaying,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.skipToNext() },
                    onPrevious = { viewModel.skipToPrevious() },
                    onClick = { viewModel.showPlayer() }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (browsingMode !is BrowsingMode.FolderDetail && 
                    browsingMode !is BrowsingMode.ArtistDetail && 
                    browsingMode !is BrowsingMode.AlbumDetail && 
                    browsingMode !is BrowsingMode.GenreDetail &&
                    browsingMode !is BrowsingMode.YearDetail &&
                    browsingMode !is BrowsingMode.PlaylistDetail) {
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

                    ScrollableTabRow(
                        selectedTabIndex = when (browsingMode) {
                            is BrowsingMode.AllSongs -> 0
                            is BrowsingMode.Artists -> 1
                            is BrowsingMode.Albums -> 2
                            is BrowsingMode.Genres -> 3
                            is BrowsingMode.Years -> 4
                            is BrowsingMode.Folders -> 5
                            is BrowsingMode.Playlists -> 6
                            is BrowsingMode.Favorites -> 7
                            is BrowsingMode.RecentlyAdded -> 8
                            is BrowsingMode.RecentlyPlayed -> 9
                            is BrowsingMode.MostPlayed -> 10
                            else -> 0
                        },
                        containerColor = MaterialTheme.colorScheme.background,
                        divider = {},
                        edgePadding = 16.dp
                    ) {
                        Tab(
                            selected = browsingMode is BrowsingMode.AllSongs,
                            onClick = { viewModel.setBrowsingMode(BrowsingMode.AllSongs) },
                            text = { Text("Songs") }
                        )
                        Tab(
                            selected = browsingMode is BrowsingMode.Artists,
                            onClick = { viewModel.setBrowsingMode(BrowsingMode.Artists) },
                            text = { Text("Artists") }
                        )
                        Tab(
                            selected = browsingMode is BrowsingMode.Albums,
                            onClick = { viewModel.setBrowsingMode(BrowsingMode.Albums) },
                            text = { Text("Albums") }
                        )
                        Tab(
                            selected = browsingMode is BrowsingMode.Genres,
                            onClick = { viewModel.setBrowsingMode(BrowsingMode.Genres) },
                            text = { Text("Genres") }
                        )
                        Tab(
                            selected = browsingMode is BrowsingMode.Years,
                            onClick = { viewModel.setBrowsingMode(BrowsingMode.Years) },
                            text = { Text("Years") }
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
                        Tab(
                            selected = browsingMode is BrowsingMode.Favorites,
                            onClick = { viewModel.setBrowsingMode(BrowsingMode.Favorites) },
                            text = { Text("Favorites") }
                        )
                        Tab(
                            selected = browsingMode is BrowsingMode.RecentlyAdded,
                            onClick = { viewModel.setBrowsingMode(BrowsingMode.RecentlyAdded) },
                            text = { Text("Recent") }
                        )
                        Tab(
                            selected = browsingMode is BrowsingMode.RecentlyPlayed,
                            onClick = { viewModel.setBrowsingMode(BrowsingMode.RecentlyPlayed) },
                            text = { Text("History") }
                        )
                        Tab(
                            selected = browsingMode is BrowsingMode.MostPlayed,
                            onClick = { viewModel.setBrowsingMode(BrowsingMode.MostPlayed) },
                            text = { Text("Top") }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    AnimatedContent(
                        targetState = browsingMode,
                        transitionSpec = {
                            if (targetState is BrowsingMode.AllSongs) {
                                slideInHorizontally { -it } + fadeIn() togetherWith
                                        slideOutHorizontally { it } + fadeOut()
                            } else {
                                slideInHorizontally { it } + fadeIn() togetherWith
                                        slideOutHorizontally { -it } + fadeOut()
                            }.using(SizeTransform(clip = false))
                        },
                        label = "browsing_mode_transition"
                    ) { mode ->
                        when {
                            mode is BrowsingMode.Artists && searchQuery.isEmpty() -> {
                                ArtistList(
                                    artists = artists,
                                    onArtistClick = { viewModel.setBrowsingMode(BrowsingMode.ArtistDetail(it)) }
                                )
                            }
                            mode is BrowsingMode.Albums && searchQuery.isEmpty() -> {
                                AlbumGrid(
                                    albums = albums,
                                    onAlbumClick = { viewModel.setBrowsingMode(BrowsingMode.AlbumDetail(it)) }
                                )
                            }
                            mode is BrowsingMode.Genres && searchQuery.isEmpty() -> {
                                GenreList(
                                    genres = genres,
                                    onGenreClick = { viewModel.setBrowsingMode(BrowsingMode.GenreDetail(it)) }
                                )
                            }
                            mode is BrowsingMode.Years && searchQuery.isEmpty() -> {
                                YearList(
                                    years = years,
                                    onYearClick = { viewModel.setBrowsingMode(BrowsingMode.YearDetail(it)) }
                                )
                            }
                        mode is BrowsingMode.Folders && searchQuery.isEmpty() -> {
                            FolderList(
                                folders = folders,
                                onFolderClick = { viewModel.setBrowsingMode(BrowsingMode.FolderDetail(it)) },
                                onExcludeFolder = { viewModel.excludeFolder(it) }
                            )
                        }
                            mode is BrowsingMode.Playlists && searchQuery.isEmpty() -> {
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
                                            onSongClick = { viewModel.playSong(it) },
                                            onMoreClick = { selectedSongForMenu = it }
                                        )
                                    }
                                    is LibraryUiState.Empty -> {
                                        Text(
                                            text = when (mode) {
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
                onToggleFavorite = { viewModel.toggleFavorite(it) },
                onOpenEqualizer = {
                    viewModel.hidePlayer()
                    viewModel.showEqualizer()
                },
                onClose = { viewModel.hidePlayer() }
            )
        }
    }

    if (selectedSongForMenu != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                selectedSongForMenu = null
                isAddingToPlaylist = false
            },
            sheetState = playlistSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            ) {
                if (isAddingToPlaylist) {
                    Text(
                        text = "Add to Playlist",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                    playlists.forEach { playlist ->
                        ListItem(
                            headlineContent = { Text(playlist.name) },
                            leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null) },
                            modifier = Modifier.clickable {
                                viewModel.addSongToPlaylist(playlist.id, selectedSongForMenu!!.mediaStoreId)
                                selectedSongForMenu = null
                                isAddingToPlaylist = false
                            }
                        )
                    }
                } else {
                    Text(
                        text = selectedSongForMenu?.title ?: "Song Options",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    ListItem(
                        headlineContent = { Text("Add to playlist") },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.PlaylistPlay, contentDescription = null) },
                        modifier = Modifier.clickable { isAddingToPlaylist = true }
                    )
                    ListItem(
                        headlineContent = { Text("Edit tags") },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null) },
                        modifier = Modifier.clickable { 
                            songForTagEditing = selectedSongForMenu
                            selectedSongForMenu = null
                        }
                    )
                }
            }
        }
    }

    if (songForTagEditing != null) {
        TagEditorDialog(
            song = songForTagEditing!!,
            onDismiss = { songForTagEditing = null },
            onSave = { title, artist, album ->
                viewModel.updateSongTags(songForTagEditing!!.mediaStoreId, title, artist, album)
                songForTagEditing = null
            }
        )
    }
}

@Composable
fun TagEditorDialog(
    song: com.gebbers.sonata.domain.model.Song,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tags") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("Artist") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = album,
                    onValueChange = { album = it },
                    label = { Text("Album") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, artist, album) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
