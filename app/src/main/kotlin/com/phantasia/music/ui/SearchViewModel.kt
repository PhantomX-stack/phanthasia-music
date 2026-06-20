package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantasia.music.network.MusicRepository
import com.phantasia.music.storage.SearchHistoryDao
import com.phantasia.music.storage.SearchHistoryEntity
import com.phantasia.music.storage.SearchHistoryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: MusicRepository,
    private val dao: SearchHistoryDao
) : ViewModel() {
    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _state.asStateFlow()
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    val history = dao.getRecent(10)

    init {
        _query.debounce(400).distinctUntilChanged().filter { it.length > 1 }
            .onEach { q ->
                _state.value = SearchUiState.Loading
                _state.value = runCatching { SearchUiState.Results(repo.search(q)) }
                    .getOrElse { SearchUiState.Error(it.message ?: "Error") }
            }.launchIn(viewModelScope)
    }

    fun onEvent(e: SearchUiEvent) = when (e) {
        is SearchUiEvent.QueryChanged  -> _query.value = e.query
        is SearchUiEvent.ClearHistory  -> viewModelScope.launch { dao.clearAll() }.let {}
        is SearchUiEvent.TrackSelected -> viewModelScope.launch {
            dao.insert(SearchHistoryEntity(query = e.videoId, type = SearchHistoryType.TRACK)) }.let {}
    }
}
