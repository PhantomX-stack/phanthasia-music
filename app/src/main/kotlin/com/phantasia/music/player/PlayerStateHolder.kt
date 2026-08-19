package com.phantasia.music.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.phantasia.music.network.TrackModel
import com.phantasia.music.ui.PlayerUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerStateHolder @Inject constructor(private val player: ExoPlayer) {
    private val _state = MutableStateFlow<PlayerUiState>(PlayerUiState.Idle)
    val uiState: StateFlow<PlayerUiState> = _state.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null
    var onTrackEnded: (() -> Unit)? = null
    var onPlayerError: ((Throwable) -> Unit)? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(s: Int) {
                sync()
                if (s == Player.STATE_ENDED) {
                    onTrackEnded?.invoke()
                } else if (s == Player.STATE_READY) {
                    startProgress()
                }
            }
            override fun onIsPlayingChanged(playing: Boolean) {
                sync()
                if (playing) startProgress() else progressJob?.cancel()
            }
            override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                error.printStackTrace()
                sync()
                onPlayerError?.invoke(error)
            }
        })
    }

    fun updatePlayingState(newState: PlayerUiState.Playing) {
        _state.value = newState
    }

    fun prepareTrack(track: TrackModel, shuffle: Boolean = false, repeat: RepeatMode = RepeatMode.NONE) {
        val curr = _state.value
        val isFav = (curr as? PlayerUiState.Playing)?.takeIf { it.track.videoId == track.videoId }?.isFavourite ?: false
        _state.value = PlayerUiState.Playing(
            track = track,
            positionMs = 0L,
            durationMs = (track.durationSeconds * 1000L).coerceAtLeast(0L),
            isPlaying = true,
            shuffleEnabled = shuffle,
            repeatMode = repeat,
            isFavourite = isFav
        )
    }

    fun playStream(track: TrackModel, url: String, shuffle: Boolean = false, repeat: RepeatMode = RepeatMode.NONE) {
        player.setMediaItem(MediaItem.fromUri(url))
        player.prepare()
        player.playWhenReady = true
        player.play()
        val curr = _state.value
        val isFav = (curr as? PlayerUiState.Playing)?.takeIf { it.track.videoId == track.videoId }?.isFavourite ?: false
        val showLyrics = (curr as? PlayerUiState.Playing)?.showLyrics ?: false
        val lyrics = (curr as? PlayerUiState.Playing)?.lyricsLines ?: emptyList()
        val viewMode = (curr as? PlayerUiState.Playing)?.viewMode ?: com.phantasia.music.ui.PlayerViewMode.THUMBNAIL
        _state.value = PlayerUiState.Playing(
            track = track,
            positionMs = 0L,
            durationMs = player.duration.takeIf { it > 0 } ?: (track.durationSeconds * 1000L),
            isPlaying = true,
            shuffleEnabled = shuffle,
            repeatMode = repeat,
            isFavourite = isFav,
            viewMode = viewMode,
            showLyrics = showLyrics,
            lyricsLines = lyrics
        )
        startProgress()
    }

    fun loadTrack(track: TrackModel, url: String) {
        playStream(track, url)
    }

    fun stop() {
        progressJob?.cancel()
        player.stop()
        player.clearMediaItems()
        _state.value = PlayerUiState.Idle
    }

    private fun sync() {
        val c = _state.value
        if (c is PlayerUiState.Playing) {
            val totalDur = player.duration.takeIf { it > 0 } ?: c.durationMs
            _state.value = c.copy(
                isPlaying = player.isPlaying || player.playWhenReady,
                durationMs = totalDur,
                positionMs = player.currentPosition
            )
        }
    }

    private fun startProgress() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val currPos = player.currentPosition
                val totalDur = player.duration.takeIf { it > 0 } ?: ((_state.value as? PlayerUiState.Playing)?.durationMs ?: 0L)
                (_state.value as? PlayerUiState.Playing)?.let {
                    _state.value = it.copy(
                        positionMs = currPos,
                        durationMs = totalDur,
                        isPlaying = player.isPlaying || player.playWhenReady
                    )
                }
                delay(250)
            }
        }
    }
}
