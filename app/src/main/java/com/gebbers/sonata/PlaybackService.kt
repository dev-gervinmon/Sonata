package com.gebbers.sonata

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.gebbers.sonata.data.mapper.toMediaItem
import com.gebbers.sonata.data.preferences.PreferenceManager
import com.gebbers.sonata.domain.repository.MusicRepository
import com.gebbers.sonata.ui.equalizer.EqualizerManager
import com.gebbers.sonata.ui.widget.MusicWidget
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaLibraryService() {
    private var mediaSession: MediaLibrarySession? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    @Inject
    lateinit var musicRepository: MusicRepository

    @Inject
    lateinit var equalizerManager: EqualizerManager

    @Inject
    lateinit var preferenceManager: PreferenceManager

    private var currentFadeDuration = 1000L
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private var fadeOutRunnable: Runnable? = null
    private var volumeAnimator: android.animation.ValueAnimator? = null

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

    private val librarySessionCallback = object : MediaLibrarySession.Callback {
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            val rootItem = MediaItem.Builder()
                .setMediaId("sonata_root")
                .setMediaMetadata(
                    androidx.media3.common.MediaMetadata.Builder()
                        .setIsBrowsable(true)
                        .setIsPlayable(false)
                        .setTitle("Library")
                        .build()
                )
                .build()
            return Futures.immediateFuture(LibraryResult.ofItem(rootItem, params))
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return serviceScope.future {
                val songs = musicRepository.getAllSongs().first()
                val mediaItems = songs.map { it.toMediaItem() }
                LibraryResult.ofItemList(mediaItems, params)
            }
        }
    }

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

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateWidget()
                // Record playback for smart playlists
                mediaItem?.mediaId?.toLongOrNull()?.let { id ->
                    serviceScope.launch { musicRepository.recordSongPlayback(id) }
                }
                
                // Restart fade monitoring for new track
                resetVolume(player)
                startFadeOutMonitor(player)
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                    fadeIn(player)
                } else if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    resetVolume(player)
                }
            }
        })

        mediaSession = MediaLibrarySession.Builder(this, player, librarySessionCallback).build()

        serviceScope.launch {
            preferenceManager.fadeDuration.collect {
                currentFadeDuration = it
            }
        }

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
        if (currentFadeDuration <= 0) {
            player.volume = 1.0f
            return
        }
        animateVolume(player, 0f, 1.0f, currentFadeDuration)
    }

    private fun fadeOut(player: Player) {
        if (currentFadeDuration <= 0) return
        animateVolume(player, player.volume, 0f, currentFadeDuration)
    }

    private fun resetVolume(player: Player) {
        volumeAnimator?.cancel()
        player.volume = 1.0f
    }

    private fun animateVolume(player: Player, from: Float, to: Float, duration: Long) {
        volumeAnimator?.cancel()
        volumeAnimator = android.animation.ValueAnimator.ofFloat(from, to).apply {
            this.duration = duration
            addUpdateListener { 
                player.volume = it.animatedValue as Float
            }
            start()
        }
    }

    private fun startFadeOutMonitor(player: Player) {
        fadeOutRunnable?.let { handler.removeCallbacks(it) }
        fadeOutRunnable = object : Runnable {
            override fun run() {
                if (!player.isPlaying) {
                    handler.postDelayed(this, 1000)
                    return
                }
                
                val remaining = player.duration - player.currentPosition
                if (player.duration > 0 && remaining in 1..currentFadeDuration) {
                    fadeOut(player)
                } else {
                    handler.postDelayed(this, 500)
                }
            }
        }
        handler.post(fadeOutRunnable!!)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
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
