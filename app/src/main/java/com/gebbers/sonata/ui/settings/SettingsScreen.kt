package com.gebbers.sonata.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val dynamicTheming by viewModel.dynamicTheming.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val fadeDuration by viewModel.fadeDuration.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is SettingsEvent.Toast -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    SettingsHeader("Appearance")
                    SettingsSwitchItem(
                        title = "Dynamic Theming",
                        description = "Extract accent colors from album art",
                        icon = Icons.Default.Palette,
                        checked = dynamicTheming,
                        onCheckedChange = { viewModel.setDynamicTheming(it) }
                    )
                    SettingsSelectItem(
                        title = "Dark Mode",
                        description = "Current: $darkMode",
                        icon = Icons.Default.Brightness4,
                        onClick = { /* Implement dialog */ }
                    )
                }

                item {
                    SettingsHeader("Audio")
                    SettingsSliderItem(
                        title = "Crossfade Duration",
                        description = "${fadeDuration / 1000} seconds",
                        icon = Icons.Default.GraphicEq,
                        value = fadeDuration.toFloat(),
                        onValueChange = { viewModel.setFadeDuration(it.toLong()) },
                        valueRange = 0f..5000f
                    )
                }

                item {
                    SettingsHeader("Library Tools")
                    SettingsClickItem(
                        title = "Rescan Library",
                        description = "Search device for new music files",
                        icon = Icons.Default.Refresh,
                        onClick = { viewModel.rescanLibrary() },
                        loading = isScanning
                    )

                    val excludedFolders by viewModel.excludedFolders.collectAsState()
                    val scannedFolders by viewModel.scannedFolders.collectAsState()
                    val folderPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.OpenDocumentTree()
                    ) { uri ->
                        uri?.let {
                            val path = it.path ?: ""
                            viewModel.addScannedFolder(path, it.lastPathSegment ?: "Folder")
                        }
                    }

                    SettingsClickItem(
                        title = "Add Music Folder",
                        description = "Only scan specific directories",
                        icon = Icons.Default.CreateNewFolder,
                        onClick = { folderPickerLauncher.launch(null) }
                    )

                    if (scannedFolders.isNotEmpty()) {
                        SettingsHeader("Scanned Folders")
                        scannedFolders.forEach { folder ->
                            ListItem(
                                headlineContent = { Text(folder.displayName) },
                                supportingContent = { Text(folder.path) },
                                leadingContent = { Icon(Icons.Default.Folder, contentDescription = null) },
                                trailingContent = {
                                    IconButton(onClick = { viewModel.removeScannedFolder(folder.path) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                                    }
                                }
                            )
                        }
                    }

                    if (excludedFolders.isNotEmpty()) {
                        SettingsHeader("Excluded Folders")
                        excludedFolders.forEach { path ->
                            ListItem(
                                headlineContent = { Text(path.substringAfterLast('/')) },
                                supportingContent = { Text(path) },
                                leadingContent = { Icon(Icons.Default.FolderOff, contentDescription = null) },
                                trailingContent = {
                                    IconButton(onClick = { viewModel.includeFolder(path) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                                    }
                                }
                            )
                        }
                    }
                }

                item {
                    SettingsHeader("About")
                    ListItem(
                        headlineContent = { Text("Sonata Music Player") },
                        supportingContent = { Text("Version 1.0.0") },
                        leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
                    )
                }
            }

            if (isScanning) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Text("Scanning library...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsSwitchItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        },
        modifier = Modifier.clickable { onCheckedChange(!checked) }
    )
}

@Composable
fun SettingsSelectItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        leadingContent = { Icon(icon, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
fun SettingsClickItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    loading: Boolean = false
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(description) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        },
        modifier = Modifier.clickable(enabled = !loading, onClick = onClick)
    )
}

@Composable
fun SettingsSliderItem(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = {
            Column {
                Text(description)
                Slider(
                    value = value,
                    onValueChange = onValueChange,
                    valueRange = valueRange
                )
            }
        },
        leadingContent = { Icon(icon, contentDescription = null) }
    )
}
