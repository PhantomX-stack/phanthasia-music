package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.phantasia.music.network.MusicRepository
import com.phantasia.music.network.TrackModel
import com.phantasia.music.player.PlayerStateHolder
import com.phantasia.music.player.QueueManager
import com.phantasia.music.storage.SongDao
import com.phantasia.music.storage.SongEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repo:    MusicRepository,
    private val sh:      PlayerStateHolder,
    private val player:  ExoPlayer,
    private val queue:   QueueManager,
    private val songDao: SongDao
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = sh.uiState

    fun onEvent(e: PlayerUiEvent) {
        when (e) {
            is PlayerUiEvent.Play            -> player.play()
            is PlayerUiEvent.Pause           -> player.pause()
            is PlayerUiEvent.SkipNext        -> { queue.skipToNext();  playCurrentTrack() }
            is PlayerUiEvent.SkipPrev        -> { queue.skipToPrev();  playCurrentTrack() }
            is PlayerUiEvent.Seek            -> player.seekTo(e.positionMs)
            is PlayerUiEvent.ToggleShuffle   -> queue.toggleShuffle()
            is PlayerUiEvent.CycleRepeat     -> queue.cycleRepeat()
            is PlayerUiEvent.PlayTrack       -> loadAndPlay(e.track)
            is PlayerUiEvent.ToggleFavourite -> toggleFavourite()
        }
    }

    private fun loadAndPlay(track: TrackModel) {
        viewModelScope.launch {
            val stream = repo.getStream(track.videoId) ?: return@launch
            sh.loadTrack(track, stream.streamUrl)
            // Persist to local DB for offline display in Library
            songDao.upsert(
                SongEntity(
                    videoId         = track.videoId,
                    title           = track.title,
                    artistName      = track.artistName,
                    albumTitle      = track.albumTitle,
                    artworkUrl      = track.artworkUrl,
                    durationSeconds = track.durationSeconds
                )
            )
        }
    }

    private fun playCurrentTrack() {
        queue.state.value.currentTrack?.let { loadAndPlay(it) }
    }

    private fun toggleFavourite() {
        val playing = uiState.value as? PlayerUiState.Playing ?: return
        viewModelScope.launch {
            val existing = songDao.getById(playing.track.videoId)
            if (existing != null) {
                songDao.setFavourite(
                    existing.videoId,
                    if (existing.isFavourite == 1) 0 else 1
                )
            }
        }
    }
}
