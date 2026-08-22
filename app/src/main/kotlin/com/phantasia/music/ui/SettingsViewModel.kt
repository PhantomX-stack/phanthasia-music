package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import com.phantasia.music.storage.DownloadQuality
import androidx.lifecycle.viewModelScope
import com.phantasia.music.security.SecurePreferenceManager
import com.phantasia.music.storage.SearchHistoryDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs:      SecurePreferenceManager,
    private val historyDao: SearchHistoryDao
) : ViewModel() {

    private val _state = MutableStateFlow(load())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private fun load() = SettingsState(
        audioQuality       = AudioQuality.values().find { it.name == prefs.getString("audio_quality") }     ?: AudioQuality.AUTO,
        crossfadeSeconds   = prefs.getString("crossfade")?.toIntOrNull()                                    ?: 0,
        normalizeVolume    = prefs.getBoolean("normalize_volume",    false),
        skipSilence        = prefs.getBoolean("skip_silence",        false),
        autoPlayRelated    = prefs.getBoolean("auto_play_related",   true),
        continueOnError    = prefs.getBoolean("continue_on_error",   true),
        persistQueue       = prefs.getBoolean("persist_queue",       true),
        progressBarStyle   = ProgressBarStyle.values().find { it.name == prefs.getString("progress_bar_style") } ?: ProgressBarStyle.GLOW_LINEAR,
        playerBackground   = PlayerBackground.values().find { it.name == prefs.getString("player_bg") }     ?: PlayerBackground.GRADIENT,
        showLyricsByDefault= prefs.getBoolean("show_lyrics_default", false),
        lyricsPosition     = LyricsPosition.values().find { it.name == prefs.getString("lyrics_position") } ?: LyricsPosition.BOTTOM,
        showSongThumbnailInNotif = prefs.getBoolean("notif_thumbnail", true),
        streamOnCellular   = prefs.getBoolean("stream_cellular",     true),
        downloadOnWifiOnly = prefs.getBoolean("download_wifi_only",  true),
        downloadQuality    = DownloadQuality.values().find { it.name == prefs.getString("download_quality") } ?: DownloadQuality.HIGH,
        cacheSize          = CacheSize.values().find { it.name == prefs.getString("cache_size") }           ?: CacheSize.GB2,
        prefetchEnabled    = prefs.getBoolean("prefetch",            true),
        contentLanguage    = ContentLanguage.values().find { it.name == prefs.getString("content_lang") }   ?: ContentLanguage.ENGLISH,
        contentCountry     = ContentCountry.values().find { it.name == prefs.getString("content_country") } ?: ContentCountry.US,
        enableExplicit     = prefs.getBoolean("explicit",            true),
        safeSearch         = prefs.getBoolean("safe_search",         false),
        pauseListenHistory = prefs.getBoolean("pause_listen_history",false),
        pauseSearchHistory = prefs.getBoolean("pause_search_history",false),
        dynamicColour      = prefs.getBoolean("dynamic_colour",      false),
        pureBlack          = prefs.getBoolean("pure_black",          false),
    )

    // Playback
    fun setAudioQuality(v: AudioQuality)       { save { copy(audioQuality = v) };       prefs.putString("audio_quality", v.name) }
    fun setCrossfade(v: Int)                   { save { copy(crossfadeSeconds = v) };   prefs.putString("crossfade", v.toString()) }
    fun setNormalizeVolume(v: Boolean)         { save { copy(normalizeVolume = v) };    prefs.putBoolean("normalize_volume", v) }
    fun setSkipSilence(v: Boolean)             { save { copy(skipSilence = v) };        prefs.putBoolean("skip_silence", v) }
    fun setAutoPlayRelated(v: Boolean)         { save { copy(autoPlayRelated = v) };    prefs.putBoolean("auto_play_related", v) }
    fun setContinueOnError(v: Boolean)         { save { copy(continueOnError = v) };    prefs.putBoolean("continue_on_error", v) }
    fun setPersistQueue(v: Boolean)            { save { copy(persistQueue = v) };       prefs.putBoolean("persist_queue", v) }

    // Player UI
    fun setProgressBarStyle(v: ProgressBarStyle) { save { copy(progressBarStyle = v) }; prefs.putString("progress_bar_style", v.name) }
    fun setPlayerBackground(v: PlayerBackground) { save { copy(playerBackground = v) };  prefs.putString("player_bg", v.name) }
    fun setShowLyricsByDefault(v: Boolean)     { save { copy(showLyricsByDefault = v) };prefs.putBoolean("show_lyrics_default", v) }
    fun setLyricsPosition(v: LyricsPosition)   { save { copy(lyricsPosition = v) };     prefs.putString("lyrics_position", v.name) }
    fun setShowThumbnailInNotif(v: Boolean)    { save { copy(showSongThumbnailInNotif = v) }; prefs.putBoolean("notif_thumbnail", v) }

    // Network
    fun setStreamOnCellular(v: Boolean)        { save { copy(streamOnCellular = v) };   prefs.putBoolean("stream_cellular", v) }
    fun setDownloadOnWifiOnly(v: Boolean)      { save { copy(downloadOnWifiOnly = v) }; prefs.putBoolean("download_wifi_only", v) }
    fun setDownloadQuality(v: DownloadQuality) { save { copy(downloadQuality = v) };    prefs.putString("download_quality", v.name) }
    fun setCacheSize(v: CacheSize)             { save { copy(cacheSize = v) };          prefs.putString("cache_size", v.name) }
    fun setPrefetchEnabled(v: Boolean)         { save { copy(prefetchEnabled = v) };    prefs.putBoolean("prefetch", v) }

    // Content
    fun setContentLanguage(v: ContentLanguage) { save { copy(contentLanguage = v) };    prefs.putString("content_lang", v.name) }
    fun setContentCountry(v: ContentCountry)   { save { copy(contentCountry = v) };     prefs.putString("content_country", v.name) }
    fun setEnableExplicit(v: Boolean)          { save { copy(enableExplicit = v) };     prefs.putBoolean("explicit", v) }
    fun setSafeSearch(v: Boolean)              { save { copy(safeSearch = v) };         prefs.putBoolean("safe_search", v) }

    // Privacy
    fun setPauseListenHistory(v: Boolean)      { save { copy(pauseListenHistory = v) }; prefs.putBoolean("pause_listen_history", v) }
    fun setPauseSearchHistory(v: Boolean)      { save { copy(pauseSearchHistory = v) }; prefs.putBoolean("pause_search_history", v) }
    fun clearSearchHistory()                   = viewModelScope.launch { historyDao.clearAll() }

    // Appearance
    fun setDynamicColour(v: Boolean)           { save { copy(dynamicColour = v) };      prefs.putBoolean("dynamic_colour", v) }
    fun setPureBlack(v: Boolean)               { save { copy(pureBlack = v) };          prefs.putBoolean("pure_black", v) }

    private fun save(block: SettingsState.() -> SettingsState) = _state.update(block)
}
