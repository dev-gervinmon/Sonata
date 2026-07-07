package com.gebbers.sonata.ui.equalizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerScreen(
    viewModel: EqualizerViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Audio Effects") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Equalizer", style = MaterialTheme.typography.titleLarge)
                    Switch(
                        checked = state.isEnabled,
                        onCheckedChange = { viewModel.setEnabled(it) }
                    )
                }
            }

            if (state.presets.isNotEmpty()) {
                item {
                    Text("Presets", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    ScrollableTabRow(
                        selectedTabIndex = state.currentPreset.coerceAtLeast(0),
                        edgePadding = 0.dp,
                        divider = {}
                    ) {
                        state.presets.forEachIndexed { index, preset ->
                            Tab(
                                selected = state.currentPreset == index,
                                onClick = { viewModel.setPreset(index) },
                                text = { Text(preset) }
                            )
                        }
                    }
                }
            }

            item {
                Text("Frequency Bands", style = MaterialTheme.typography.titleMedium)
            }

            items(state.bands) { band ->
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (band.centerFrequency >= 1000) "${band.centerFrequency / 1000} kHz" else "${band.centerFrequency} Hz",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${band.currentLevel / 100} dB",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Slider(
                        value = band.currentLevel.toFloat(),
                        onValueChange = { viewModel.setBandLevel(band.index, it.toInt().toShort()) },
                        valueRange = band.minLevel.toFloat()..band.maxLevel.toFloat(),
                        enabled = state.isEnabled
                    )
                }
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Volume Normalization", style = MaterialTheme.typography.titleLarge)
                        Text("Balance loudness across tracks", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = state.isLoudnessEnabled,
                        onCheckedChange = { viewModel.setLoudnessEnabled(it) }
                    )
                }
                
                if (state.isLoudnessEnabled) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Target Gain", style = MaterialTheme.typography.bodyMedium)
                        Text("${state.loudnessGain / 100} dB", style = MaterialTheme.typography.bodySmall)
                    }
                    Slider(
                        value = state.loudnessGain.toFloat(),
                        onValueChange = { viewModel.setLoudnessGain(it.toInt()) },
                        valueRange = 0f..2000f, // 0 to 20dB
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
