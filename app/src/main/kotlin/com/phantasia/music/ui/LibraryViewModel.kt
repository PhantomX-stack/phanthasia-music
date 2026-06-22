package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantasia.music.storage.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songDao: SongDao, private val playlistDao: PlaylistDao,
) : ViewModel() {
    val favourites = songDao.getFavourites().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val playlists: Flow<List<PlaylistWithSongs>> = playlistDao.getAll()
    fun toggleFavourite(s: SongEntity) = viewModelScope.launch { songDao.setFavourite(s.videoId, if (s.isFavourite == 1) 0 else 1) }
    fun createPlaylist(name: String) = viewModelScope.launch { playlistDao.create(PlaylistEntity(name = name)) }
    fun addToPlaylist(pid: Long, vid: String) = viewModelScope.launch { playlistDao.addSong(PlaylistSongCrossRef(pid, vid)) }
}
