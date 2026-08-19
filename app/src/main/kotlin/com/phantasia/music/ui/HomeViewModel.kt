package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantasia.music.network.HomeSection
import com.phantasia.music.network.MusicRepository
import com.phantasia.music.network.TrackModel
import com.phantasia.music.storage.SongDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading:      Boolean           = true,
    val isRefreshing:   Boolean           = false,
    val homeSections:   List<HomeSection> = emptyList(),
    val quickPicks:     List<TrackModel>  = emptyList(),
    val recentlyPlayed: List<TrackModel>  = emptyList(),
    val selectedGenre:  String            = "All",
    val genreTracks:    List<TrackModel>  = emptyList(),
    val isGenreLoading: Boolean           = false,
    val error:          String?           = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo:    MusicRepository,
    private val songDao: SongDao
) : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        loadFeed()
        viewModelScope.launch {
            songDao.getAll().collect { songs ->
                _state.update { s -> s.copy(
                    recentlyPlayed = songs.take(20).map { song ->
                        TrackModel(song.videoId, song.title, song.artistName,
                            song.albumTitle, song.artworkUrl, song.durationSeconds)
                    }
                )}
            }
        }
    }

    fun loadFeed() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val sections = repo.getHomeSections()
                val picks    = sections.flatMap { it.items }.shuffled().take(12)
                _state.update { it.copy(isLoading = false, homeSections = sections, quickPicks = picks) }
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false,
                    error = "Could not load recommendations: ${e.message}") }
            }
        }
    }

    fun selectGenre(genre: String) {
        if (_state.value.selectedGenre == genre) return
        _state.update { it.copy(selectedGenre = genre) }
        if (genre == "All") {
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isGenreLoading = true) }
            runCatching {
                val tracks = repo.getGenreTracks(genre)
                _state.update { it.copy(genreTracks = tracks, isGenreLoading = false) }
            }.onFailure {
                _state.update { it.copy(isGenreLoading = false) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isRefreshing = true) }
            runCatching { repo.getHomeSections() }.onSuccess { sections ->
                _state.update { it.copy(isRefreshing = false, homeSections = sections,
                    quickPicks = sections.flatMap { it.items }.shuffled().take(12)) }
            }.onFailure { _state.update { it.copy(isRefreshing = false) } }
        }
    }
}
