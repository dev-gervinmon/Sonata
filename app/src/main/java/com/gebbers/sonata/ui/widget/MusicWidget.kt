package com.gebbers.sonata.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.*
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.gebbers.sonata.MainActivity
import com.gebbers.sonata.R

class MusicWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
        val prefs = currentState<Preferences>()
        val title = prefs[stringPreferencesKey("title")] ?: "Not Playing"
        val artist = prefs[stringPreferencesKey("artist")] ?: "Sonata Music"
        val isPlaying = prefs[booleanPreferencesKey("is_playing")] ?: false
        
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ImageProvider(R.drawable.glance_widget_background))
                .padding(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier
                        .size(50.dp)
                        .background(ColorProvider(androidx.compose.ui.graphics.Color.DarkGray))
                ) {
                    // Art
                }
                
                Spacer(modifier = GlanceModifier.width(12.dp))
                
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(androidx.compose.ui.graphics.Color.White)
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = artist,
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = ColorProvider(androidx.compose.ui.graphics.Color.LightGray)
                        ),
                        maxLines = 1
                    )
                }
            }
            
            Spacer(modifier = GlanceModifier.height(12.dp))
            
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_skip_previous),
                    contentDescription = "Previous",
                    modifier = GlanceModifier
                        .size(32.dp)
                        .clickable(actionRunCallback<SkipPreviousActionCallback>())
                )
                
                Spacer(modifier = GlanceModifier.width(24.dp))
                
                Image(
                    provider = ImageProvider(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    modifier = GlanceModifier
                        .size(40.dp)
                        .clickable(actionRunCallback<PlayPauseActionCallback>())
                )
                
                Spacer(modifier = GlanceModifier.width(24.dp))
                
                Image(
                    provider = ImageProvider(R.drawable.ic_skip_next),
                    contentDescription = "Next",
                    modifier = GlanceModifier
                        .size(32.dp)
                        .clickable(actionRunCallback<SkipNextActionCallback>())
                )
            }
        }
    }
}

class MusicWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MusicWidget()
}
