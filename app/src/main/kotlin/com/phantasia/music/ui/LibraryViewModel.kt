package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantasia.music.scanner.LocalMusicScanner
import com.phantasia.music.scanner.LocalSong
import com.phantasia.music.storage.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songDao:           SongDao,
    private val playlistDao:       PlaylistDao,
    private val localMusicScanner: LocalMusicScanner
) : ViewModel() {

    val favourites: StateFlow<List<SongEntity>> = songDao.getFavourites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playlists: Flow<List<PlaylistWithSongs>> = playlistDao.getAll()

    private val _localSongs = MutableStateFlow<List<LocalSong>>(emptyList())
    val localSongs: StateFlow<List<LocalSong>> = _localSongs.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanMessage = MutableStateFlow<String?>(null)
    val scanMessage: StateFlow<String?> = _scanMessage.asStateFlow()

    init {
        // Initial silent scan
        scanDeviceMusic()
    }

    fun scanDeviceMusic() {
        viewModelScope.launch {
            _isScanning.value = true
            _scanMessage.value = "Scanning device storage for audio files…"
            try {
                val scanned = localMusicScanner.scanDeviceAudio()
                _localSongs.value = scanned
                _scanMessage.value = if (scanned.isEmpty()) "No local music found on device" else "Found ${scanned.size} local songs"
            } catch (e: Exception) {
                _scanMessage.value = "Failed to scan device: ${e.message}"
            } finally {
                _isScanning.value = false
            }
        }
    }

    fun clearScanMessage() {
        _scanMessage.value = null
    }

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
