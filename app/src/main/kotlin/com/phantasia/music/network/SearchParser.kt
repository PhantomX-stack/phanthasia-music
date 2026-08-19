package com.phantasia.music.network

import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchParser @Inject constructor() {
    fun parse(root: JsonElement): List<SearchResultModel> {
        val out = mutableListOf<SearchResultModel>()
        try {
            val contents = root.obj("contents")
            val sectionList = contents?.obj("tabbedSearchResultsRenderer")
                ?.arr("tabs")?.idx(0)?.obj("tabRenderer")?.obj("content")?.obj("sectionListRenderer")?.arr("contents")
                ?: contents?.obj("singleColumnSearchResultsRenderer")
                    ?.arr("tabs")?.idx(0)?.obj("tabRenderer")?.obj("content")?.obj("sectionListRenderer")?.arr("contents")
                ?: contents?.obj("sectionListRenderer")?.arr("contents")
                ?: contents?.arr("contents")
                ?: emptyList()

            for (section in sectionList) {
                // 1. MusicCardShelfRenderer (Top Result card)
                section.obj("musicCardShelfRenderer")?.let { card ->
                    parseCardShelf(card)?.let { out.add(it) }
                    // Also parse any nested list items in the card
                    card.arr("contents")?.forEach { nestedItem ->
                        nestedItem.obj("musicResponsiveListItemRenderer")?.let { r ->
                            parseItem(r)?.let { out.add(it) }
                        }
                    }
                }

                // 2. MusicShelfRenderer (Standard search results list)
                section.obj("musicShelfRenderer")?.arr("contents")?.forEach { item ->
                    item.obj("musicResponsiveListItemRenderer")?.let { r ->
                        parseItem(r)?.let { out.add(it) }
                    }
                }

                // 3. MusicCarouselShelfRenderer (Carousel results)
                section.obj("musicCarouselShelfRenderer")?.arr("contents")?.forEach { item ->
                    item.obj("musicTwoRowItemRenderer")?.let { r ->
                        parseTwoRowItem(r)?.let { out.add(it) }
                    } ?: item.obj("musicResponsiveListItemRenderer")?.let { r ->
                        parseItem(r)?.let { out.add(it) }
                    }
                }

                // 4. ItemSectionRenderer
                section.obj("itemSectionRenderer")?.arr("contents")?.forEach { item ->
                    item.obj("musicResponsiveListItemRenderer")?.let { r ->
                        parseItem(r)?.let { out.add(it) }
                    }
                }
            }
        } catch (_: Exception) {}
        return out.distinctBy {
            when (it) {
                is SearchResultModel.TrackResult -> "track_${it.track.videoId}"
                is SearchResultModel.AlbumResult -> "album_${it.album.browseId}"
                is SearchResultModel.ArtistResult -> "artist_${it.artist.browseId}"
            }
        }
    }

    private fun parseCardShelf(card: JsonElement): SearchResultModel? = try {
        val header = card.obj("header")?.obj("musicCardShelfHeaderBasicRenderer")
        val title = card.obj("title")?.runsText() ?: header?.obj("title")?.runsText() ?: return null
        val subRuns = card.obj("subtitle")?.arr("runs")
        val thumb = card.obj("thumbnail")?.obj("musicThumbnailRenderer")?.obj("thumbnail")?.bestThumbnailUrl() ?: ""

        val navEp = card.obj("title")?.arr("runs")?.idx(0)?.obj("navigationEndpoint")
            ?: card.obj("onTap")
        val watchId = navEp?.obj("watchEndpoint")?.str("videoId")
        val browseId = navEp?.obj("browseEndpoint")?.str("browseId")

        if (watchId != null) {
            val artist = subRuns?.idx(0)?.asStr() ?: "YouTube Music"
            val dur = subRuns?.lastOrNull()?.asStr() ?: "0:00"
            SearchResultModel.TrackResult(TrackModel(watchId, title, artist, "", thumb, parseDur(dur)))
        } else if (browseId != null) {
            val isAlbum = browseId.startsWith("MPRE") || browseId.startsWith("FEmusic_library_album")
            if (isAlbum) {
                val artist = subRuns?.idx(0)?.asStr() ?: ""
                val year = subRuns?.lastOrNull()?.asStr() ?: ""
                SearchResultModel.AlbumResult(AlbumModel(browseId, title, year, thumb, artist))
            } else {
                SearchResultModel.ArtistResult(ArtistModel(browseId, title, thumb))
            }
        } else null
    } catch (_: Exception) { null }

    private fun parseTwoRowItem(r: JsonElement): SearchResultModel? = try {
        val title = r.obj("title")?.runsText() ?: return null
        val thumb = r.obj("thumbnailRenderer")?.obj("musicThumbnailRenderer")
            ?.obj("thumbnail")?.bestThumbnailUrl()
            ?: r.obj("thumbnail")?.bestThumbnailUrl() ?: ""
        val navEp = r.obj("navigationEndpoint")
        val watchId = navEp?.obj("watchEndpoint")?.str("videoId")
        val browseId = navEp?.obj("browseEndpoint")?.str("browseId")
        val subRuns = r.obj("subtitle")?.arr("runs")

        if (watchId != null) {
            val artist = subRuns?.idx(0)?.asStr() ?: ""
            SearchResultModel.TrackResult(TrackModel(watchId, title, artist, "", thumb, 0L))
        } else if (browseId != null) {
            val isAlbum = browseId.startsWith("MPRE") || browseId.startsWith("FEmusic_library_album")
            if (isAlbum) {
                val artist = subRuns?.idx(0)?.asStr() ?: ""
                val year = subRuns?.lastOrNull()?.asStr() ?: ""
                SearchResultModel.AlbumResult(AlbumModel(browseId, title, year, thumb, artist))
            } else {
                SearchResultModel.ArtistResult(ArtistModel(browseId, title, thumb))
            }
        } else null
    } catch (_: Exception) { null }

    private fun parseItem(r: JsonElement): SearchResultModel? = try {
        val cols = r.arr("flexColumns") ?: return null
        val col0 = cols.idx(0)?.obj("musicResponsiveListItemFlexColumnRenderer")
        val col1 = cols.idx(1)?.obj("musicResponsiveListItemFlexColumnRenderer")
        val title = col0?.obj("text")?.runsText() ?: return null
        val thumb = r.obj("thumbnail")?.obj("musicThumbnailRenderer")?.obj("thumbnail")?.bestThumbnailUrl()
            ?: r.obj("thumbnail")?.bestThumbnailUrl() ?: ""
        val runs0 = col0.obj("text")?.arr("runs")
        val navEp = runs0?.idx(0)?.obj("navigationEndpoint") ?: r.obj("navigationEndpoint")
        val watchId = navEp?.obj("watchEndpoint")?.str("videoId")
            ?: r.obj("playlistItemData")?.str("videoId")
        val browseId = navEp?.obj("browseEndpoint")?.str("browseId")
        val pageType = navEp?.obj("browseEndpoint")
            ?.obj("browseEndpointContextSupportedConfigs")
            ?.obj("browseEndpointContextMusicConfig")?.str("pageType")

        when {
            watchId != null -> {
                val artist = col1?.obj("text")?.arr("runs")?.idx(0)?.asStr() ?: "Unknown"
                val album = col1?.obj("text")?.arr("runs")?.idx(2)?.asStr() ?: ""
                val dur = col1?.obj("text")?.arr("runs")?.lastOrNull()?.asStr() ?: "0:00"
                SearchResultModel.TrackResult(TrackModel(watchId, title, artist, album, thumb, parseDur(dur)))
            }
            browseId != null && (pageType?.contains("ALBUM") == true || browseId.startsWith("MPRE")) -> {
                val artist = col1?.obj("text")?.arr("runs")?.idx(0)?.asStr() ?: ""
                val year = col1?.obj("text")?.arr("runs")?.lastOrNull()?.asStr() ?: ""
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
