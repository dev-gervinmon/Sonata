package com.gebbers.sonata.ui.equalizer

import android.content.Context
import android.media.AudioManager
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Visualizer
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class EqualizerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var equalizer: Equalizer? = null
    private var loudnessEnhancer: LoudnessEnhancer? = null
    private var bassBoost: BassBoost? = null
    
    // Legacy Virtualizer for Android < 15
    private var legacyVirtualizer: android.media.audiofx.Virtualizer? = null
    private var visualizer: Visualizer? = null
    
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _state = MutableStateFlow(EqualizerState())
    val state = _state.asStateFlow()

    private val _fftData = MutableStateFlow(FloatArray(0))
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
            
            // New Docs: Use Spatializer instead of Virtualizer for Android 15+
            // We initialize legacy Virtualizer only for older versions
            if (Build.VERSION.SDK_INT < 35) {
                initLegacyVirtualizer(audioSessionId)
            }
            
            visualizer = try {
                Visualizer(audioSessionId).apply {
                    captureSize = Visualizer.getCaptureSizeRange()[1]
                    setDataCaptureListener(object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(v: Visualizer?, waveform: ByteArray?, samplingRate: Int) {}
                        override fun onFftDataCapture(v: Visualizer?, fft: ByteArray?, samplingRate: Int) {
                            fft?.let { processFft(it) }
                        }
                    }, Visualizer.getMaxCaptureRate() / 2, false, true)
                    enabled = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }

            loadEqualizerSettings()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initLegacyVirtualizer(audioSessionId: Int) {
        try {
            @Suppress("DEPRECATION")
            legacyVirtualizer = android.media.audiofx.Virtualizer(0, audioSessionId).apply {
                enabled = _state.value.isVirtualizerEnabled
                setStrength(_state.value.virtualizerStrength)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun processFft(fft: ByteArray) {
        val magnitudes = FloatArray(fft.size / 2)
        for (i in magnitudes.indices) {
            val r = fft[i * 2].toFloat()
            val j = fft[i * 2 + 1].toFloat()
            magnitudes[i] = sqrt((r * r + j * j).toDouble()).toFloat()
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
        _state.value = _state.value.copy(isVirtualizerEnabled = enabled)
        
        if (Build.VERSION.SDK_INT < 35) {
            @Suppress("DEPRECATION")
            legacyVirtualizer?.enabled = enabled
        }
        // On Android 15+, spatialization is handled in PlaybackService by observing this state
    }

    fun setVirtualizerStrength(strength: Short) {
        _state.value = _state.value.copy(virtualizerStrength = strength)
        
        if (Build.VERSION.SDK_INT < 35) {
            @Suppress("DEPRECATION")
            legacyVirtualizer?.setStrength(strength)
        }
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
        @Suppress("DEPRECATION")
        legacyVirtualizer?.release()
        visualizer?.release()
        equalizer = null
        loudnessEnhancer = null
        bassBoost = null
        legacyVirtualizer = null
        visualizer = null
    }
}
