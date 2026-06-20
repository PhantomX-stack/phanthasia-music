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
                val contents = section.obj("musicShelfRenderer")?.arr("contents") ?: continue
                for (item in contents) {
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
        val thumb = r.obj("thumbnail")?.obj("musicThumbnailRenderer")
            ?.obj("thumbnail")?.bestThumbnailUrl() ?: ""
        val pageType = col0?.obj("text")?.arr("runs")?.idx(0)
            ?.obj("navigationEndpoint")?.obj("browseEndpoint")
            ?.obj("browseEndpointContextSupportedConfigs")
            ?.obj("browseEndpointContextMusicConfig")?.str("pageType")
            ?: col0?.obj("text")?.arr("runs")?.idx(0)
                ?.obj("navigationEndpoint")?.obj("watchEndpoint")
                ?.str("videoId")?.let { "SONG" } ?: "SONG"
        when {
            pageType.contains("ALBUM") || pageType.contains("PLAYLIST") -> {
                val bid = col0?.obj("text")?.arr("runs")?.idx(0)
                    ?.obj("navigationEndpoint")?.obj("browseEndpoint")?.str("browseId") ?: return null
                SearchResultModel.AlbumResult(AlbumModel(bid, title,
                    col1?.obj("text")?.arr("runs")?.lastOrNull()?.asStr() ?: "",
                    thumb,
                    col1?.obj("text")?.arr("runs")?.idx(0)?.asStr() ?: ""))
            }
            pageType.contains("ARTIST") -> {
                val bid = col0?.obj("text")?.arr("runs")?.idx(0)
                    ?.obj("navigationEndpoint")?.obj("browseEndpoint")?.str("browseId") ?: return null
                SearchResultModel.ArtistResult(ArtistModel(bid, title, thumb))
            }
            else -> {
                val vid = col0?.obj("text")?.arr("runs")?.idx(0)
                    ?.obj("navigationEndpoint")?.obj("watchEndpoint")?.str("videoId") ?: return null
                val artist = col1?.obj("text")?.arr("runs")?.idx(0)?.asStr() ?: "Unknown"
                val album  = col1?.obj("text")?.arr("runs")?.idx(2)?.asStr() ?: ""
                val dur    = col1?.obj("text")?.arr("runs")?.lastOrNull()?.asStr() ?: "0:00"
                SearchResultModel.TrackResult(TrackModel(vid, title, artist, album, thumb, dur(dur)))
            }
        }
    } catch (_: Exception) { null }

    private fun dur(raw: String): Long = runCatching {
        val p = raw.trim().split(":").map { it.toLong() }
        when (p.size) { 2 -> p[0]*60+p[1]; 3 -> p[0]*3600+p[1]*60+p[2]; else -> 0L }
    }.getOrDefault(0L)
}
