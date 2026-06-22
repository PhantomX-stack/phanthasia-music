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
    object Play; object Pause; object SkipNext; object SkipPrev
    object ToggleShuffle; object CycleRepeat; object ToggleFavourite
    data class Seek(val positionMs: Long)       : PlayerUiEvent
    data class PlayTrack(val track: TrackModel) : PlayerUiEvent
}
