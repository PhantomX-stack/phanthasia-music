package com.phantasia.music.ui

import com.phantasia.music.storage.DownloadQuality

enum class AudioQuality(val label: String, val bitrate: String) {
    AUTO("Auto",         "Automatic"),
    HIGH("High",         "256 kbps"),
    MEDIUM("Medium",     "128 kbps"),
    LOW("Low",           "64 kbps"),
    VERY_LOW("Very low", "32 kbps")
}

enum class CacheSize(val label: String, val bytes: Long) {
    MB256("256 MB",  256L * 1024 * 1024),
    MB512("512 MB",  512L * 1024 * 1024),
    GB1("1 GB",      1024L * 1024 * 1024),
    GB2("2 GB",      2L * 1024 * 1024 * 1024),
    GB4("4 GB",      4L * 1024 * 1024 * 1024),
}

enum class PlayerBackground(val label: String) {
    GRADIENT("Gradient"),
    BLUR("Blurred artwork"),
    SOLID("Solid dark"),
    COLOUR("Artwork colour")
}

enum class LyricsPosition(val label: String) {
    BOTTOM("Bottom overlay"),
    FULLSCREEN("Full screen"),
    SIDE("Side panel")
}

enum class ContentLanguage(val label: String, val code: String) {
    ENGLISH("English", "en"),
    HINDI("Hindi",     "hi"),
    SPANISH("Spanish", "es"),
    FRENCH("French",   "fr"),
    GERMAN("German",   "de"),
    JAPANESE("Japanese","ja"),
    KOREAN("Korean",   "ko"),
    PORTUGUESE("Portuguese","pt"),
}

enum class ContentCountry(val label: String, val code: String) {
    US("United States", "US"),
    IN("India",         "IN"),
    GB("United Kingdom","GB"),
    AU("Australia",     "AU"),
    CA("Canada",        "CA"),
    DE("Germany",       "DE"),
    FR("France",        "FR"),
    JP("Japan",         "JP"),
    KR("South Korea",   "KR"),
    BR("Brazil",        "BR"),
}

data class SettingsState(
    // Playback
    val audioQuality:         AudioQuality     = AudioQuality.AUTO,
    val crossfadeSeconds:     Int              = 0,
    val normalizeVolume:      Boolean          = false,
    val skipSilence:          Boolean          = false,
    val autoPlayRelated:      Boolean          = true,
    val continueOnError:      Boolean          = true,
    val persistQueue:         Boolean          = true,

    // Player UI
    val playerBackground:     PlayerBackground = PlayerBackground.GRADIENT,
    val showLyricsByDefault:  Boolean          = false,
    val lyricsPosition:       LyricsPosition   = LyricsPosition.BOTTOM,
    val showSongThumbnailInNotif: Boolean      = true,

    // Network
    val streamOnCellular:     Boolean          = true,
    val downloadOnWifiOnly:   Boolean          = true,
    val downloadQuality:      DownloadQuality  = DownloadQuality.HIGH,
    val cacheSize:            CacheSize        = CacheSize.GB2,
    val prefetchEnabled:      Boolean          = true,

    // Content
    val contentLanguage:      ContentLanguage  = ContentLanguage.ENGLISH,
    val contentCountry:       ContentCountry   = ContentCountry.US,
    val enableExplicit:       Boolean          = true,
    val safeSearch:           Boolean          = false,

    // Privacy
    val pauseListenHistory:   Boolean          = false,
    val pauseSearchHistory:   Boolean          = false,

    // Appearance — changing these applies instantly
    val dynamicColour:        Boolean          = false,
    val pureBlack:            Boolean          = false,
)
