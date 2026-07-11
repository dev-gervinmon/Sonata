package com.gebbers.sonata.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gebbers.sonata.domain.model.Song
import com.gebbers.sonata.domain.repository.MusicRepository
import com.gebbers.sonata.ui.playback.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BrowsingMode {
    object AllSongs : BrowsingMode
    object Artists : BrowsingMode
    object Albums : BrowsingMode
    object Genres : BrowsingMode
    object Years : BrowsingMode
    object Folders : BrowsingMode
    object Playlists : BrowsingMode
    object Favorites : BrowsingMode
    object RecentlyAdded : BrowsingMode
    object RecentlyPlayed : BrowsingMode
    object MostPlayed : BrowsingMode
    data class ArtistDetail(val artist: com.gebbers.sonata.domain.model.Artist) : BrowsingMode
    data class AlbumDetail(val album: com.gebbers.sonata.domain.model.Album) : BrowsingMode
    data class GenreDetail(val genre: String) : BrowsingMode
    data class YearDetail(val year: Int) : BrowsingMode
    data class FolderDetail(val folder: com.gebbers.sonata.domain.model.Folder) : BrowsingMode
    data class PlaylistDetail(val playlist: com.gebbers.sonata.domain.model.Playlist) : BrowsingMode
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    val musicController: MusicController
) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _browsingMode = MutableStateFlow<BrowsingMode>(BrowsingMode.AllSongs)
    val browsingMode: StateFlow<BrowsingMode> = _browsingMode.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val currentSong = musicController.currentSong
    val isPlaying = musicController.isPlaying
    val shuffleModeEnabled = musicController.shuffleModeEnabled
    val repeatMode = musicController.repeatMode
    val currentPosition = musicController.currentPosition
    val duration = musicController.duration
    val sleepTimerMillisLeft = musicController.sleepTimerMillisLeft
    val playbackSpeed = musicController.playbackSpeed
    val playbackPitch = musicController.playbackPitch

    private val _isPlayerSheetVisible = MutableStateFlow(false)
    val isPlayerSheetVisible = _isPlayerSheetVisible.asStateFlow()

    private val _isEqualizerVisible = MutableStateFlow(false)
    val isEqualizerVisible = _isEqualizerVisible.asStateFlow()

    private val _isSettingsVisible = MutableStateFlow(false)
    val isSettingsVisible = _isSettingsVisible.asStateFlow()

    private val _folders = MutableStateFlow<List<com.gebbers.sonata.domain.model.Folder>>(emptyList())
    val folders = _folders.asStateFlow()

    private val _artists = MutableStateFlow<List<com.gebbers.sonata.domain.model.Artist>>(emptyList())
    val artists = _artists.asStateFlow()

    private val _albums = MutableStateFlow<List<com.gebbers.sonata.domain.model.Album>>(emptyList())
    val albums = _albums.asStateFlow()

    private val _playlists = MutableStateFlow<List<com.gebbers.sonata.domain.model.Playlist>>(emptyList())
    val playlists = _playlists.asStateFlow()

    private val _genres = MutableStateFlow<List<String>>(emptyList())
    val genres = _genres.asStateFlow()

    private val _years = MutableStateFlow<List<Int>>(emptyList())
    val years = _years.asStateFlow()

    private val _excludedFolders = MutableStateFlow<List<String>>(emptyList())
    val excludedFolders = _excludedFolders.asStateFlow()

    val recentlyPlayed: StateFlow<List<Song>> = musicRepository.getRecentlyPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val mostPlayed: StateFlow<List<Song>> = musicRepository.getMostPlayed()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        musicController.connect()
        observeSongs()
        observeFolders()
        observeArtists()
        observeAlbums()
        observePlaylists()
        observeGenres()
        observeYears()
        observeExcludedFolders()
        observeCurrentSongForLyrics()
    }

    private fun observeCurrentSongForLyrics() {
        viewModelScope.launch {
            musicController.currentSong.collect { song ->
                if (song != null && song.lyrics == null) {
                    musicRepository.getLyrics(song)
                }
            }
        }
    }

    private fun observeFolders() {
        viewModelScope.launch {
            musicRepository.getAllFolders().collect {
                _folders.value = it
            }
        }
    }

    private fun observeArtists() {
        viewModelScope.launch {
            musicRepository.getAllArtists().collect {
                _artists.value = it
            }
        }
    }

    private fun observeAlbums() {
        viewModelScope.launch {
            musicRepository.getAllAlbums().collect {
                _albums.value = it
            }
        }
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            musicRepository.getAllPlaylists().collect {
                _playlists.value = it
            }
        }
    }

    private fun observeGenres() {
        viewModelScope.launch {
            musicRepository.getAllGenres().collect {
                _genres.value = it
            }
        }
    }

    private fun observeYears() {
        viewModelScope.launch {
            musicRepository.getAllYears().collect {
                _years.value = it
            }
        }
    }

    private fun observeExcludedFolders() {
        viewModelScope.launch {
            musicRepository.getExcludedFolders().collect {
                _excludedFolders.value = it
            }
        }
    }

    private fun observeSongs() {
        viewModelScope.launch {
            combine(_searchQuery.debounce(300L), _browsingMode) { query, mode ->
                query to mode
            }.flatMapLatest { (query, mode) ->
                if (query.isNotBlank()) {
                    musicRepository.searchSongs(query)
                } else {
                    when (mode) {
                        is BrowsingMode.AllSongs -> musicRepository.getAllSongs()
                        is BrowsingMode.Artists -> musicRepository.getAllSongs()
                        is BrowsingMode.Albums -> musicRepository.getAllSongs()
                        is BrowsingMode.Genres -> musicRepository.getAllSongs()
                        is BrowsingMode.Years -> musicRepository.getAllSongs()
                        is BrowsingMode.Folders -> musicRepository.getAllSongs()
                        is BrowsingMode.Playlists -> musicRepository.getAllSongs()
                        is BrowsingMode.Favorites -> musicRepository.getFavoriteSongs()
                        is BrowsingMode.RecentlyAdded -> musicRepository.getRecentlyAdded()
                        is BrowsingMode.RecentlyPlayed -> musicRepository.getRecentlyPlayed()
                        is BrowsingMode.MostPlayed -> musicRepository.getMostPlayed()
                        is BrowsingMode.ArtistDetail -> musicRepository.getSongsByArtist(mode.artist.name)
                        is BrowsingMode.AlbumDetail -> musicRepository.getSongsByAlbum(mode.album.id)
                        is BrowsingMode.GenreDetail -> musicRepository.getSongsByGenre(mode.genre)
                        is BrowsingMode.YearDetail -> musicRepository.getSongsByYear(mode.year)
                        is BrowsingMode.FolderDetail -> musicRepository.getSongsByFolder(mode.folder.path)
                        is BrowsingMode.PlaylistDetail -> musicRepository.getSongsInPlaylist(mode.playlist.id)
                    }
                }
            }
            .onStart { _uiState.value = LibraryUiState.Loading }
            .catch { e -> _uiState.value = LibraryUiState.Error(e.message ?: "Unknown error") }
            .collect { songs ->
                if (songs.isEmpty() && _searchQuery.value.isEmpty()) {
                    _uiState.value = LibraryUiState.Empty
                } else if (songs.isEmpty()) {
                    _uiState.value = LibraryUiState.NoResults
                } else {
                    _uiState.value = LibraryUiState.Success(songs)
                }
            }
        }
    }

    fun createPlaylist(name: String) {
        viewModelScope.launch {
            musicRepository.createPlaylist(name)
        }
    }

    fun addSongToPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.addSongToPlaylist(playlistId, songId)
        }
    }

    fun removeSongFromPlaylist(playlistId: Long, songId: Long) {
        viewModelScope.launch {
            musicRepository.removeSongFromPlaylist(playlistId, songId)
        }
    }

    fun reorderPlaylist(playlistId: Long, songs: List<Song>) {
        viewModelScope.launch {
            musicRepository.reorderPlaylist(playlistId, songs)
        }
    }

    fun moveSongInPlaylist(playlistId: Long, songs: List<Song>, fromIndex: Int, toIndex: Int) {
        if (fromIndex !in songs.indices || toIndex !in songs.indices) return
        val newList = songs.toMutableList()
        val song = newList.removeAt(fromIndex)
        newList.add(toIndex, song)
        reorderPlaylist(playlistId, newList)
    }

    fun updateSongTags(songId: Long, title: String, artist: String, album: String, trackNumber: Int?, discNumber: Int?) {
        viewModelScope.launch {
            musicRepository.updateSongTags(songId, title, artist, album, trackNumber, discNumber)
        }
    }

    private val _artworkSearchResults = MutableStateFlow<List<String>>(emptyList())
    val artworkSearchResults = _artworkSearchResults.asStateFlow()

    fun searchArtwork(query: String) {
        viewModelScope.launch {
            _artworkSearchResults.value = musicRepository.searchOnlineArtwork(query)
        }
    }

    fun setCustomArtwork(songId: Long, uri: String) {
        viewModelScope.launch {
            musicRepository.updateCustomArtwork(songId, uri)
        }
    }

    fun toggleFavorite(song: Song) {
        viewModelScope.launch {
            musicRepository.toggleFavorite(song.mediaStoreId, !song.isFavorite)
        }
    }

    fun setBrowsingMode(mode: BrowsingMode) {
        _browsingMode.value = mode
    }

    fun navigateBack(): Boolean {
        return when (val current = _browsingMode.value) {
            is BrowsingMode.ArtistDetail -> {
                _browsingMode.value = BrowsingMode.Artists
                true
            }
            is BrowsingMode.AlbumDetail -> {
                _browsingMode.value = BrowsingMode.Albums
                true
            }
            is BrowsingMode.GenreDetail -> {
                _browsingMode.value = BrowsingMode.Genres
                true
            }
            is BrowsingMode.YearDetail -> {
                _browsingMode.value = BrowsingMode.Years
                true
            }
            is BrowsingMode.FolderDetail -> {
                _browsingMode.value = BrowsingMode.Folders
                true
            }
            is BrowsingMode.PlaylistDetail -> {
                _browsingMode.value = BrowsingMode.Playlists
                true
            }
            is BrowsingMode.Artists, is BrowsingMode.Albums, is BrowsingMode.Folders, 
            is BrowsingMode.Playlists, is BrowsingMode.Favorites, is BrowsingMode.Genres, is BrowsingMode.Years,
            is BrowsingMode.RecentlyAdded, is BrowsingMode.RecentlyPlayed, is BrowsingMode.MostPlayed -> {
                _browsingMode.value = BrowsingMode.AllSongs
                true
            }
            else -> false
        }
    }

    fun excludeFolder(path: String) {
        viewModelScope.launch {
            musicRepository.excludeFolder(path)
        }
    }

    fun includeFolder(path: String) {
        viewModelScope.launch {
            musicRepository.includeFolder(path)
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun playSong(song: Song) {
        val currentState = uiState.value
        if (currentState is LibraryUiState.Success) {
            musicController.playSong(song, currentState.songs)
        }
    }

    fun togglePlayPause() {
        musicController.togglePlayPause()
    }

    fun skipToNext() {
        musicController.skipToNext()
    }

    fun skipToPrevious() {
        musicController.skipToPrevious()
    }

    fun toggleShuffle() {
        musicController.toggleShuffle()
    }

    fun toggleRepeatMode() {
        musicController.toggleRepeatMode()
    }

    fun seekTo(position: Long) {
        musicController.seekTo(position)
    }

    fun seekForward() {
        musicController.seekForward()
    }

    fun seekBack() {
        musicController.seekBack()
    }

    fun setSleepTimer(minutes: Int) {
        musicController.setSleepTimer(minutes)
    }

    fun cancelSleepTimer() {
        musicController.cancelSleepTimer()
    }

    fun setPlaybackSpeed(speed: Float) {
        musicController.setPlaybackSpeed(speed)
    }

    fun setPlaybackPitch(pitch: Float) {
        musicController.setPlaybackPitch(pitch)
    }

    fun showPlayer() {
        _isPlayerSheetVisible.value = true
    }

    fun hidePlayer() {
        _isPlayerSheetVisible.value = false
    }

    fun showEqualizer() {
        _isEqualizerVisible.value = true
    }

    fun hideEqualizer() {
        _isEqualizerVisible.value = false
    }

    fun showSettings() {
        _isSettingsVisible.value = true
    }

    fun hideSettings() {
        _isSettingsVisible.value = false
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                musicRepository.refreshLibrary()
            } catch (e: Exception) {
                // Log error or show snackbar
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            refreshLibrary()
        } else {
            _uiState.value = LibraryUiState.PermissionDenied
        }
    }

    override fun onCleared() {
        super.onCleared()
        musicController.release()
    }
}

sealed interface LibraryUiState {
    object Loading : LibraryUiState
    data class Success(val songs: List<Song>) : LibraryUiState
    object Empty : LibraryUiState
    object NoResults : LibraryUiState
    object PermissionDenied : LibraryUiState
    data class Error(val message: String) : LibraryUiState
}
