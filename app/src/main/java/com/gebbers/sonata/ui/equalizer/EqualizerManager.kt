package com.gebbers.sonata.ui.equalizer

import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqualizerManager @Inject constructor() {
    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    
    private val _state = MutableStateFlow(EqualizerState())
    val state = _state.asStateFlow()

    fun init(audioSessionId: Int) {
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                enabled = _state.value.isLoudnessEnabled
                setTargetGain(_state.value.loudnessGain)
            }
            loadEqualizerSettings()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadEqualizerSettings() {
        val eq = equalizer ?: return
        val bands = mutableListOf<EqualizerBand>()
        val numBands = eq.numberOfBands
        val levelRange = eq.bandLevelRange

        for (i in 0 until numBands.toInt()) {
            bands.add(
                EqualizerBand(
                    index = i,
                    centerFrequency = eq.getCenterFreq(i.toShort()) / 1000,
                    minLevel = levelRange[0],
                    maxLevel = levelRange[1],
                    currentLevel = eq.getBandLevel(i.toShort())
                )
            )
        }

        val presets = mutableListOf<String>()
        for (i in 0 until eq.numberOfPresets.toInt()) {
            presets.add(eq.getPresetName(i.toShort()))
        }

        _state.value = _state.value.copy(
            bands = bands,
            presets = presets
        )
    }

    fun setEnabled(enabled: Boolean) {
        equalizer?.enabled = enabled
        _state.value = _state.value.copy(isEnabled = enabled)
    }

    fun setLoudnessEnabled(enabled: Boolean) {
        loudnessEnhancer?.enabled = enabled
        _state.value = _state.value.copy(isLoudnessEnabled = enabled)
    }

    fun setLoudnessGain(gain: Int) {
        loudnessEnhancer?.setTargetGain(gain)
        _state.value = _state.value.copy(loudnessGain = gain)
    }

    fun setBandLevel(bandIndex: Int, level: Short) {
        equalizer?.setBandLevel(bandIndex.toShort(), level)
        val newBands = _state.value.bands.map {
            if (it.index == bandIndex) it.copy(currentLevel = level) else it
        }
        _state.value = _state.value.copy(bands = newBands, currentPreset = -1)
    }

    fun setPreset(presetIndex: Int) {
        equalizer?.usePreset(presetIndex.toShort())
        _state.value = _state.value.copy(currentPreset = presetIndex)
        loadEqualizerSettings()
    }

    fun release() {
        equalizer?.release()
        loudnessEnhancer?.release()
        equalizer = null
        loudnessEnhancer = null
    }
}
