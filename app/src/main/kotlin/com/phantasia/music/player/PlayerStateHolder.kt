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

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(s: Int) { sync() }
            override fun onIsPlayingChanged(playing: Boolean) { sync(); if (playing) startProgress() else progressJob?.cancel() }
        })
    }

    fun loadTrack(track: TrackModel, url: String) {
        _state.value = PlayerUiState.Loading
        player.setMediaItem(MediaItem.fromUri(url)); player.prepare(); player.play()
        _state.value = PlayerUiState.Playing(track, 0L, player.duration.takeIf { it > 0 } ?: 0L, true, false, RepeatMode.NONE)
    }

    private fun sync() {
        val c = _state.value
        if (c is PlayerUiState.Playing) _state.value = c.copy(
            isPlaying = player.isPlaying, durationMs = player.duration.takeIf { it > 0 } ?: c.durationMs)
    }

    private fun startProgress() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                (_state.value as? PlayerUiState.Playing)?.let { _state.value = it.copy(positionMs = player.currentPosition) }
                delay(500)
            }
        }
    }
}
