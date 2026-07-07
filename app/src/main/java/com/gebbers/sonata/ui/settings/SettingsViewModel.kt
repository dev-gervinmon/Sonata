package com.gebbers.sonata.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gebbers.sonata.data.preferences.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager
) : ViewModel() {

    val dynamicTheming: StateFlow<Boolean> = preferenceManager.dynamicTheming
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val darkMode: StateFlow<String> = preferenceManager.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val fadeDuration: StateFlow<Long> = preferenceManager.fadeDuration
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1000L)

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
}
