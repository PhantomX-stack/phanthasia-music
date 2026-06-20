package com.phantasia.music.network

import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumParser @Inject constructor() {

    fun parse(browseId: String, root: JsonElement): AlbumModel? = runCatching {
        val header = root.obj("header")?.obj("musicDetailHeaderRenderer")
            ?: root.obj("header")?.obj("musicImmersiveHeaderRenderer") ?: return null
        val sub    = header.obj("subtitle")?.arr("runs")
        AlbumModel(
            browseId     = browseId,
            title        = header.obj("title")?.runsText() ?: "Unknown",
            year         = sub?.lastOrNull()?.asStr() ?: "",
            thumbnailUrl = header.obj("thumbnail")?.obj("croppedSquareThumbnailRenderer")
                ?.obj("thumbnail")?.bestThumbnailUrl()
                ?: header.obj("thumbnail")?.obj("musicThumbnailRenderer")
                    ?.obj("thumbnail")?.bestThumbnailUrl() ?: "",
            artistName   = sub?.idx(0)?.asStr() ?: "",
            tracks       = parseTracks(root)
        )
    }.getOrNull()

    private fun parseTracks(root: JsonElement): List<TrackModel> = runCatching {
        root.obj("contents")?.obj("singleColumnBrowseResultsRenderer")
            ?.arr("tabs")?.idx(0)?.obj("tabRenderer")?.obj("content")
            ?.obj("sectionListRenderer")?.arr("contents")?.idx(0)
            ?.obj("musicShelfRenderer")?.arr("contents")
            ?.mapNotNull { item ->
                val r    = item.obj("musicResponsiveListItemRenderer") ?: return@mapNotNull null
                val cols = r.arr("flexColumns") ?: return@mapNotNull null
                val title = cols.idx(0)?.obj("musicResponsiveListItemFlexColumnRenderer")
                    ?.obj("text")?.runsText() ?: return@mapNotNull null
                val vid   = cols.idx(0)?.obj("musicResponsiveListItemFlexColumnRenderer")
                    ?.obj("text")?.arr("runs")?.idx(0)
                    ?.obj("navigationEndpoint")?.obj("watchEndpoint")?.str("videoId") ?: return@mapNotNull null
                val artist = cols.idx(1)?.obj("musicResponsiveListItemFlexColumnRenderer")
                    ?.obj("text")?.arr("runs")?.idx(0)?.asStr() ?: ""
                val dur    = cols.idx(1)?.obj("musicResponsiveListItemFlexColumnRenderer")
                    ?.obj("text")?.arr("runs")?.lastOrNull()?.asStr() ?: "0:00"
                val thumb  = r.obj("thumbnail")?.obj("musicThumbnailRenderer")
                    ?.obj("thumbnail")?.bestThumbnailUrl() ?: ""
                val dSec   = runCatching { val p = dur.split(":").map { it.toLong() }
                    when (p.size) { 2 -> p[0]*60+p[1]; 3 -> p[0]*3600+p[1]*60+p[2]; else -> 0L } }.getOrDefault(0L)
                TrackModel(vid, title, artist, "", thumb, dSec)
            } ?: emptyList()
    }.getOrDefault(emptyList())
}
