package com.gebbers.sonata.ui.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback

class PlayPauseActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        // Send intent to PlaybackService
        val intent = Intent("com.gebbers.sonata.ACTION_TOGGLE_PLAY_PAUSE")
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }
}

class SkipNextActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent("com.gebbers.sonata.ACTION_SKIP_NEXT")
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }
}

class SkipPreviousActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val intent = Intent("com.gebbers.sonata.ACTION_SKIP_PREVIOUS")
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }
}
