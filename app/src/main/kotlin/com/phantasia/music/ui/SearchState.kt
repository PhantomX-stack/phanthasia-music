package com.phantasia.music.ui

import com.phantasia.music.network.SearchResultModel

sealed interface SearchUiState {
    object Idle; object Loading
    data class Results(val items: List<SearchResultModel>) : SearchUiState
    data class Error(val message: String)                  : SearchUiState
}
sealed interface SearchUiEvent {
    data class QueryChanged(val query: String)    : SearchUiEvent
    data class TrackSelected(val videoId: String) : SearchUiEvent
    object ClearHistory                           : SearchUiEvent
}
