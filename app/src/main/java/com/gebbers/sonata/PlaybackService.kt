package com.gebbers.sonata

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.core.content.ContextCompat
import com.gebbers.sonata.ui.equalizer.EqualizerManager
import com.gebbers.sonata.ui.widget.MusicWidget
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private val widgetReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val player = mediaSession?.player ?: return
            when (intent?.action) {
                "com.gebbers.sonata.ACTION_TOGGLE_PLAY_PAUSE" -> {
                    if (player.isPlaying) player.pause() else player.play()
                }
                "com.gebbers.sonata.ACTION_SKIP_NEXT" -> player.seekToNext()
                "com.gebbers.sonata.ACTION_SKIP_PREVIOUS" -> player.seekToPrevious()
            }
        }
    }

    @Inject
    lateinit var equalizerManager: EqualizerManager

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(
                androidx.media3.common.AudioAttributes.Builder()
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .build(),
                true
            )
            .build()
        
        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                equalizerManager.init(audioSessionId)
            }
            
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateWidget()
            }

            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                updateWidget()
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                    fadeIn(player)
                }
            }
        })

        mediaSession = MediaSession.Builder(this, player).build()

        val filter = IntentFilter().apply {
            addAction("com.gebbers.sonata.ACTION_TOGGLE_PLAY_PAUSE")
            addAction("com.gebbers.sonata.ACTION_SKIP_NEXT")
            addAction("com.gebbers.sonata.ACTION_SKIP_PREVIOUS")
        }
        ContextCompat.registerReceiver(
            this,
            widgetReceiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    private fun updateWidget() {
        val player = mediaSession?.player ?: return
        val currentItem = player.currentMediaItem
        val title = currentItem?.mediaMetadata?.title?.toString() ?: "Not Playing"
        val artist = currentItem?.mediaMetadata?.artist?.toString() ?: "Sonata Music"
        val isPlaying = player.isPlaying

        serviceScope.launch {
            val glanceId = GlanceAppWidgetManager(this@PlaybackService)
                .getGlanceIds(MusicWidget::class.java).firstOrNull()
            
            if (glanceId != null) {
                updateAppWidgetState(this@PlaybackService, glanceId) { prefs ->
                    prefs[stringPreferencesKey("title")] = title
                    prefs[stringPreferencesKey("artist")] = artist
                    prefs[booleanPreferencesKey("is_playing")] = isPlaying
                }
                MusicWidget().update(this@PlaybackService, glanceId)
            }
        }
    }

    private fun fadeIn(player: Player) {
        val fadeInDuration = 1000L
        val steps = 20
        val interval = fadeInDuration / steps
        val volumeStep = 1.0f / steps
        var currentVolume = 0f
        player.volume = currentVolume
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val fadeOutCheck = object : Runnable {
            override fun run() {
                val remaining = player.duration - player.currentPosition
                if (remaining in 1..2000L) {
                    fadeOut(player)
                } else {
                    handler.postDelayed(this, 500)
                }
            }
        }
        handler.post(fadeOutCheck)
        val runnable = object : Runnable {
            override fun run() {
                if (currentVolume < 1.0f) {
                    currentVolume += volumeStep
                    player.volume = currentVolume
                    handler.postDelayed(this, interval)
                }
            }
        }
        handler.post(runnable)
    }

    private fun fadeOut(player: Player) {
        val fadeOutDuration = 2000L
        val steps = 20
        val interval = fadeOutDuration / steps
        val volumeStep = player.volume / steps
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (player.volume > 0.05f) {
                    player.volume -= volumeStep
                    handler.postDelayed(this, interval)
                }
            }
        }
        handler.post(runnable)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        unregisterReceiver(widgetReceiver)
        equalizerManager.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player != null) {
            if (!player.playWhenReady || player.mediaItemCount == 0) {
                stopSelf()
            }
        }
    }
}
