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

    // !! PERSISTED IN ROOM — survives app close/reopen
    // getRecent() returns a Flow backed by Room DB — always up to date
    val history: StateFlow<List<SearchHistoryEntity>> =
        dao.getRecent(20).stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    init {
        // Reset to Idle when query is blank
        _query.filter { it.isBlank() }
            .onEach { _state.value = SearchUiState.Idle; _suggestions.value = emptyList() }
            .launchIn(viewModelScope)

        // Live suggestions (200ms debounce — fast)
        _query.debounce(200)
            .distinctUntilChanged()
            .filter { it.length >= 2 }
            .flatMapLatest { q ->
                flow {
                    emit(runCatching { repo.getSearchSuggestions(q) }.getOrDefault(emptyList()))
                }
            }
            .onEach { _suggestions.value = it }
            .launchIn(viewModelScope)

        // Actual search (320ms debounce — avoids API spam)
        _query.debounce(320)
            .distinctUntilChanged()
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
                // Save the actual text query (not videoId) so history shows readable text
                val q = _query.value.trim()
                if (q.isNotBlank()) {
                    dao.insert(SearchHistoryEntity(
                        query = q,
                        type  = SearchHistoryType.TRACK
                    ))
                }
            }
        }
    }

    fun playRandomFromHistory(): SearchHistoryEntity? {
        return history.value.randomOrNull()
    }

    fun searchFromHistory(query: String) {
        _query.value = query
    }

    fun deleteHistoryItem(item: SearchHistoryEntity) {
        viewModelScope.launch { dao.deleteById(item.id) }
    }
}
