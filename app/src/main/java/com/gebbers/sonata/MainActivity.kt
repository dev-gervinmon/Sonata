package com.gebbers.sonata

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
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
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onPermissionResult(isGranted)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                viewModel.onPermissionResult(true)
            }
            else -> {
                permissionLauncher.launch(permission)
            }
        }
    }
}
