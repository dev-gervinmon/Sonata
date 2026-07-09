package com.gebbers.sonata.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gebbers.sonata.data.preferences.PreferenceManager
import com.gebbers.sonata.domain.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val musicRepository: MusicRepository
) : ViewModel() {

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

    fun bulkCleanTags() {
        viewModelScope.launch {
            musicRepository.bulkCleanTags()
        }
    }
}
