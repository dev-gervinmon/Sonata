package com.gebbers.sonata

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.gebbers.sonata.ui.MainScreen
import com.gebbers.sonata.ui.equalizer.EqualizerViewModel
import com.gebbers.sonata.ui.library.LibraryViewModel
import com.gebbers.sonata.ui.settings.SettingsViewModel
import com.gebbers.sonata.ui.theme.SonataTheme
import com.gebbers.sonata.ui.theme.ThemeViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: LibraryViewModel by viewModels()
    private val equalizerViewModel: EqualizerViewModel by viewModels()
    private val themeViewModel: ThemeViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val isGranted = result.values.all { it }
        viewModel.onPermissionResult(isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        lifecycleScope.launch {
            viewModel.currentSong.collect { song ->
                themeViewModel.updateColorFromImage(song?.albumArtUri)
            }
        }

        setContent {
            val seedColor by themeViewModel.seedColor.collectAsState()

            SonataTheme(seedColor = seedColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        libraryViewModel = viewModel,
                        equalizerViewModel = equalizerViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }

        checkPermissions()
    }

    private fun checkPermissions() {
        val permissions = mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_AUDIO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
            add(Manifest.permission.RECORD_AUDIO)
        }

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            viewModel.onPermissionResult(true)
        } else {
            permissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }
}
