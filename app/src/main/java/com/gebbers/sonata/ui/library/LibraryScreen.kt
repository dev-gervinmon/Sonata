package com.gebbers.sonata.ui.library

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.gebbers.sonata.domain.model.Song
import com.gebbers.sonata.ui.equalizer.EqualizerViewModel
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
    val artworkSearchResults by viewModel.artworkSearchResults.collectAsState()

    var selectedSongForMenu by remember { mutableStateOf<Song?>(null) }
    var songForTagEditing by remember { mutableStateOf<Song?>(null) }
    var songForArtworkSelection by remember { mutableStateOf<Song?>(null) }
    var isAddingToPlaylist by remember { mutableStateOf(false) }

    val playlistSheetState = rememberModalBottomSheetState()

    Column(modifier = Modifier.fillMaxSize()) {
        if (browsingMode !is BrowsingMode.FolderDetail && 
            browsingMode !is BrowsingMode.ArtistDetail && 
            browsingMode !is BrowsingMode.AlbumDetail && 
            browsingMode !is BrowsingMode.GenreDetail &&
            browsingMode !is BrowsingMode.YearDetail &&
            browsingMode !is BrowsingMode.PlaylistDetail) {
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Library",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.showSettings() }) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }

            TextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Search songs, artists...") },
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
                val tabs = listOf("Songs", "Artists", "Albums", "Genres", "Years", "Folders", "Playlists", "Favorites", "Recent", "History", "Top")
                val modes = listOf(
                    BrowsingMode.AllSongs, BrowsingMode.Artists, BrowsingMode.Albums, 
                    BrowsingMode.Genres, BrowsingMode.Years, BrowsingMode.Folders, 
                    BrowsingMode.Playlists, BrowsingMode.Favorites, 
                    BrowsingMode.RecentlyAdded, BrowsingMode.RecentlyPlayed, BrowsingMode.MostPlayed
                )
                
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = browsingMode == modes[index],
                        onClick = { viewModel.setBrowsingMode(modes[index]) },
                        text = { Text(title) }
                    )
                }
            }
        } else {
            // Header for detail views
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (val mode = browsingMode) {
                            is BrowsingMode.FolderDetail -> mode.folder.name
                            is BrowsingMode.ArtistDetail -> mode.artist.name
                            is BrowsingMode.AlbumDetail -> mode.album.name
                            is BrowsingMode.GenreDetail -> mode.genre
                            is BrowsingMode.YearDetail -> mode.year.toString()
                            is BrowsingMode.PlaylistDetail -> mode.playlist.name
                            else -> ""
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.navigateBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = browsingMode,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
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
                        val state = uiState
                        if (state is LibraryUiState.Loading) {
                            ShimmerList()
                        } else if (state is LibraryUiState.Success) {
                            SongList(
                                songs = state.songs,
                                onSongClick = { viewModel.playSong(it) },
                                onMoreClick = { selectedSongForMenu = it },
                                showTrackNumbers = mode is BrowsingMode.AlbumDetail
                            )
                        } else if (state is LibraryUiState.Empty) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No music found")
                            }
                        } else if (state is LibraryUiState.NoResults) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No results found for \"$searchQuery\"")
                            }
                        }
                    }
                }
            }
        }
    }

    if (selectedSongForMenu != null) {
        ModalBottomSheet(
            onDismissRequest = { 
                selectedSongForMenu = null
                isAddingToPlaylist = false
            }
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
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
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
                    ListItem(
                        headlineContent = { Text(selectedSongForMenu!!.title, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text(selectedSongForMenu!!.artist) },
                        leadingContent = {
                            AsyncImage(
                                model = selectedSongForMenu!!.albumArtUri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Crop,
                                error = androidx.compose.ui.graphics.vector.rememberVectorPainter(Icons.Default.MusicNote)
                            )
                        }
                    )
                    HorizontalDivider()
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
                    if (browsingMode is BrowsingMode.PlaylistDetail) {
                        val currentSongs = (uiState as? LibraryUiState.Success)?.songs ?: emptyList()
                        val currentIndex = currentSongs.indexOf(selectedSongForMenu)
                        
                        ListItem(
                            headlineContent = { Text("Move up") },
                            leadingContent = { Icon(Icons.Default.ArrowUpward, contentDescription = null) },
                            modifier = Modifier.clickable(enabled = currentIndex > 0) {
                                viewModel.moveSongInPlaylist((browsingMode as BrowsingMode.PlaylistDetail).playlist.id, currentSongs, currentIndex, currentIndex - 1)
                                selectedSongForMenu = null
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Move down") },
                            leadingContent = { Icon(Icons.Default.ArrowDownward, contentDescription = null) },
                            modifier = Modifier.clickable(enabled = currentIndex < currentSongs.size - 1) {
                                viewModel.moveSongInPlaylist((browsingMode as BrowsingMode.PlaylistDetail).playlist.id, currentSongs, currentIndex, currentIndex + 1)
                                selectedSongForMenu = null
                            }
                        )
                        ListItem(
                            headlineContent = { Text("Remove from playlist") },
                            leadingContent = { Icon(Icons.Default.Delete, contentDescription = null) },
                            modifier = Modifier.clickable {
                                viewModel.removeSongFromPlaylist((browsingMode as BrowsingMode.PlaylistDetail).playlist.id, selectedSongForMenu!!.mediaStoreId)
                                selectedSongForMenu = null
                            }
                        )
                    }
                    ListItem(
                        headlineContent = { Text("Change artwork") },
                        leadingContent = { Icon(Icons.Default.Image, contentDescription = null) },
                        modifier = Modifier.clickable { 
                            songForArtworkSelection = selectedSongForMenu
                            selectedSongForMenu = null
                        }
                    )
                }
            }
        }
    }

    if (songForArtworkSelection != null) {
        ArtworkPickerDialog(
            song = songForArtworkSelection!!,
            searchResults = artworkSearchResults,
            onSearch = { viewModel.searchArtwork(it) },
            onSelect = { uri ->
                viewModel.setCustomArtwork(songForArtworkSelection!!.mediaStoreId, uri)
                songForArtworkSelection = null
            },
            onDismiss = { songForArtworkSelection = null }
        )
    }

    if (songForTagEditing != null) {
        TagEditorDialog(
            song = songForTagEditing!!,
            onDismiss = { songForTagEditing = null },
            onSave = { title, artist, album, track, disc ->
                viewModel.updateSongTags(songForTagEditing!!.mediaStoreId, title, artist, album, track, disc)
                songForTagEditing = null
            }
        )
    }
}

@Composable
fun TagEditorDialog(
    song: Song,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int?, Int?) -> Unit
) {
    var title by remember { mutableStateOf(song.title) }
    var artist by remember { mutableStateOf(song.artist) }
    var album by remember { mutableStateOf(song.album) }
    var trackNumber by remember { mutableStateOf(song.trackNumber?.toString() ?: "") }
    var discNumber by remember { mutableStateOf(song.discNumber?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Tags", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = trackNumber,
                        onValueChange = { if (it.all { char -> char.isDigit() }) trackNumber = it },
                        label = { Text("Track") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = discNumber,
                        onValueChange = { if (it.all { char -> char.isDigit() }) discNumber = it },
                        label = { Text("Disc") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                onSave(
                    title, 
                    artist, 
                    album, 
                    trackNumber.toIntOrNull(), 
                    discNumber.toIntOrNull()
                ) 
            }) {
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
