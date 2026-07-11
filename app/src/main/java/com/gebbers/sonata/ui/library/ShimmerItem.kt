package com.gebbers.sonata.ui.library

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerSongItem() {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f),
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    ListItem(
        headlineContent = {
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(20.dp).background(brush))
        },
        supportingContent = {
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(14.dp).background(brush))
        },
        leadingContent = {
            Box(modifier = Modifier.size(56.dp).clip(MaterialTheme.shapes.small).background(brush))
        },
        trailingContent = {
            Box(modifier = Modifier.size(24.dp).background(brush))
        }
    )
}

@Composable
fun ShimmerList() {
    Column {
        repeat(10) {
            ShimmerSongItem()
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
