package com.phantasia.music.player

import com.phantasia.music.network.TrackModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class RepeatMode { NONE, ONE, ALL }

data class QueueState(
    val tracks: List<TrackModel> = emptyList(),
    val currentIndex: Int = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.NONE,
) {
    val currentTrack get() = tracks.getOrNull(currentIndex)
    val hasNext get() = currentIndex < tracks.size - 1 || repeatMode == RepeatMode.ALL
    val hasPrev get() = currentIndex > 0
    val remainingCount get() = (tracks.size - 1 - currentIndex).coerceAtLeast(0)
}

@Singleton
class QueueManager @Inject constructor() {
    private val _state = MutableStateFlow(QueueState())
    val state: StateFlow<QueueState> = _state.asStateFlow()

    fun setQueue(tracks: List<TrackModel>, startIndex: Int = 0) {
        if (tracks.isEmpty()) return
        val distinctTracks = tracks.distinctBy { it.videoId }
        _state.value = _state.value.copy(
            tracks = distinctTracks,
            currentIndex = startIndex.coerceIn(0, distinctTracks.lastIndex.coerceAtLeast(0))
        )
    }

    fun addTrack(t: TrackModel) {
        val s = _state.value
        if (s.tracks.any { it.videoId == t.videoId }) return
        _state.value = s.copy(tracks = s.tracks + t)
    }

    fun addTracks(newTracks: List<TrackModel>) {
        val s = _state.value
        val existingIds = s.tracks.map { it.videoId }.toSet()
        val toAdd = newTracks.filter { it.videoId !in existingIds }
        if (toAdd.isNotEmpty()) {
            _state.value = s.copy(tracks = s.tracks + toAdd)
        }
    }

    fun skipToNext(): TrackModel? {
        val s = _state.value
        val next = when {
            s.currentIndex < s.tracks.lastIndex -> s.currentIndex + 1
            s.repeatMode == RepeatMode.ALL  -> 0
            else -> return null
        }
        _state.value = s.copy(currentIndex = next)
        return _state.value.currentTrack
    }

    fun skipToPrev(): TrackModel? {
        val s = _state.value
        if (!s.hasPrev) return null
        _state.value = s.copy(currentIndex = s.currentIndex - 1)
        return _state.value.currentTrack
    }

    fun toggleShuffle() {
        val s = _state.value
        val newShuffle = !s.shuffleEnabled
        if (newShuffle && s.tracks.size > 1) {
            val curr = s.currentTrack
            val remaining = s.tracks.filter { it.videoId != curr?.videoId }.shuffled()
            val newTracks = if (curr != null) listOf(curr) + remaining else remaining
            _state.value = s.copy(tracks = newTracks, currentIndex = 0, shuffleEnabled = true)
        } else {
            _state.value = s.copy(shuffleEnabled = newShuffle)
        }
    }

    fun cycleRepeat() {
        val n = RepeatMode.values()[(_state.value.repeatMode.ordinal + 1) % RepeatMode.values().size]
        _state.value = _state.value.copy(repeatMode = n)
    }

    fun playTrackAt(index: Int): TrackModel? {
        val s = _state.value
        if (index !in s.tracks.indices) return null
        _state.value = s.copy(currentIndex = index)
        return _state.value.currentTrack
    }

    fun playNext(track: TrackModel) {
        val s = _state.value
        val list = s.tracks.toMutableList()
        list.removeAll { it.videoId == track.videoId }
        val insertIndex = (s.currentIndex + 1).coerceAtMost(list.size)
        list.add(insertIndex, track)
        _state.value = s.copy(tracks = list)
    }

    fun addToQueue(track: TrackModel) {
        addTrack(track)
    }

    fun removeTrack(index: Int) {
        val s = _state.value
        if (index !in s.tracks.indices) return
        val list = s.tracks.toMutableList()
        list.removeAt(index)
        val newIdx = when {
            list.isEmpty() -> 0
            index < s.currentIndex -> s.currentIndex - 1
            index == s.currentIndex -> s.currentIndex.coerceAtMost(list.lastIndex)
            else -> s.currentIndex
        }
        _state.value = s.copy(tracks = list, currentIndex = newIdx)
    }

    fun moveTrack(fromIndex: Int, toIndex: Int) {
        val s = _state.value
        if (fromIndex !in s.tracks.indices || toIndex !in s.tracks.indices || fromIndex == toIndex) return
        val list = s.tracks.toMutableList()
        val item = list.removeAt(fromIndex)
        list.add(toIndex, item)
        val currentTrack = s.currentTrack
        val newCurrIdx = if (currentTrack != null) list.indexOfFirst { it.videoId == currentTrack.videoId }.coerceAtLeast(0) else 0
        _state.value = s.copy(tracks = list, currentIndex = newCurrIdx)
    }

    fun clearUpcoming() {
        val s = _state.value
        val curr = s.currentTrack ?: return
        _state.value = s.copy(tracks = listOf(curr), currentIndex = 0)
    }

    fun clearQueue() {
        _state.value = QueueState()
    }
}
