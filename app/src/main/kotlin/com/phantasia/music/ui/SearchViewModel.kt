package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantasia.music.network.MusicRepository
import com.phantasia.music.storage.SearchHistoryDao
import com.phantasia.music.storage.SearchHistoryEntity
import com.phantasia.music.storage.SearchHistoryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: MusicRepository,
    private val dao:  SearchHistoryDao
) : ViewModel() {

    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _state.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    val history: Flow<List<SearchHistoryEntity>> = dao.getRecent(8)

    init {
        _query
            .onEach { q -> if (q.isBlank()) _state.value = SearchUiState.Idle }
            .debounce(320)
            .filter { it.isNotBlank() }
            .flatMapLatest { q ->
                flow {
                    emit(SearchUiState.Loading)
                    emit(
                        runCatching { SearchUiState.Results(repo.search(q)) }
                            .getOrElse { SearchUiState.Error(it.message ?: "Search failed") }
                    )
                }
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun onEvent(e: SearchUiEvent) {
        when (e) {
            is SearchUiEvent.QueryChanged  -> _query.value = e.query
            is SearchUiEvent.ClearHistory  -> viewModelScope.launch { dao.clearAll() }
            is SearchUiEvent.TrackSelected -> viewModelScope.launch {
                dao.insert(
                    SearchHistoryEntity(query = e.videoId, type = SearchHistoryType.TRACK)
                )
            }
        }
    }
}
