package com.example.playback

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.data.local.SongEntity
import com.example.data.remote.ApiClient
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

object PlaybackManager {
    private const val TAG = "PlaybackManager"

    private var exoPlayer: ExoPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null
    private var appContext: Context? = null

    private val _currentSong = MutableStateFlow<SongEntity?>(null)
    val currentSong: StateFlow<SongEntity?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playlist = MutableStateFlow<List<SongEntity>>(emptyList())
    val playlist: StateFlow<List<SongEntity>> = _playlist.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var currentPlaylistIndex: Int = -1

    fun getPlayer(context: Context): ExoPlayer {
        appContext = context.applicationContext
        return exoPlayer ?: synchronized(this) {
            val player = ExoPlayer.Builder(context.applicationContext).build().apply {
                repeatMode = Player.REPEAT_MODE_OFF
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlayingChanged: Boolean) {
                        _isPlaying.value = isPlayingChanged
                        _duration.value = duration.coerceAtLeast(0)
                        if (isPlayingChanged) {
                            startProgressTicker()
                        } else {
                            stopProgressTicker()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        _duration.value = duration.coerceAtLeast(0)
                        _isLoading.value = playbackState == Player.STATE_BUFFERING

                        if (playbackState == Player.STATE_READY) {
                            _duration.value = duration
                        } else if (playbackState == Player.STATE_ENDED) {
                            playNext()
                        }
                    }

                    override fun onPositionDiscontinuity(
                        oldPosition: Player.PositionInfo,
                        newPosition: Player.PositionInfo,
                        reason: Int
                    ) {
                        _currentPosition.value = newPosition.positionMs
                    }
                })
            }
            exoPlayer = player
            player
        }
    }

    fun playSong(context: Context, song: SongEntity, fullPlaylist: List<SongEntity>) {
        appContext = context.applicationContext
        val player = getPlayer(context)
        _playlist.value = fullPlaylist
        _currentSong.value = song

        currentPlaylistIndex = fullPlaylist.indexOfFirst { it.id == song.id }
        if (currentPlaylistIndex == -1) {
            _playlist.value = fullPlaylist + song
            currentPlaylistIndex = _playlist.value.size - 1
        }

        val mediaUri = if (song.isDownloaded && !song.localPath.isNullOrEmpty() && File(song.localPath).exists()) {
            Log.d(TAG, "Playing downloaded offline song: ${song.title} from path ${song.localPath}")
            Uri.fromFile(File(song.localPath))
        } else {
            val remoteUrl = ApiClient.getStreamUrl(song.id)
            Log.d(TAG, "Playing remote live streamed song: ${song.title} from URL $remoteUrl")
            Uri.parse(remoteUrl)
        }

        val metadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setArtworkUri(song.artworkUrl?.let { Uri.parse(it) })
            .build()

        val mediaItem = MediaItem.Builder()
            .setUri(mediaUri)
            .setMediaId(song.id)
            .setMediaMetadata(metadata)
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        PlaybackService.startMediaService(context)
    }

    fun togglePlayPause(context: Context) {
        val player = exoPlayer ?: return
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
            PlaybackService.startMediaService(context)
        }
    }

    fun seekTo(positionMs: Long) {
        val player = exoPlayer ?: return
        player.seekTo(positionMs)
        _currentPosition.value = positionMs
    }

    fun playNext(context: Context? = null) {
        val ctx = context ?: appContext ?: return
        val list = _playlist.value
        if (list.isEmpty() || currentPlaylistIndex == -1) return

        var nextIndex = currentPlaylistIndex + 1
        if (nextIndex >= list.size) {
            nextIndex = 0
        }

        playSong(ctx, list[nextIndex], list)
    }

    fun playPrevious(context: Context? = null) {
        val ctx = context ?: appContext ?: return
        val list = _playlist.value
        if (list.isEmpty() || currentPlaylistIndex == -1) return

        var prevIndex = currentPlaylistIndex - 1
        if (prevIndex < 0) {
            prevIndex = list.size - 1
        }

        playSong(ctx, list[prevIndex], list)
    }

    private fun startProgressTicker() {
        stopProgressTicker()
        progressJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                exoPlayer?.let {
                    _currentPosition.value = it.currentPosition
                    _duration.value = it.duration.coerceAtLeast(0)
                }
                delay(400)
            }
        }
    }

    private fun stopProgressTicker() {
        progressJob?.cancel()
        progressJob = null
    }

    fun releaseAll() {
        stopProgressTicker()
        exoPlayer?.release()
        exoPlayer = null
        _currentSong.value = null
        _isPlaying.value = false
    }
}
