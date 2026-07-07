package com.gebbers.sonata.ui.equalizer

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EqualizerViewModel @Inject constructor(
    private val equalizerManager: EqualizerManager
) : ViewModel() {
    val state = equalizerManager.state

    fun setEnabled(enabled: Boolean) {
        equalizerManager.setEnabled(enabled)
    }

    fun setBandLevel(bandIndex: Int, level: Short) {
        equalizerManager.setBandLevel(bandIndex, level)
    }

    fun setPreset(presetIndex: Int) {
        equalizerManager.setPreset(presetIndex)
    }
}
