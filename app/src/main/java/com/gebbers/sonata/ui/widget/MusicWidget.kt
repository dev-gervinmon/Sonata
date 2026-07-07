package com.gebbers.sonata.ui.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.action.Action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import com.gebbers.sonata.MainActivity
import com.gebbers.sonata.R

class MusicWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    private fun WidgetContent() {
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
                        .size(60.dp)
                        .background(ColorProvider(androidx.compose.ui.graphics.Color.DarkGray))
                ) {
                    // Album art would go here
                }
                
                Spacer(modifier = GlanceModifier.width(12.dp))
                
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Text(
                        text = "Not Playing",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ColorProvider(androidx.compose.ui.graphics.Color.White)
                        ),
                        maxLines = 1
                    )
                    Text(
                        text = "Sonata Music",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = ColorProvider(androidx.compose.ui.graphics.Color.LightGray)
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

class MusicWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MusicWidget()
}
