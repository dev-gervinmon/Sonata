package com.gebbers.sonata.ui.playback

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.gebbers.sonata.PlaybackService
import com.gebbers.sonata.domain.model.Song
import com.gebbers.sonata.data.mapper.toMediaItem
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private val controller: MediaController?
        get() = if (controllerFuture?.isDone == true) controllerFuture?.get() else null

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private var currentPlaylist: List<Song> = emptyList()

    fun connect() {
        if (controllerFuture != null) return

        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture?.addListener({
            val controller = controller ?: return@addListener
            controller.addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                }

                override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                    mediaItem?.let { item ->
                        _currentSong.value = currentPlaylist.find { it.mediaStoreId.toString() == item.mediaId }
                    }
                }
            })
        }, MoreExecutors.directExecutor())
    }

    fun playSong(song: Song, allSongs: List<Song>) {
        val controller = controller ?: return
        
        currentPlaylist = allSongs
        val mediaItems = allSongs.map { it.toMediaItem() }
        val startIndex = allSongs.indexOf(song).coerceAtLeast(0)
        
        controller.setMediaItems(mediaItems, startIndex, 0L)
        controller.prepare()
        controller.play()
        _currentSong.value = song
    }

    fun togglePlayPause() {
        val controller = controller ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
        }
    }

    fun skipToNext() {
        controller?.seekToNext()
    }

    fun skipToPrevious() {
        controller?.seekToPrevious()
    }

    fun release() {
        controllerFuture?.let {
            MediaController.releaseFuture(it)
            controllerFuture = null
        }
    }
}
