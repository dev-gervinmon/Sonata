package com.gebbers.sonata.ui.playback

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LyricsView(
    lyrics: String?,
    currentPosition: Long,
    modifier: Modifier = Modifier
) {
    val parsedLyrics = remember(lyrics) { parseLyrics(lyrics) }
    
    if (parsedLyrics.isEmpty()) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "No lyrics available",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val lazyListState = rememberLazyListState()
    
    val currentLineIndex = remember(parsedLyrics, currentPosition) {
        val index = parsedLyrics.findLast { it.time <= currentPosition }?.let { 
            parsedLyrics.indexOf(it) 
        } ?: 0
        index
    }

    LaunchedEffect(currentLineIndex) {
        if (parsedLyrics.any { it.time > 0 }) {
            lazyListState.animateScrollToItem(currentLineIndex, scrollOffset = -200)
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 120.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(parsedLyrics) { index, line ->
            val isCurrent = index == currentLineIndex && line.time > 0
            Text(
                text = line.text,
                style = if (isCurrent) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleMedium,
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 24.dp)
                    .animateContentSize(),
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
