package com.gebbers.sonata.ui.equalizer

import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.BassBoost
import android.media.audiofx.Virtualizer
import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EqualizerManager @Inject constructor() {
    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var bassBoost: BassBoost? = null
    private var virtualizer: Virtualizer? = null
    private var visualizer: Visualizer? = null
    
    private val _state = MutableStateFlow(EqualizerState())
    val state = _state.asStateFlow()

    private val _fftData = MutableStateFlow<FloatArray>(FloatArray(0))
    val fftData = _fftData.asStateFlow()

    fun init(audioSessionId: Int) {
        try {
            equalizer = Equalizer(0, audioSessionId).apply {
                enabled = _state.value.isEnabled
            }
            loudnessEnhancer = LoudnessEnhancer(audioSessionId).apply {
                enabled = _state.value.isLoudnessEnabled
                setTargetGain(_state.value.loudnessGain)
            }
            bassBoost = BassBoost(0, audioSessionId).apply {
                enabled = _state.value.isBassBoostEnabled
                setStrength(_state.value.bassBoostStrength)
            }
            virtualizer = Virtualizer(0, audioSessionId).apply {
                enabled = _state.value.isVirtualizerEnabled
                setStrength(_state.value.virtualizerStrength)
            }
            
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[1]
                setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                    override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {}
                    override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                        fft?.let { processFft(it) }
                    }
                }, Visualizer.getMaxCaptureRate() / 2, false, true)
                enabled = true
            }

            loadEqualizerSettings()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processFft(fft: ByteArray) {
        val magnitudes = FloatArray(fft.size / 2)
        for (i in 0 until magnitudes.size) {
            val r = fft[i * 2].toFloat()
            val j = fft[i * 2 + 1].toFloat()
            magnitudes[i] = Math.sqrt((r * r + j * j).toDouble()).toFloat()
        }
        _fftData.value = magnitudes
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

    fun setBassBoostEnabled(enabled: Boolean) {
        bassBoost?.enabled = enabled
        _state.value = _state.value.copy(isBassBoostEnabled = enabled)
    }

    fun setBassBoostStrength(strength: Short) {
        bassBoost?.setStrength(strength)
        _state.value = _state.value.copy(bassBoostStrength = strength)
    }

    fun setVirtualizerEnabled(enabled: Boolean) {
        virtualizer?.enabled = enabled
        _state.value = _state.value.copy(isVirtualizerEnabled = enabled)
    }

    fun setVirtualizerStrength(strength: Short) {
        virtualizer?.setStrength(strength)
        _state.value = _state.value.copy(virtualizerStrength = strength)
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
        bassBoost?.release()
        virtualizer?.release()
        visualizer?.release()
        equalizer = null
        loudnessEnhancer = null
        bassBoost = null
        virtualizer = null
        visualizer = null
    }
}
