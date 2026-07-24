package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import com.phantasia.music.security.SecurePreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: SecurePreferenceManager
) : ViewModel() {

    private val _state = MutableStateFlow(loadFromPrefs())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private fun loadFromPrefs() = SettingsState(
        audioQuality     = AudioQuality.values().find { it.name == prefs.getString("audio_quality") } ?: AudioQuality.HIGH,
        streamOnCellular = prefs.getBoolean("stream_on_cellular", true),
        cacheSize        = CacheSize.values().find { it.name == prefs.getString("cache_size") } ?: CacheSize.GB2,
        darkMode         = DarkModeOption.values().find { it.name == prefs.getString("dark_mode") } ?: DarkModeOption.SYSTEM,
        crossfadeSeconds = prefs.getString("crossfade_seconds")?.toIntOrNull() ?: 3,
        normalizeVolume  = prefs.getBoolean("normalize_volume", false),
        downloadOnWifiOnly = prefs.getBoolean("download_wifi_only", true)
    )

    fun setAudioQuality(v: AudioQuality)     { _state.update { it.copy(audioQuality = v) };       prefs.putString("audio_quality", v.name) }
    fun setStreamOnCellular(v: Boolean)      { _state.update { it.copy(streamOnCellular = v) };   prefs.putBoolean("stream_on_cellular", v) }
    fun setCacheSize(v: CacheSize)           { _state.update { it.copy(cacheSize = v) };          prefs.putString("cache_size", v.name) }
    fun setDarkMode(v: DarkModeOption)       { _state.update { it.copy(darkMode = v) };           prefs.putString("dark_mode", v.name) }
    fun setCrossfade(v: Int)                 { _state.update { it.copy(crossfadeSeconds = v) };   prefs.putString("crossfade_seconds", v.toString()) }
    fun setNormalizeVolume(v: Boolean)       { _state.update { it.copy(normalizeVolume = v) };    prefs.putBoolean("normalize_volume", v) }
    fun setDownloadOnWifiOnly(v: Boolean)    { _state.update { it.copy(downloadOnWifiOnly = v) }; prefs.putBoolean("download_wifi_only", v) }
}
