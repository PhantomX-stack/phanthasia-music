package com.phantasia.music.ui

import androidx.lifecycle.SavedStateHandle
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
    private val dao:  SearchHistoryDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialQuery: String = savedStateHandle.get<String>("q") ?: ""

    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _state.asStateFlow()

    private val _query = MutableStateFlow(initialQuery)
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filter = MutableStateFlow("All")
    val filter: StateFlow<String> = _filter.asStateFlow()

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

        // Live suggestions (150ms debounce)
        _query.debounce(150)
            .distinctUntilChanged()
            .filter { it.length >= 2 }
            .flatMapLatest { q ->
                flow {
                    emit(runCatching { repo.getSearchSuggestions(q) }.getOrDefault(emptyList()))
                }
            }
            .onEach { _suggestions.value = it }
            .launchIn(viewModelScope)

        // Live search results (300ms debounce)
        combine(_query.debounce(300), _filter) { q, f -> q to f }
            .distinctUntilChanged()
            .filter { it.first.isNotBlank() }
            .flatMapLatest { (q, f) ->
                flow {
                    emit(SearchUiState.Loading)
                    val filterParam = if (f == "All") null else f.lowercase()
                    emit(
                        runCatching { SearchUiState.Results(repo.search(q, filterParam)) }
                            .getOrElse { SearchUiState.Error(it.message ?: "Search failed") }
                    )
                }
            }
            .onEach { _state.value = it }
            .launchIn(viewModelScope)

        if (initialQuery.isNotBlank()) {
            executeSearch(initialQuery)
        }
    }

    fun executeSearch(q: String) {
        _query.value = q
        viewModelScope.launch {
            _state.value = SearchUiState.Loading
            try {
                val f = _filter.value
                val filterParam = if (f == "All") null else f.lowercase()
                val results = repo.search(q, filterParam)
                _state.value = SearchUiState.Results(results)
                dao.insert(SearchHistoryEntity(query = q.trim(), type = SearchHistoryType.QUERY))
            } catch (e: Exception) {
                _state.value = SearchUiState.Error(e.message ?: "Search failed")
            }
        }
    }

    fun onEvent(e: SearchUiEvent) {
        when (e) {
            is SearchUiEvent.QueryChanged  -> _query.value = e.query
            is SearchUiEvent.SetFilter     -> {
                _filter.value = e.filter
                val q = _query.value.trim()
                if (q.isNotBlank()) {
                    executeSearch(q)
                }
            }
            is SearchUiEvent.SearchSubmitted -> {
                val q = _query.value.trim()
                if (q.isNotBlank()) {
                    executeSearch(q)
                }
            }
            is SearchUiEvent.ClearHistory  -> viewModelScope.launch { dao.clearAll() }
            is SearchUiEvent.TrackSelected -> viewModelScope.launch {
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

    fun searchFromHistory(query: String) {
        executeSearch(query)
    }

    fun deleteHistoryItem(item: SearchHistoryEntity) {
        viewModelScope.launch { dao.deleteById(item.id) }
    }
}
