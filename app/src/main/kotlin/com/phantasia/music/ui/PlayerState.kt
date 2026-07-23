package com.phantasia.music.ui

import com.phantasia.music.network.TrackModel
import com.phantasia.music.player.RepeatMode

sealed interface PlayerUiState {
    object Idle    : PlayerUiState
    object Loading : PlayerUiState
    data class Playing(
        val track: TrackModel, val positionMs: Long, val durationMs: Long,
        val isPlaying: Boolean, val shuffleEnabled: Boolean, val repeatMode: RepeatMode,
    ) : PlayerUiState
}

sealed interface PlayerUiEvent {
    object Play : PlayerUiEvent
    object Pause : PlayerUiEvent
    object SkipNext : PlayerUiEvent
    object SkipPrev : PlayerUiEvent
    object ToggleShuffle : PlayerUiEvent
    object CycleRepeat : PlayerUiEvent
    object ToggleFavourite : PlayerUiEvent
    data class Seek(val positionMs: Long)       : PlayerUiEvent
    data class PlayTrack(val track: TrackModel) : PlayerUiEvent
}
