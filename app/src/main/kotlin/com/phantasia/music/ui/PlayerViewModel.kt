package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.phantasia.music.network.MusicRepository
import com.phantasia.music.network.TrackModel
import com.phantasia.music.player.PlayerStateHolder
import com.phantasia.music.player.QueueManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repo: MusicRepository, private val sh: PlayerStateHolder,
    private val player: ExoPlayer, private val queue: QueueManager,
) : ViewModel() {
    val uiState: StateFlow<PlayerUiState> = sh.uiState

    fun onEvent(e: PlayerUiEvent) {
        when (e) {
            is PlayerUiEvent.Play          -> player.play()
            is PlayerUiEvent.Pause         -> player.pause()
            is PlayerUiEvent.SkipNext      -> { queue.skipToNext(); playCurrentTrack() }
            is PlayerUiEvent.SkipPrev      -> { queue.skipToPrev(); playCurrentTrack() }
            is PlayerUiEvent.Seek          -> player.seekTo(e.positionMs)
            is PlayerUiEvent.ToggleShuffle -> queue.toggleShuffle()
            is PlayerUiEvent.CycleRepeat   -> queue.cycleRepeat()
            is PlayerUiEvent.PlayTrack     -> loadAndPlay(e.track)
            else                           -> Unit
        }
    }

    private fun loadAndPlay(t: TrackModel) = viewModelScope.launch {
        val url = repo.getStream(t.videoId)?.streamUrl ?: return@launch; sh.loadTrack(t, url)
    }

    private fun playCurrentTrack() { queue.state.value.currentTrack?.let { loadAndPlay(it) } }
}
