package com.gebbers.sonata.ui.equalizer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
                title = { Text("Equalizer") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Switch(
                        checked = state.isEnabled,
                        onCheckedChange = { viewModel.setEnabled(it) }
                    )
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
                Text("Bands", style = MaterialTheme.typography.titleMedium)
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
        }
    }
}
