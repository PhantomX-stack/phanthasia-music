package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.phantasia.music.network.MusicRepository
import com.phantasia.music.network.TrackModel
import com.phantasia.music.player.PlayerStateHolder
import com.phantasia.music.player.QueueManager
import com.phantasia.music.storage.SongDao
import com.phantasia.music.storage.SongEntity
import com.phantasia.music.storage.StatsDao
import com.phantasia.music.security.SecurePreferenceManager
import com.phantasia.music.storage.PlayEventEntity
import com.phantasia.music.storage.PlayCountEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repo:    MusicRepository,
    private val sh:      PlayerStateHolder,
    private val player:  ExoPlayer,
    private val queue:   QueueManager,
    private val songDao: SongDao,
    private val statsDao: StatsDao,
    private val prefs: SecurePreferenceManager
) : ViewModel() {

    val uiState: StateFlow<PlayerUiState> = sh.uiState
    val queueState: StateFlow<com.phantasia.music.player.QueueState> = queue.state

    init {
        sh.onTrackEnded = {
            handleTrackEnded()
        }
        sh.onPlayerError = { _ ->
            val playing = sh.uiState.value as? PlayerUiState.Playing
            if (playing != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    val searchTracks = repo.search("${playing.track.title} ${playing.track.artistName}", "songs")
                        .filterIsInstance<com.phantasia.music.network.SearchResultModel.TrackResult>()
                        .map { it.track }
                    val alternative = searchTracks.firstOrNull { it.videoId != playing.track.videoId }
                    if (alternative != null) {
                        val altStream = repo.getStream(alternative.videoId)
                        if (altStream != null && altStream.streamUrl.isNotBlank()) {
                            sh.playStream(playing.track, altStream.streamUrl, queue.state.value.shuffleEnabled, queue.state.value.repeatMode)
                        }
                    }
                }
            }
        }
    }

    fun onEvent(e: PlayerUiEvent) {
        when (e) {
            is PlayerUiEvent.Play            -> player.play()
            is PlayerUiEvent.Pause           -> player.pause()
            is PlayerUiEvent.SkipNext        -> skipNext()
            is PlayerUiEvent.SkipPrev        -> skipPrev()
            is PlayerUiEvent.Seek            -> player.seekTo(e.positionMs)
            is PlayerUiEvent.ToggleShuffle   -> {
                queue.toggleShuffle()
                syncQueueSettingsToState()
            }
            is PlayerUiEvent.CycleRepeat     -> {
                queue.cycleRepeat()
                syncQueueSettingsToState()
            }
            is PlayerUiEvent.PlayTrack       -> {
                if (e.playlist.isNotEmpty()) {
                    val idx = e.playlist.indexOfFirst { it.videoId == e.track.videoId }
                    queue.setQueue(e.playlist, if (idx >= 0) idx else 0)
                    // Seamlessly prefetch more radio songs based on last song in playlist
                    fetchEndlessRadio(e.playlist.lastOrNull() ?: e.track)
                } else {
                    queue.setQueue(listOf(e.track), 0)
                    fetchEndlessRadio(e.track)
                }
                loadAndPlay(e.track)
            }
            is PlayerUiEvent.PlayQueueIndex  -> {
                val track = queue.playTrackAt(e.index)
                if (track != null) {
                    loadAndPlay(track)
                    if (queue.state.value.remainingCount <= 5) {
                        fetchEndlessRadio(track)
                    }
                }
            }
            is PlayerUiEvent.RemoveQueueIndex -> {
                queue.removeTrack(e.index)
                if (queue.state.value.remainingCount <= 5) {
                    queue.state.value.currentTrack?.let { fetchEndlessRadio(it) }
                }
            }
            is PlayerUiEvent.AddToQueue      -> {
                queue.addToQueue(e.track)
            }
            is PlayerUiEvent.PlayNext        -> {
                queue.playNext(e.track)
            }
            is PlayerUiEvent.ClearQueue      -> {
                queue.clearUpcoming()
                queue.state.value.currentTrack?.let { fetchEndlessRadio(it) }
            }
            is PlayerUiEvent.ToggleFavourite -> toggleFavourite()
            is PlayerUiEvent.ToggleLyrics    -> toggleLyrics()
            is PlayerUiEvent.SearchLyricsOnline -> searchLyricsOnline(e.customQuery)
            is PlayerUiEvent.SetViewThumbnail -> {
                setViewMode(PlayerViewMode.THUMBNAIL)
                player.play()
            }
            is PlayerUiEvent.SetViewVideo     -> {
                player.pause()
                setViewMode(PlayerViewMode.VIDEO)
            }
            is PlayerUiEvent.Stop             -> {
                sh.stop()
            }
        }
    }

    private fun skipNext() {
        val next = queue.skipToNext()
        if (next != null) {
            loadAndPlay(next)
            if (queue.state.value.remainingCount <= 6) {
                fetchEndlessRadio(next)
            }
        } else {
            val curr = queue.state.value.currentTrack
            if (curr != null) {
                viewModelScope.launch {
                    val radioSongs = repo.getRadioTracks(curr.videoId, curr.title, curr.artistName)
                    if (radioSongs.isNotEmpty()) {
                        queue.addTracks(radioSongs)
                        val nextTrack = queue.skipToNext()
                        if (nextTrack != null) loadAndPlay(nextTrack)
                    }
                }
            }
        }
    }

    private fun skipPrev() {
        val prev = queue.skipToPrev()
        if (prev != null) {
            loadAndPlay(prev)
        }
    }

    private fun handleTrackEnded() {
        viewModelScope.launch(Dispatchers.Main) {
            val s = queue.state.value
            if (s.repeatMode == com.phantasia.music.player.RepeatMode.ONE) {
                player.seekTo(0)
                player.play()
                return@launch
            }
            skipNext()
        }
    }

    private fun fetchEndlessRadio(seedTrack: TrackModel) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                val songs = repo.getRadioTracks(seedTrack.videoId, seedTrack.title, seedTrack.artistName)
                if (songs.isNotEmpty()) {
                    queue.addTracks(songs)
                }
            }
        }
    }

    private fun syncQueueSettingsToState() {
        val s = sh.uiState.value as? PlayerUiState.Playing ?: return
        val q = queue.state.value
        sh.updatePlayingState(s.copy(
            shuffleEnabled = q.shuffleEnabled,
            repeatMode = q.repeatMode
        ))
    }

    private fun loadAndPlay(track: TrackModel) {
        viewModelScope.launch {
            val q = queue.state.value
            sh.prepareTrack(track, q.shuffleEnabled, q.repeatMode)

            val isLocal = track.videoId.startsWith("local_") ||
                (track.streamUrl != null && (track.streamUrl.startsWith("content://") || track.streamUrl.startsWith("file://")))

            if (isLocal) {
                val localUri = track.streamUrl.takeIf { !it.isNullOrBlank() }
                    ?: run {
                        val rawId = track.videoId.removePrefix("local_").toLongOrNull()
                        if (rawId != null) {
                            android.content.ContentUris.withAppendedId(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, rawId).toString()
                        } else {
                            track.videoId.removePrefix("local_")
                        }
                    }
                sh.playStream(track, localUri, q.shuffleEnabled, q.repeatMode)
            } else {
                val stream = repo.getStream(track.videoId)
                if (stream != null && stream.streamUrl.isNotBlank()) {
                    sh.playStream(track, stream.streamUrl, q.shuffleEnabled, q.repeatMode)
                } else {
                    // If direct stream resolution couldn't find stream for this ID, search for best audio match
                    launch(Dispatchers.IO) {
                        val searchTracks = repo.search("${track.title} ${track.artistName}", "songs")
                            .filterIsInstance<com.phantasia.music.network.SearchResultModel.TrackResult>()
                            .map { it.track }
                        val alternative = searchTracks.firstOrNull { it.videoId != track.videoId }
                        if (alternative != null) {
                            val altStream = repo.getStream(alternative.videoId)
                            if (altStream != null && altStream.streamUrl.isNotBlank()) {
                                sh.playStream(track, altStream.streamUrl, q.shuffleEnabled, q.repeatMode)
                            }
                        }
                    }
                }
            }

            // In parallel fetch lyrics
            launch(Dispatchers.IO) {
                val lyrics = repo.getLyrics(track)
                (sh.uiState.value as? PlayerUiState.Playing)?.let { curr ->
                    if (curr.track.videoId == track.videoId) {
                        sh.updatePlayingState(curr.copy(lyricsLines = lyrics))
                    }
                }
            }

            if (!prefs.getBoolean("pause_listen_history", false)) {
                recordPlay(track)
            }
            // Persist to local DB
            songDao.upsert(
                SongEntity(
                    videoId         = track.videoId,
                    title           = track.title,
                    artistName      = track.artistName,
                    albumTitle      = track.albumTitle,
                    artworkUrl      = track.artworkUrl,
                    durationSeconds = track.durationSeconds
                )
            )
        }
    }

    private fun toggleLyrics() {
        val s = sh.uiState.value as? PlayerUiState.Playing ?: return
        val newShow = !s.showLyrics
        sh.updatePlayingState(s.copy(showLyrics = newShow))
        if (newShow && s.lyricsLines.isEmpty()) {
            searchLyricsOnline(null)
        }
    }

    private fun searchLyricsOnline(customQuery: String?) {
        val s = sh.uiState.value as? PlayerUiState.Playing ?: return
        val targetTrack = s.track
        sh.updatePlayingState(
            s.copy(
                isSearchingLyrics = true,
                lyricsStatusMessage = "Searching online lyrics for \"${targetTrack.title}\"…"
            )
        )
        viewModelScope.launch(Dispatchers.IO) {
            val lyrics = repo.searchLyricsOnline(targetTrack, customQuery)
            val current = sh.uiState.value as? PlayerUiState.Playing
            if (current != null && current.track.videoId == targetTrack.videoId) {
                sh.updatePlayingState(
                    current.copy(
                        lyricsLines = lyrics,
                        isSearchingLyrics = false,
                        lyricsStatusMessage = if (lyrics.isNotEmpty()) "Lyrics synchronized" else "No lyrics found online"
                    )
                )
            }
        }
    }

    private fun setViewMode(mode: PlayerViewMode) {
        val s = sh.uiState.value as? PlayerUiState.Playing ?: return
        sh.updatePlayingState(s.copy(viewMode = mode))
    }

    private fun recordPlay(track: TrackModel) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching {
                statsDao.insertEvent(PlayEventEntity(
                    videoId    = track.videoId,
                    title      = track.title,
                    artistName = track.artistName,
                    artworkUrl = track.artworkUrl,
                    playedAt   = System.currentTimeMillis(),
                    durationMs = 0L
                ))
                val existing = statsDao.getTopSongs(1000).first()
                    .find { it.videoId == track.videoId }
                statsDao.upsertCount(PlayCountEntity(
                    videoId    = track.videoId,
                    title      = track.title,
                    artistName = track.artistName,
                    artworkUrl = track.artworkUrl,
                    count      = (existing?.count ?: 0) + 1,
                    totalMs    = existing?.totalMs ?: 0L
                ))
            }
        }
    }

    private fun toggleFavourite() {
        val playing = uiState.value as? PlayerUiState.Playing ?: return
        viewModelScope.launch {
            val existing = songDao.getById(playing.track.videoId)
            val newFav = if (existing?.isFavourite == 1) 0 else 1
            if (existing != null) {
                songDao.setFavourite(existing.videoId, newFav)
            } else {
                songDao.upsert(
                    SongEntity(
                        videoId = playing.track.videoId,
                        title = playing.track.title,
                        artistName = playing.track.artistName,
                        albumTitle = playing.track.albumTitle,
                        artworkUrl = playing.track.artworkUrl,
                        durationSeconds = playing.track.durationSeconds,
                        isFavourite = newFav
                    )
                )
            }
            sh.updatePlayingState(playing.copy(isFavourite = newFav == 1))
        }
    }
}
