package com.phantasia.music.ui

import com.phantasia.music.network.TrackModel
import com.phantasia.music.player.RepeatMode

// Which view is shown in the top artwork area of the player
enum class PlayerViewMode {
    THUMBNAIL,   // shows album artwork image
    VIDEO        // shows YouTube video WebView
}

sealed interface PlayerUiState {
    object Idle    : PlayerUiState
    object Loading : PlayerUiState
    data class Playing(
        val track:          TrackModel,
        val positionMs:     Long,
        val durationMs:     Long,
        val isPlaying:      Boolean,
        val shuffleEnabled: Boolean,
        val repeatMode:     RepeatMode,
        val isFavourite:    Boolean         = false,
        val viewMode:       PlayerViewMode  = PlayerViewMode.THUMBNAIL,
        val showLyrics:     Boolean         = false,
        val lyricsLines:    List<com.phantasia.music.ui.LrcLine>   = emptyList()
    ) : PlayerUiState
}

sealed interface PlayerUiEvent {
    object Play              : PlayerUiEvent
    object Pause             : PlayerUiEvent
    object SkipNext          : PlayerUiEvent
    object SkipPrev          : PlayerUiEvent
    object ToggleShuffle     : PlayerUiEvent
    object CycleRepeat       : PlayerUiEvent
    object ToggleFavourite   : PlayerUiEvent
    object ToggleLyrics      : PlayerUiEvent
    object SetViewThumbnail  : PlayerUiEvent   // show artwork
    object SetViewVideo      : PlayerUiEvent   // show video WebView
    data class Seek(val positionMs: Long)       : PlayerUiEvent
    data class PlayTrack(val track: TrackModel) : PlayerUiEvent
}
