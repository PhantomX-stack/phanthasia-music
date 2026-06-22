package com.phantasia.music.network

import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchParser @Inject constructor() {
    fun parse(root: JsonElement): List<SearchResultModel> {
        val out = mutableListOf<SearchResultModel>()
        try {
            val sections = root.obj("contents")?.obj("tabbedSearchResultsRenderer")
                ?.arr("tabs")?.idx(0)?.obj("tabRenderer")?.obj("content")
                ?.obj("sectionListRenderer")?.arr("contents") ?: return out
            for (section in sections) {
                val items = section.obj("musicShelfRenderer")?.arr("contents") ?: continue
                for (item in items) {
                    val r = item.obj("musicResponsiveListItemRenderer") ?: continue
                    parseItem(r)?.let { out.add(it) }
                }
            }
        } catch (_: Exception) {}
        return out
    }

    private fun parseItem(r: JsonElement): SearchResultModel? = try {
        val cols  = r.arr("flexColumns") ?: return null
        val col0  = cols.idx(0)?.obj("musicResponsiveListItemFlexColumnRenderer")
        val col1  = cols.idx(1)?.obj("musicResponsiveListItemFlexColumnRenderer")
        val title = col0?.obj("text")?.runsText() ?: return null
        val thumb = r.obj("thumbnail")?.obj("musicThumbnailRenderer")?.obj("thumbnail")?.bestThumbnailUrl() ?: ""
        val runs0   = col0?.obj("text")?.arr("runs")
        val navEp   = runs0?.idx(0)?.obj("navigationEndpoint")
        val watchId  = navEp?.obj("watchEndpoint")?.str("videoId")
        val browseId = navEp?.obj("browseEndpoint")?.str("browseId")
        val pageType = navEp?.obj("browseEndpoint")
            ?.obj("browseEndpointContextSupportedConfigs")
            ?.obj("browseEndpointContextMusicConfig")?.str("pageType")
        when {
            watchId != null -> {
                val artist = col1?.obj("text")?.arr("runs")?.idx(0)?.asStr() ?: "Unknown"
                val album  = col1?.obj("text")?.arr("runs")?.idx(2)?.asStr() ?: ""
                val dur    = col1?.obj("text")?.arr("runs")?.lastOrNull()?.asStr() ?: "0:00"
                SearchResultModel.TrackResult(TrackModel(watchId, title, artist, album, thumb, parseDur(dur)))
            }
            browseId != null && pageType?.contains("ALBUM") == true -> {
                val artist = col1?.obj("text")?.arr("runs")?.idx(0)?.asStr() ?: ""
                val year   = col1?.obj("text")?.arr("runs")?.lastOrNull()?.asStr() ?: ""
                SearchResultModel.AlbumResult(AlbumModel(browseId, title, year, thumb, artist))
            }
            browseId != null -> SearchResultModel.ArtistResult(ArtistModel(browseId, title, thumb))
            else -> null
        }
    } catch (_: Exception) { null }

    private fun parseDur(raw: String): Long = runCatching {
        val p = raw.trim().split(":").map { it.toLong() }
        when (p.size) { 2 -> p[0]*60+p[1]; 3 -> p[0]*3600+p[1]*60+p[2]; else -> 0L }
    }.getOrDefault(0L)
}
