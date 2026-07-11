package com.gebbers.sonata.ui.equalizer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun VisualizerView(
    magnitudes: FloatArray,
    modifier: Modifier = Modifier
) {
    val color = MaterialTheme.colorScheme.primary
    
    Canvas(modifier = modifier.fillMaxWidth().height(100.dp)) {
        val width = size.width
        val height = size.height
        val barWidth = width / magnitudes.size.coerceAtLeast(1)
        
        for (i in magnitudes.indices) {
            val magnitude = magnitudes[i]
            val barHeight = (magnitude / 100f) * height // Normalized height
            
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(color, color.copy(alpha = 0.3f))
                ),
                topLeft = androidx.compose.ui.geometry.Offset(i * barWidth, height - barHeight),
                size = androidx.compose.ui.geometry.Size(barWidth - 2.dp.toPx(), barHeight)
            )
        }
    }
}
