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
    private val songDao:     SongDao,
    private val playlistDao: PlaylistDao
) : ViewModel() {

    val favourites: StateFlow<List<SongEntity>> = songDao.getFavourites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playlists: Flow<List<PlaylistWithSongs>> = playlistDao.getAll()

    fun toggleFavourite(song: SongEntity) = viewModelScope.launch {
        songDao.setFavourite(song.videoId, if (song.isFavourite == 1) 0 else 1)
    }

    fun createPlaylist(name: String) = viewModelScope.launch {
        playlistDao.create(PlaylistEntity(name = name))
    }

    fun addToPlaylist(playlistId: Long, videoId: String) = viewModelScope.launch {
        playlistDao.addSong(PlaylistSongCrossRef(playlistId, videoId))
    }

    fun removeFromPlaylist(playlistId: Long, videoId: String) = viewModelScope.launch {
        playlistDao.removeSong(PlaylistSongCrossRef(playlistId, videoId))
    }
}
