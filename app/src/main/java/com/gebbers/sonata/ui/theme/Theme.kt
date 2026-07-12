package com.gebbers.sonata.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun SonataTheme(
    seedColor: Color? = null,
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        seedColor != null -> {
            // Manual seed color theming
            if (darkTheme) {
                darkColorScheme(
                    primary = seedColor,
                    secondary = seedColor.copy(alpha = 0.8f),
                    tertiary = seedColor.copy(alpha = 0.6f)
                )
            } else {
                lightColorScheme(
                    primary = seedColor,
                    secondary = seedColor.copy(alpha = 0.8f),
                    tertiary = seedColor.copy(alpha = 0.6f)
                )
            }
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> darkColorScheme()
        else -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
