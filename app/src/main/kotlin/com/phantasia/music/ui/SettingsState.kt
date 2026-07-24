package com.phantasia.music.ui

enum class AudioQuality(val label: String) { HIGH("High"), MEDIUM("Medium"), LOW("Low") }
enum class CacheSize(val label: String, val bytes: Long) {
    MB512("512 MB", 512L * 1024 * 1024),
    GB1("1 GB", 1024L * 1024 * 1024),
    GB2("2 GB", 2L * 1024 * 1024 * 1024)
}
enum class DarkModeOption(val label: String) { SYSTEM("Follow system"), DARK("Always dark"), LIGHT("Always light") }

data class SettingsState(
    val audioQuality:       AudioQuality   = AudioQuality.HIGH,
    val streamOnCellular:   Boolean        = true,
    val cacheSize:          CacheSize      = CacheSize.GB2,
    val darkMode:           DarkModeOption = DarkModeOption.SYSTEM,
    val crossfadeSeconds:   Int            = 3,
    val normalizeVolume:    Boolean        = false,
    val downloadOnWifiOnly: Boolean        = true
)
