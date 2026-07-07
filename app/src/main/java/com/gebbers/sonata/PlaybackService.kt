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
        val player = ExoPlayer.Builder(this).build()
        
        player.addListener(object : Player.Listener {
            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                equalizerManager.init(audioSessionId)
            }
        })

        mediaSession = MediaSession.Builder(this, player).build()
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
