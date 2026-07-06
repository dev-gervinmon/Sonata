package com.gebbers.sonata.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gebbers.sonata.domain.model.Song
import com.gebbers.sonata.domain.repository.MusicRepository
import com.gebbers.sonata.ui.playback.MusicController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val musicRepository: MusicRepository,
    private val musicController: MusicController
) : ViewModel() {

    private val _uiState = MutableStateFlow<LibraryUiState>(LibraryUiState.Loading)
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val currentSong = musicController.currentSong
    val isPlaying = musicController.isPlaying
    val currentPosition = musicController.currentPosition
    val duration = musicController.duration

    private val _isPlayerSheetVisible = MutableStateFlow(false)
    val isPlayerSheetVisible = _isPlayerSheetVisible.asStateFlow()

    init {
        musicController.connect()
        observeSongs()
    }

    private fun observeSongs() {
        viewModelScope.launch {
            musicRepository.getAllSongs()
                .onStart { _uiState.value = LibraryUiState.Loading }
                .catch { e -> _uiState.value = LibraryUiState.Error(e.message ?: "Unknown error") }
                .collect { songs ->
                    if (songs.isEmpty()) {
                        _uiState.value = LibraryUiState.Empty
                    } else {
                        _uiState.value = LibraryUiState.Success(songs)
                    }
                }
        }
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

    fun seekTo(position: Long) {
        musicController.seekTo(position)
    }

    fun showPlayer() {
        _isPlayerSheetVisible.value = true
    }

    fun hidePlayer() {
        _isPlayerSheetVisible.value = false
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
    object PermissionDenied : LibraryUiState
    data class Error(val message: String) : LibraryUiState
}
