package com.gebbers.sonata

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.gebbers.sonata.ui.equalizer.EqualizerManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

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
            
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                    // Start fade-in for the new track
                    fadeIn(player)
                }
            }
        })

        mediaSession = MediaSession.Builder(this, player).build()
    }

    private fun fadeIn(player: Player) {
        val fadeInDuration = 1000L // 1 second
        val steps = 20
        val interval = fadeInDuration / steps
        val volumeStep = 1.0f / steps
        
        var currentVolume = 0f
        player.volume = currentVolume
        
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        
        // Poll for end of track to trigger fade out
        val fadeOutCheck = object : Runnable {
            override fun run() {
                val remaining = player.duration - player.currentPosition
                if (remaining in 1..2000L) { // 2 seconds before end
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
        val fadeOutDuration = 2000L // 2 seconds
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
