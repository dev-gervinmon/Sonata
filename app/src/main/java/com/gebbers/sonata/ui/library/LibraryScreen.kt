package com.gebbers.sonata.ui.library

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.gebbers.sonata.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onSongClick: (com.gebbers.sonata.domain.model.Song) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sonata Music") }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is LibraryUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is LibraryUiState.Success -> {
                    SongList(
                        songs = state.songs,
                        onSongClick = onSongClick
                    )
                }
                is LibraryUiState.Empty -> {
                    Text(
                        text = "No music found on device",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is LibraryUiState.PermissionDenied -> {
                    Text(
                        text = "Permission denied. Please grant storage access.",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is LibraryUiState.Error -> {
                    Text(
                        text = "Error: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
