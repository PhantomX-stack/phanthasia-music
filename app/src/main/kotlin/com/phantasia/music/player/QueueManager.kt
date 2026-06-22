package com.phantasia.music.player

import com.phantasia.music.network.TrackModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class RepeatMode { NONE, ONE, ALL }

data class QueueState(
    val tracks: List<TrackModel> = emptyList(), val currentIndex: Int = 0,
    val shuffleEnabled: Boolean = false, val repeatMode: RepeatMode = RepeatMode.NONE,
) {
    val currentTrack get() = tracks.getOrNull(currentIndex)
    val hasNext get() = currentIndex < tracks.size - 1 || repeatMode == RepeatMode.ALL
    val hasPrev get() = currentIndex > 0
}

@Singleton
class QueueManager @Inject constructor() {
    private val _state = MutableStateFlow(QueueState())
    val state: StateFlow<QueueState> = _state.asStateFlow()

    fun setQueue(tracks: List<TrackModel>, startIndex: Int = 0) {
        _state.value = _state.value.copy(tracks = tracks,
            currentIndex = startIndex.coerceIn(0, tracks.lastIndex.coerceAtLeast(0)))
    }
    fun addTrack(t: TrackModel) { _state.value = _state.value.copy(tracks = _state.value.tracks + t) }
    fun skipToNext() {
        val s = _state.value
        val next = when {
            s.currentIndex < s.tracks.lastIndex -> s.currentIndex + 1
            s.repeatMode == RepeatMode.ALL  -> 0
            else -> return
        }
        _state.value = s.copy(currentIndex = next)
    }
    fun skipToPrev() { val s = _state.value; if (s.hasPrev) _state.value = s.copy(currentIndex = s.currentIndex - 1) }
    fun toggleShuffle() { _state.value = _state.value.copy(shuffleEnabled = !_state.value.shuffleEnabled) }
    fun cycleRepeat() {
        val n = RepeatMode.values()[(_state.value.repeatMode.ordinal + 1) % RepeatMode.values().size]
        _state.value = _state.value.copy(repeatMode = n)
    }
}
