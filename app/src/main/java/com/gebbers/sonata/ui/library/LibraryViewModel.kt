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
    object Folders : BrowsingMode
    data class FolderDetail(val folder: com.gebbers.sonata.domain.model.Folder) : BrowsingMode
}

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicController: MusicController
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

    private val _folders = MutableStateFlow<List<com.gebbers.sonata.domain.model.Folder>>(emptyList())
    val folders = _folders.asStateFlow()

    init {
        musicController.connect()
        observeSongs()
        observeFolders()
    }

    private fun observeFolders() {
        viewModelScope.launch {
            musicRepository.getAllFolders().collect {
                _folders.value = it
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
                        is BrowsingMode.Folders -> musicRepository.getAllSongs() // Not really used for list
                        is BrowsingMode.FolderDetail -> musicRepository.getSongsByFolder(mode.folder.path)
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

    fun setBrowsingMode(mode: BrowsingMode) {
        _browsingMode.value = mode
    }

    fun navigateBack(): Boolean {
        return when (val current = _browsingMode.value) {
            is BrowsingMode.FolderDetail -> {
                _browsingMode.value = BrowsingMode.Folders
                true
            }
            is BrowsingMode.Folders -> {
                _browsingMode.value = BrowsingMode.AllSongs
                true
            }
            else -> false
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
