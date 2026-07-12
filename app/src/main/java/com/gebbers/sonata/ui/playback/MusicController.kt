package com.gebbers.sonata.ui.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.gebbers.sonata.PlaybackService
import com.gebbers.sonata.domain.model.Song
import com.gebbers.sonata.data.mapper.toMediaItem
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class MusicController @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val musicRepository: com.gebbers.sonata.domain.repository.MusicRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private val controller: MediaController?
        get() = mediaController

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _shuffleModeEnabled = MutableStateFlow(false)
    val shuffleModeEnabled = _shuffleModeEnabled.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode = _repeatMode.asStateFlow()

    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _sleepTimerMillisLeft = MutableStateFlow<Long?>(null)
    val sleepTimerMillisLeft = _sleepTimerMillisLeft.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    private val _playbackPitch = MutableStateFlow(1.0f)
    val playbackPitch = _playbackPitch.asStateFlow()

    private var currentPlaylist: List<Song> = emptyList()
    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null

    fun connect() {
        if (controllerFuture != null) return

        val sessionToken = SessionToken(context, ComponentName(context, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                mediaController?.let { controller ->
                    controller.addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying
                            if (isPlaying) {
                                startProgressUpdate()
                            } else {
                                stopProgressUpdate()
                            }
                        }

                        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                            _shuffleModeEnabled.value = shuffleModeEnabled
                        }

                        override fun onRepeatModeChanged(repeatMode: Int) {
                            _repeatMode.value = repeatMode
                        }

                        override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                            mediaItem?.let { item ->
                                val song = currentPlaylist.find { it.mediaStoreId.toString() == item.mediaId }
                                _currentSong.value = song
                                _duration.value = controller.duration.coerceAtLeast(0L)
                                
                                if (song != null) {
                                    scope.launch { musicRepository.recordSongPlayback(song.mediaStoreId) }
                                }
                            }
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            if (playbackState == Player.STATE_READY) {
                                _duration.value = controller.duration.coerceAtLeast(0L)
                            }
                        }
                    })
                    
                    // Initial Sync
                    _isPlaying.value = controller.isPlaying
                    _shuffleModeEnabled.value = controller.shuffleModeEnabled
                    _repeatMode.value = controller.repeatMode
                    if (controller.isPlaying) startProgressUpdate()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                controller?.let {
                    _currentPosition.value = it.currentPosition
                }
                delay(1000.milliseconds)
            }
        }
    }

    private fun stopProgressUpdate() {
        progressJob?.cancel()
    }

    fun playSong(song: Song, allSongs: List<Song>) {
        val controller = mediaController ?: return
        
        scope.launch {
            try {
                // Testing with a very small queue to isolate Binder issues
                val mediaItems = listOf(song.toMediaItem())
                currentPlaylist = listOf(song)
                
                withContext(Dispatchers.Main) {
                    controller.setMediaItems(mediaItems)
                    controller.prepare()
                    controller.play()
                    _currentSong.value = song
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun seekTo(position: Long) {
        controller?.seekTo(position)
    }

    fun seekForward() {
        val controller = controller ?: return
        val newPosition = controller.currentPosition + 10_000L
        controller.seekTo(newPosition.coerceAtMost(controller.duration))
    }

    fun seekBack() {
        val controller = controller ?: return
        val newPosition = controller.currentPosition - 10_000L
        controller.seekTo(newPosition.coerceAtLeast(0L))
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

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        val totalMillis = minutes * 60 * 1000L
        _sleepTimerMillisLeft.value = totalMillis
        
        sleepTimerJob = scope.launch {
            var remaining = totalMillis
            while (remaining > 0) {
                delay(1000.milliseconds)
                remaining -= 1000
                _sleepTimerMillisLeft.value = remaining
            }
            controller?.pause()
            _sleepTimerMillisLeft.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerMillisLeft.value = null
    }

    fun setPlaybackSpeed(speed: Float) {
        val controller = controller ?: return
        _playbackSpeed.value = speed
        controller.setPlaybackSpeed(speed)
    }

    fun setPlaybackPitch(pitch: Float) {
        val controller = controller ?: return
        _playbackPitch.value = pitch
        val currentSpeed = _playbackSpeed.value
        controller.playbackParameters = androidx.media3.common.PlaybackParameters(currentSpeed, pitch)
    }

    fun toggleShuffle() {
        val controller = controller ?: return
        controller.shuffleModeEnabled = !controller.shuffleModeEnabled
    }

    fun toggleRepeatMode() {
        val controller = controller ?: return
        controller.repeatMode = when (controller.repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun release() {
        stopProgressUpdate()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
            controllerFuture = null
            mediaController = null
        }
    }
}
