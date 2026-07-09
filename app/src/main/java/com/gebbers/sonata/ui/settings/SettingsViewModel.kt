package com.gebbers.sonata.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gebbers.sonata.data.preferences.PreferenceManager
import com.gebbers.sonata.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsEvent {
    data class Toast(val message: String) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _isScanning = MutableStateFlow(false)
    val isScanning = _isScanning.asStateFlow()

    private val _eventFlow = MutableSharedFlow<SettingsEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    val dynamicTheming: StateFlow<Boolean> = preferenceManager.dynamicTheming
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val darkMode: StateFlow<String> = preferenceManager.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val fadeDuration: StateFlow<Long> = preferenceManager.fadeDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1000L)

    val excludedFolders = musicRepository.getExcludedFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scannedFolders = musicRepository.getScannedFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setDynamicTheming(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDynamicTheming(enabled)
        }
    }

    fun setDarkMode(mode: String) {
        viewModelScope.launch {
            preferenceManager.setDarkMode(mode)
        }
    }

    fun setFadeDuration(duration: Long) {
        viewModelScope.launch {
            preferenceManager.setFadeDuration(duration)
        }
    }

    fun includeFolder(path: String) {
        viewModelScope.launch {
            musicRepository.includeFolder(path)
        }
    }

    fun addScannedFolder(path: String, name: String) {
        viewModelScope.launch {
            musicRepository.addScannedFolder(path, name)
        }
    }

    fun removeScannedFolder(path: String) {
        viewModelScope.launch {
            musicRepository.removeScannedFolder(path)
        }
    }

    fun rescanLibrary() {
        viewModelScope.launch {
            _isScanning.value = true
            try {
                musicRepository.refreshLibrary()
                _eventFlow.emit(SettingsEvent.Toast("Library rescan complete"))
            } catch (e: Exception) {
                _eventFlow.emit(SettingsEvent.Toast("Rescan failed: ${e.message}"))
            } finally {
                _isScanning.value = false
            }
        }
    }
}
