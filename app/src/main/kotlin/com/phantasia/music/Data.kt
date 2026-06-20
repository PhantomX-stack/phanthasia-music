package com.phantasia.music

data class MusicItem(val id: String, val title: String, val subtitle: String)

object DataSource {
    private val libraryItems = listOf(
        MusicItem("1", "Dream Echo", "Ambient pop"),
        MusicItem("2", "Neon Pulse", "Synthwave"),
        MusicItem("3", "Aurora Drift", "Chill beats")
    )

    fun library(): List<MusicItem> = libraryItems
    fun find(id: String): MusicItem? = libraryItems.firstOrNull { it.id == id }
    fun search(query: String): List<MusicItem> {
        if (query.isBlank()) return libraryItems
        return libraryItems.filter {
            it.title.contains(query, ignoreCase = true) || it.subtitle.contains(query, ignoreCase = true)
        }
    }
}
