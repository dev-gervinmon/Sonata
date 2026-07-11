package com.gebbers.sonata.ui.equalizer

data class EqualizerState(
    val isEnabled: Boolean = false,
    val bands: List<EqualizerBand> = emptyList(),
    val presets: List<String> = emptyList(),
    val currentPreset: Int = -1,
    val isLoudnessEnabled: Boolean = false,
    val loudnessGain: Int = 0, // in mB (millibels)
    val isBassBoostEnabled: Boolean = false,
    val bassBoostStrength: Short = 0,
    val isVirtualizerEnabled: Boolean = false,
    val virtualizerStrength: Short = 0
)

data class EqualizerBand(
    val index: Int,
    val centerFrequency: Int, // in Hz
    val minLevel: Short,
    val maxLevel: Short,
    val currentLevel: Short
)
