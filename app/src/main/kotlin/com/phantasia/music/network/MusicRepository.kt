package com.phantasia.music.network

import com.phantasia.music.ui.LrcLine
import com.phantasia.music.ui.parseLrc
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton

data class HomeSection(
    val title: String,
    val items: List<TrackModel>,
    val subtitle: String? = null
)

interface MusicRepository {
    suspend fun getHomeSections(): List<HomeSection>
    suspend fun getGenreTracks(genre: String): List<TrackModel>
    suspend fun search(query: String, filter: String? = null): List<SearchResultModel>
    suspend fun getStream(videoId: String): StreamDataModel?
    suspend fun getAlbum(browseId: String): AlbumModel?
    suspend fun getSuggestions(videoId: String): List<TrackModel>
    suspend fun getRadioTracks(videoId: String, seedTitle: String = "", seedArtist: String = ""): List<TrackModel>
    suspend fun getSearchSuggestions(query: String): List<String>
    suspend fun getPlaylist(playlistId: String): Pair<String, List<TrackModel>>
    suspend fun getLikedSongs(): List<TrackModel>
    suspend fun getLyrics(track: TrackModel): List<LrcLine>
    suspend fun searchLyricsOnline(track: TrackModel, customQuery: String? = null): List<LrcLine>
}

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val http:   HttpClient,
    private val req:    InnerTubeRequests,
    private val sParse: SearchParser,
    private val pParse: PlayerParser,
    private val aParse: AlbumParser,
    private val cipher: CipherEngine
) : MusicRepository {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    override suspend fun getHomeSections(): List<HomeSection> = coroutineScope {
        val sections = mutableListOf<HomeSection>()

        // 1. Try native InnerTube home browse
        runCatching {
            val raw = req.home().bodyAsText()
            val root = json.parseToJsonElement(raw).jsonObject
            extractSectionsFromRoot(root, sections)
        }

        // 2. If native browse returned few or no sections, fetch charts/explore
        if (sections.size < 3) {
            runCatching {
                val rawCharts = req.charts().bodyAsText()
                val rootCharts = json.parseToJsonElement(rawCharts).jsonObject
                extractSectionsFromRoot(rootCharts, sections)
            }
        }

        // 3. Dynamic category search queries to enrich sections
        val feedQueries = listOf(
            Triple("Easy Mornings", "PEPPY MUSIC TO START YOUR DAY", "Easy mornings songs acoustic hits"),
            Triple("Rain Therapy 🍀 🌧️", "FOR COZY DAYS AND ENDLESS CUPS OF TEA", "Rain therapy songs cozy acoustic hindi english"),
            Triple("Quick Picks", "START RADIO FROM A SONG", "Today's biggest top hits pop"),
            Triple("Today's Biggest Hits", "GLOBAL TOP CHARTS", "Top hits 2024 global charts"),
            Triple("Upbeat Bollywood & Hits", "FEEL GOOD VIBES", "Bollywood upbeat dance hits"),
            Triple("Chill & Lo-Fi Loft", "FOCUS & RELAXATION", "Chill lofi beats study sleep"),
            Triple("Workout Bangers", "PUMP UP YOUR ENERGY", "Workout gym high energy motivation songs"),
            Triple("Romance & Acoustic", "HEARTWARMING MELODIES", "Romantic acoustic love songs")
        )

        if (sections.size < 5) {
            val jobs = feedQueries.map { (title, subtitle, query) ->
                async {
                    runCatching {
                        val tracks = search(query, "songs")
                            .filterIsInstance<SearchResultModel.TrackResult>()
                            .map { it.track }
                            .distinctBy { it.videoId }
                        if (tracks.isNotEmpty()) HomeSection(title, tracks.take(12), subtitle) else null
                    }.getOrNull()
                }
            }

            jobs.forEach { deferred ->
                deferred.await()?.let { section ->
                    if (sections.none { it.title.equals(section.title, ignoreCase = true) }) {
                        sections.add(section)
                    }
                }
            }
        }

        // 4. Default guaranteed curated sections if network is restricted or initial load
        if (sections.isEmpty()) {
            sections.addAll(getGuaranteedCuratedSections())
        }

        sections.distinctBy { it.title }.take(10)
    }

    private fun getGuaranteedCuratedSections(): List<HomeSection> {
        return listOf(
            HomeSection(
                title = "Easy Mornings",
                subtitle = "PEPPY MUSIC TO START YOUR DAY",
                items = listOf(
                    TrackModel("kJQP7kiw5Fk", "Despacito", "Luis Fonsi, Daddy Yankee", "VIDA", "https://i.ytimg.com/vi/kJQP7kiw5Fk/hqdefault.jpg", 228),
                    TrackModel("JGwWNGJdvx8", "Shape of You", "Ed Sheeran", "÷ (Divide)", "https://i.ytimg.com/vi/JGwWNGJdvx8/hqdefault.jpg", 233),
                    TrackModel("0Vw-P9B_o34", "Kesariya", "Arijit Singh, Pritam", "Brahmastra", "https://i.ytimg.com/vi/0Vw-P9B_o34/hqdefault.jpg", 268),
                    TrackModel("fJ9rUzIMcZQ", "Bohemian Rhapsody", "Queen", "A Night at the Opera", "https://i.ytimg.com/vi/fJ9rUzIMcZQ/hqdefault.jpg", 359),
                    TrackModel("OPf0YbXqDm0", "Uptown Funk", "Mark Ronson ft. Bruno Mars", "Uptown Special", "https://i.ytimg.com/vi/OPf0YbXqDm0/hqdefault.jpg", 270),
                    TrackModel("4NRXx6U8ABQ", "Blinding Lights", "The Weeknd", "After Hours", "https://i.ytimg.com/vi/4NRXx6U8ABQ/hqdefault.jpg", 200)
                )
            ),
            HomeSection(
                title = "Rain Therapy 🍀 🌧️",
                subtitle = "FOR COZY DAYS AND ENDLESS CUPS OF TEA",
                items = listOf(
                    TrackModel("jfKfPfyJRdk", "lofi hip hop radio - beats to relax/study to", "Lofi Girl", "Lofi Beats", "https://i.ytimg.com/vi/jfKfPfyJRdk/hqdefault.jpg", 0),
                    TrackModel("2Vv-BfVoq4g", "Perfect", "Ed Sheeran", "÷ (Divide)", "https://i.ytimg.com/vi/2Vv-BfVoq4g/hqdefault.jpg", 263),
                    TrackModel("YQHsXMglC9A", "Hello", "Adele", "25", "https://i.ytimg.com/vi/YQHsXMglC9A/hqdefault.jpg", 295),
                    TrackModel("hT_nvWreIhg", "Counting Stars", "OneRepublic", "Native", "https://i.ytimg.com/vi/hT_nvWreIhg/hqdefault.jpg", 257),
                    TrackModel("RgKAFK5djSk", "See You Again", "Wiz Khalifa ft. Charlie Puth", "Furious 7", "https://i.ytimg.com/vi/RgKAFK5djSk/hqdefault.jpg", 229)
                )
            ),
            HomeSection(
                title = "Today's Biggest Hits",
                subtitle = "GLOBAL CHARTS",
                items = listOf(
                    TrackModel("CevxZvSJLk8", "Roar", "Katy Perry", "PRISM", "https://i.ytimg.com/vi/CevxZvSJLk8/hqdefault.jpg", 223),
                    TrackModel("k2qgadSvNyU", "New Rules", "Dua Lipa", "Dua Lipa", "https://i.ytimg.com/vi/k2qgadSvNyU/hqdefault.jpg", 209),
                    TrackModel("nfWlot6h_JM", "Shake It Off", "Taylor Swift", "1989", "https://i.ytimg.com/vi/nfWlot6h_JM/hqdefault.jpg", 242),
                    TrackModel("SlPhMPnQ58k", "Memories", "Maroon 5", "JORDI", "https://i.ytimg.com/vi/SlPhMPnQ58k/hqdefault.jpg", 189)
                )
            ),
            HomeSection(
                title = "Workout Bangers",
                subtitle = "HIGH VOLTAGE MOTIVATION",
                items = listOf(
                    TrackModel("7wtfhZwyrcc", "Believer", "Imagine Dragons", "Evolve", "https://i.ytimg.com/vi/7wtfhZwyrcc/hqdefault.jpg", 204),
                    TrackModel("fKopy74weus", "Thunder", "Imagine Dragons", "Evolve", "https://i.ytimg.com/vi/fKopy74weus/hqdefault.jpg", 187),
                    TrackModel("YVkUvmDQ3HY", "Without Me", "Eminem", "The Eminem Show", "https://i.ytimg.com/vi/YVkUvmDQ3HY/hqdefault.jpg", 290),
                    TrackModel("eVTXPUF4Oz4", "In The End", "Linkin Park", "Hybrid Theory", "https://i.ytimg.com/vi/eVTXPUF4Oz4/hqdefault.jpg", 216)
                )
            )
        )
    }

    private fun extractSectionsFromRoot(root: JsonObject, out: MutableList<HomeSection>) {
        val contents = root["contents"]?.jsonObject
        val tabs = contents?.get("tabbedBrowseResultsRenderer")?.jsonObject?.get("tabs")?.jsonArray
            ?: contents?.get("singleColumnBrowseResultsRenderer")?.jsonObject?.get("tabs")?.jsonArray
            ?: root["contents"]?.jsonObject?.entries?.firstOrNull()?.value?.jsonObject?.get("tabs")?.jsonArray

        val sectionArray = tabs?.getOrNull(0)?.jsonObject
            ?.get("tabRenderer")?.jsonObject?.get("content")?.jsonObject
            ?.get("sectionListRenderer")?.jsonObject?.get("contents")?.jsonArray
            ?: root["contents"]?.jsonObject?.get("sectionListRenderer")?.jsonObject?.get("contents")?.jsonArray
            ?: root["contents"]?.jsonArray

        sectionArray?.forEach { sectionElement ->
            val secObj = sectionElement.jsonObject
            parseCarouselSection(secObj)?.let { out.add(it) }
            parseMusicShelfSection(secObj)?.let { out.add(it) }
        }
    }

    override suspend fun getGenreTracks(genre: String): List<TrackModel> = runCatching {
        val q = when (genre.lowercase()) {
            "workout"    -> "workout gym high energy hits"
            "relax"      -> "relaxing acoustic peaceful music"
            "focus"      -> "focus study instrumental lofi"
            "chill"      -> "chill vibes songs"
            "party"      -> "party dance club hits"
            "romance"    -> "romantic love songs"
            "bollywood"  -> "bollywood top hits"
            "pop"        -> "pop radio hits"
            "hip hop"    -> "hip hop rap hits"
            "rock"       -> "rock hits classics"
            "electronic" -> "electronic dance edm hits"
            "jazz"       -> "jazz music essentials"
            "classical"  -> "classical music masterpieces"
            else         -> "$genre popular songs"
        }
        search(q, "songs").filterIsInstance<SearchResultModel.TrackResult>().map { it.track }.distinctBy { it.videoId }
    }.getOrDefault(emptyList())

    private fun parseCarouselSection(obj: JsonObject): HomeSection? {
        val carousel = obj["musicCarouselShelfRenderer"]?.jsonObject ?: return null
        val header = carousel["header"]?.jsonObject
        val basicHeader = header?.get("musicCarouselShelfBasicHeaderRenderer")?.jsonObject
            ?: header?.get("musicHeaderRenderer")?.jsonObject
        val title = basicHeader?.get("title")?.jsonObject?.get("runs")?.jsonArray
            ?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
            ?: basicHeader?.get("title")?.jsonObject?.get("simpleText")?.jsonPrimitive?.contentOrNull
            ?: return null
        val strapline = basicHeader?.get("strapline")?.jsonObject?.get("runs")?.jsonArray
            ?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull
            ?: basicHeader?.get("subheading")?.jsonObject?.get("runs")?.jsonArray
                ?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull

        val items = mutableListOf<TrackModel>()
        carousel["contents"]?.jsonArray?.forEach { content ->
            val obj2 = content.jsonObject
            // Two-row item (album/playlist/video tiles)
            obj2["musicTwoRowItemRenderer"]?.jsonObject?.let { r ->
                val t = r["title"]?.jsonObject?.get("runs")?.jsonArray
                    ?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: return@let
                val thumb = r["thumbnailRenderer"]?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject
                    ?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray
                    ?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull ?: ""
                val navEp = r["navigationEndpoint"]?.jsonObject
                val vid = navEp?.get("watchEndpoint")?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull
                    ?: navEp?.get("watchPlaylistEndpoint")?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull
                val sub = r["subtitle"]?.jsonObject?.get("runs")?.jsonArray
                    ?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: ""
                if (vid != null) items.add(TrackModel(vid, t, sub, "", thumb, 0L))
            }

            // Responsive list item
            obj2["musicResponsiveListItemRenderer"]?.jsonObject?.let { r ->
                val cols = r["flexColumns"]?.jsonArray
                val titleNode = cols?.getOrNull(0)?.jsonObject
                    ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                    ?.get("text")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject
                    ?: return@let
                val songTitle = titleNode["text"]?.jsonPrimitive?.contentOrNull ?: return@let
                val navEp = titleNode["navigationEndpoint"]?.jsonObject ?: r["navigationEndpoint"]?.jsonObject
                val watchId = r["playlistItemData"]?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull
                    ?: navEp?.get("watchEndpoint")?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull
                    ?: return@let
                val artist = cols.getOrNull(1)?.jsonObject
                    ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                    ?.get("text")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject
                    ?.get("text")?.jsonPrimitive?.contentOrNull ?: "YouTube Music"
                val thumb = r["thumbnail"]?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject
                    ?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject
                    ?.get("url")?.jsonPrimitive?.contentOrNull ?: ""
                items.add(TrackModel(watchId, songTitle, artist, "", thumb, 0L))
            }
        }

        return if (items.isNotEmpty()) HomeSection(title, items.distinctBy { it.videoId }, strapline) else null
    }

    private fun parseMusicShelfSection(obj: JsonObject): HomeSection? {
        val shelf = obj["musicShelfRenderer"]?.jsonObject ?: return null
        val title = shelf["title"]?.jsonObject?.get("runs")?.jsonArray
            ?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: "Songs"
        val items = mutableListOf<TrackModel>()
        shelf["contents"]?.jsonArray?.forEach { content ->
            val r = content.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject ?: return@forEach
            val cols = r["flexColumns"]?.jsonArray ?: return@forEach
            val c0 = cols.getOrNull(0)?.jsonObject?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
            val songTitle = c0?.get("text")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject
                ?.get("text")?.jsonPrimitive?.contentOrNull ?: return@forEach
            val navEp = c0.get("text")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject
                ?.get("navigationEndpoint")?.jsonObject ?: r["navigationEndpoint"]?.jsonObject
            val watchId = r["playlistItemData"]?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull
                ?: navEp?.get("watchEndpoint")?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull
                ?: return@forEach
            val c1 = cols.getOrNull(1)?.jsonObject?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
            val artist = c1?.get("text")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject
                ?.get("text")?.jsonPrimitive?.contentOrNull ?: "YouTube Music"
            val thumb = r["thumbnail"]?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject
                ?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject
                ?.get("url")?.jsonPrimitive?.contentOrNull ?: ""
            items.add(TrackModel(watchId, songTitle, artist, "", thumb, 0L))
        }
        return if (items.isNotEmpty()) HomeSection(title, items.distinctBy { it.videoId }) else null
    }

    override suspend fun search(query: String, filter: String?): List<SearchResultModel> = runCatching {
        sParse.parse(json.parseToJsonElement(req.search(query, filter).bodyAsText()))
    }.getOrDefault(emptyList())

    override suspend fun getStream(videoId: String): StreamDataModel? {
        // Strategy 1: ANDROID_TESTSUITE client (direct unthrottled audio streams)
        try {
            val r0 = req.player(videoId, ClientType.ANDROID_TESTSUITE).bodyAsText()
            val root0 = json.parseToJsonElement(r0)
            val stream0 = pParse.parse(videoId, root0)
            if (stream0 != null && stream0.streamUrl.isNotBlank()) {
                val resolved = cipher.resolveStreamUrl(stream0.streamUrl, IT.PLAYER)
                if (resolved.isNotBlank()) {
                    return stream0.copy(streamUrl = resolved)
                }
            }
        } catch (_: Exception) {}

        // Strategy 2: IOS client (returns direct m4a/aac audio streams)
        try {
            val r3 = req.player(videoId, ClientType.IOS).bodyAsText()
            val root3 = json.parseToJsonElement(r3)
            val stream3 = pParse.parse(videoId, root3)
            if (stream3 != null && stream3.streamUrl.isNotBlank()) {
                val resolved = cipher.resolveStreamUrl(stream3.streamUrl, IT.PLAYER)
                if (resolved.isNotBlank()) {
                    return stream3.copy(streamUrl = resolved)
                }
            }
        } catch (_: Exception) {}

        // Strategy 3: TVHTML5_SIMPLY_EMBEDDED client
        try {
            val rTv = req.player(videoId, ClientType.TVHTML5_SIMPLY_EMBEDDED).bodyAsText()
            val rootTv = json.parseToJsonElement(rTv)
            val streamTv = pParse.parse(videoId, rootTv)
            if (streamTv != null && streamTv.streamUrl.isNotBlank()) {
                val resolved = cipher.resolveStreamUrl(streamTv.streamUrl, IT.PLAYER)
                if (resolved.isNotBlank()) {
                    return streamTv.copy(streamUrl = resolved)
                }
            }
        } catch (_: Exception) {}

        // Strategy 4: ANDROID_MUSIC client
        try {
            val r1 = req.player(videoId, ClientType.ANDROID_MUSIC).bodyAsText()
            val root1 = json.parseToJsonElement(r1)
            val stream1 = pParse.parse(videoId, root1)
            if (stream1 != null && stream1.streamUrl.isNotBlank()) {
                val resolved = cipher.resolveStreamUrl(stream1.streamUrl, IT.PLAYER)
                if (resolved.isNotBlank()) {
                    return stream1.copy(streamUrl = resolved)
                }
            }
        } catch (_: Exception) {}

        // Strategy 5: ANDROID client (standard YouTube app)
        try {
            val r2 = req.player(videoId, ClientType.ANDROID).bodyAsText()
            val root2 = json.parseToJsonElement(r2)
            val stream2 = pParse.parse(videoId, root2)
            if (stream2 != null && stream2.streamUrl.isNotBlank()) {
                val resolved = cipher.resolveStreamUrl(stream2.streamUrl, IT.PLAYER)
                if (resolved.isNotBlank()) {
                    return stream2.copy(streamUrl = resolved)
                }
            }
        } catch (_: Exception) {}

        // Strategy 6: WEB_REMIX client
        try {
            val r4 = req.player(videoId, ClientType.WEB_REMIX).bodyAsText()
            val root4 = json.parseToJsonElement(r4)
            val stream4 = pParse.parse(videoId, root4)
            if (stream4 != null && stream4.streamUrl.isNotBlank()) {
                val resolved = cipher.resolveStreamUrl(stream4.streamUrl, IT.PLAYER)
                if (resolved.isNotBlank()) {
                    return stream4.copy(streamUrl = resolved)
                }
            }
        } catch (_: Exception) {}

        // Strategy 7: Piped / Invidious high-availability audio streams
        val pipedUrl = fetchPipedStream(videoId)
        if (!pipedUrl.isNullOrBlank()) {
            return StreamDataModel(
                videoId = videoId,
                streamUrl = pipedUrl,
                itag = 140,
                mimeType = "audio/mp4",
                bitrate = 128000L,
                contentLength = 0L,
                expiresInSeconds = 21600L,
                audioQuality = "AUDIO_QUALITY_MEDIUM"
            )
        }

        return null
    }

    private suspend fun fetchPipedStream(videoId: String): String? {
        val pipedEndpoints = listOf(
            "https://pipedapi.kavin.rocks/streams/$videoId",
            "https://api.piped.privacy.com.de/streams/$videoId",
            "https://piped-api.lunar.icu/streams/$videoId",
            "https://pipedapi.tokhmi.xyz/streams/$videoId",
            "https://invidious.nerdvpn.de/api/v1/videos/$videoId",
            "https://yt.drgnz.club/api/v1/videos/$videoId",
            "https://invidious.jing.rocks/api/v1/videos/$videoId",
            "https://inv.tux.pizza/api/v1/videos/$videoId"
        )
        for (endpoint in pipedEndpoints) {
            val res = runCatching {
                val text = http.get(endpoint).bodyAsText()
                val root = json.parseToJsonElement(text).jsonObject
                // Try Piped structure
                val audioArr = root["audioStreams"]?.jsonArray
                val firstPiped = audioArr?.firstOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull
                if (!firstPiped.isNullOrBlank()) return@runCatching firstPiped

                // Try Invidious adaptiveFormats
                val adaptive = root["adaptiveFormats"]?.jsonArray ?: root["formatStreams"]?.jsonArray
                val invAudio = adaptive?.mapNotNull { it.jsonObject }?.firstOrNull {
                    val mime = it["type"]?.jsonPrimitive?.contentOrNull ?: it["mimeType"]?.jsonPrimitive?.contentOrNull ?: ""
                    mime.startsWith("audio/") || mime.contains("audio")
                }
                invAudio?.get("url")?.jsonPrimitive?.contentOrNull
            }.getOrNull()
            if (!res.isNullOrBlank()) return res
        }
        return null
    }

    override suspend fun getAlbum(browseId: String) = runCatching {
        aParse.parse(browseId, json.parseToJsonElement(req.browse(browseId).bodyAsText()))
    }.getOrNull()

    override suspend fun getSuggestions(videoId: String): List<TrackModel> = getRadioTracks(videoId)

    override suspend fun getRadioTracks(videoId: String, seedTitle: String, seedArtist: String): List<TrackModel> = coroutineScope {
        val result = mutableListOf<TrackModel>()
        // 1. YouTube Music watch-next recommendations
        runCatching {
            val raw = req.next(videoId).bodyAsText()
            val root = json.parseToJsonElement(raw)
            val parsed = parseNextTracks(root)
            result.addAll(parsed)
        }

        // 2. Concurrently fetch artist songs / related tracks if needed
        if (result.size < 20 && seedArtist.isNotBlank() && !seedArtist.equals("Unknown", true)) {
            runCatching {
                val artistHits = search("$seedArtist songs", "songs")
                    .filterIsInstance<SearchResultModel.TrackResult>()
                    .map { it.track }
                result.addAll(artistHits)
            }
        }

        // 3. Fallback to trending hits so queue is never empty
        if (result.size < 20) {
            runCatching {
                val trendingHits = search("Top Global Hits", "songs")
                    .filterIsInstance<SearchResultModel.TrackResult>()
                    .map { it.track }
                result.addAll(trendingHits)
            }
        }

        result.distinctBy { it.videoId }.filter { it.videoId != videoId }
    }

    private fun parseNextTracks(root: JsonElement): List<TrackModel> = runCatching {
        val tracks = mutableListOf<TrackModel>()
        val contents = root.obj("contents")

        // 1. Queue playlistPanelRenderer
        val panel = contents?.obj("singleColumnMusicWatchNextResultsRenderer")
            ?.obj("tabbedRenderer")?.obj("watchNextTabbedResultsRenderer")
            ?.arr("tabs")?.idx(0)?.obj("tabRenderer")?.obj("content")
            ?.obj("musicQueueRenderer")?.obj("content")?.obj("playlistPanelRenderer")
            ?: contents?.obj("musicQueueRenderer")?.obj("content")?.obj("playlistPanelRenderer")

        panel?.arr("contents")?.forEach { item ->
            val r = item.obj("playlistPanelVideoRenderer")
                ?: item.obj("playlistPanelVideoWrapperRenderer")?.obj("primaryRenderer")?.obj("playlistPanelVideoRenderer")
                ?: return@forEach
            val vid = r.str("videoId")
                ?: r.obj("navigationEndpoint")?.obj("watchEndpoint")?.str("videoId") ?: return@forEach
            val title = r.obj("title")?.runsText() ?: r.obj("title")?.str("simpleText") ?: return@forEach
            val artist = r.obj("longBylineText")?.runsText()
                ?: r.obj("shortBylineText")?.runsText()
                ?: r.obj("bylineText")?.runsText() ?: "YouTube Music"
            val thumb = r.obj("thumbnail")?.bestThumbnailUrl()
                ?: r.obj("thumbnail")?.obj("thumbnails")?.bestThumbnailUrl() ?: ""
            val lenStr = r.obj("lengthText")?.runsText() ?: "0:00"
            val sec = runCatching {
                val p = lenStr.trim().split(":").map { it.toLong() }
                when (p.size) { 2 -> p[0]*60+p[1]; 3 -> p[0]*3600+p[1]*60+p[2]; else -> 0L }
            }.getOrDefault(0L)
            tracks.add(TrackModel(vid, title, artist, "", thumb, sec))
        }

        // 2. Also check if there are responsive list items or carousels in watch next
        if (tracks.isEmpty()) {
            val results = sParse.parse(root)
            tracks.addAll(results.filterIsInstance<SearchResultModel.TrackResult>().map { it.track })
        }

        tracks.distinctBy { it.videoId }
    }.getOrDefault(emptyList())

    override suspend fun getSearchSuggestions(q: String) = runCatching {
        val body = json.parseToJsonElement(req.suggestions(q).bodyAsText())
        body.arr("contents")?.flatMap { s ->
            s.obj("searchSuggestionsSectionRenderer")?.arr("contents")
                ?.mapNotNull { it.obj("searchSuggestionRenderer")?.obj("suggestion")?.runsText() }
                ?: emptyList()
        } ?: emptyList()
    }.getOrDefault(emptyList())

    override suspend fun getPlaylist(playlistId: String): Pair<String, List<TrackModel>> = runCatching {
        val browseId = if (playlistId.startsWith("VL") || playlistId.startsWith("FE")) playlistId else "VL$playlistId"
        val raw = req.browse(browseId).bodyAsText()
        val root = json.parseToJsonElement(raw)

        val title = root.obj("header")?.obj("musicDetailHeaderRenderer")?.obj("title")?.runsText()
            ?: root.obj("header")?.obj("musicEditablePlaylistDetailHeaderRenderer")?.obj("header")
                ?.obj("musicDetailHeaderRenderer")?.obj("title")?.runsText()
            ?: root.obj("contents")?.obj("singleColumnBrowseResultsRenderer")?.arr("tabs")?.idx(0)
                ?.obj("tabRenderer")?.obj("content")?.obj("sectionListRenderer")?.arr("contents")?.idx(0)
                ?.obj("musicResponsiveHeaderRenderer")?.obj("title")?.runsText()
            ?: "Imported Playlist"

        val tracks = mutableListOf<TrackModel>()
        val tabContent = root.obj("contents")?.obj("singleColumnBrowseResultsRenderer")
            ?.arr("tabs")?.idx(0)?.obj("tabRenderer")?.obj("content")
        val sections = tabContent?.obj("sectionListRenderer")?.arr("contents")
            ?: root.arr("contents") ?: emptyList()

        for (section in sections) {
            val shelf = section.obj("musicPlaylistShelfRenderer")
                ?: section.obj("musicShelfRenderer") ?: continue
            val contents = shelf.arr("contents") ?: continue
            for (item in contents) {
                val r = item.obj("musicResponsiveListItemRenderer") ?: continue
                val cols = r.arr("flexColumns") ?: continue
                val c0 = cols.idx(0)?.obj("musicResponsiveListItemFlexColumnRenderer")
                val c1 = cols.idx(1)?.obj("musicResponsiveListItemFlexColumnRenderer")
                val songTitle = c0?.obj("text")?.runsText() ?: continue
                val runs0 = c0.obj("text")?.arr("runs")
                val navEp = runs0?.idx(0)?.obj("navigationEndpoint")
                val vid = r.obj("playlistItemData")?.str("videoId")
                    ?: navEp?.obj("watchEndpoint")?.str("videoId") ?: continue
                val artist = c1?.obj("text")?.arr("runs")?.idx(0)?.asStr() ?: "Unknown"
                val album = c1?.obj("text")?.arr("runs")?.idx(2)?.asStr() ?: ""
                val durStr = c1?.obj("text")?.arr("runs")?.lastOrNull()?.asStr() ?: "0:00"
                val thumb = r.obj("thumbnail")?.obj("musicThumbnailRenderer")
                    ?.obj("thumbnail")?.bestThumbnailUrl() ?: ""
                val durSec = runCatching {
                    val p = durStr.split(":").map { it.toLong() }
                    when (p.size) { 2 -> p[0]*60+p[1]; 3 -> p[0]*3600+p[1]*60+p[2]; else -> 0L }
                }.getOrDefault(0L)
                tracks.add(TrackModel(vid, songTitle, artist, album, thumb, durSec))
            }
        }
        Pair(title, tracks)
    }.getOrDefault(Pair("Imported Playlist", emptyList()))

    override suspend fun getLikedSongs(): List<TrackModel> = runCatching {
        val res = getPlaylist("LM")
        if (res.second.isNotEmpty()) res.second else getPlaylist("FEmusic_liked_videos").second
    }.getOrDefault(emptyList())

    private fun cleanSongQuery(raw: String): String {
        return raw.replace(Regex("""(?i)\(.*?official.*?\)|\[.*?official.*?\]|\(.*?video.*?\)|\[.*?video.*?\]|\(.*?lyrics?.*?\)|\[.*?lyrics?.*?\]|\(.*?audio.*?\)|\[.*?audio.*?\]|\(.*?feat.*?\)|\[.*?feat.*?\]|\bfeat\..*|\bft\..*"""), "")
            .replace(Regex("""[|/\\_\-#]"""), " ")
            .replace(Regex("""\s+"""), " ")
            .trim()
    }

    override suspend fun getLyrics(track: TrackModel): List<LrcLine> = searchLyricsOnline(track, null)

    override suspend fun searchLyricsOnline(track: TrackModel, customQuery: String?): List<LrcLine> = runCatching {
        val cleanTitle = cleanSongQuery(if (!customQuery.isNullOrBlank()) customQuery else track.title)
        val cleanArtist = cleanSongQuery(track.artistName).replace("YouTube Music", "").trim()
        val duration = track.durationSeconds

        // 1. LRCLIB exact get
        if (cleanTitle.isNotBlank() && cleanArtist.isNotBlank()) {
            val directRes = runCatching {
                val response = req.fetchLrcLyrics(cleanTitle, cleanArtist, duration).bodyAsText()
                val lrcJson = json.parseToJsonElement(response).jsonObject
                val syncedLyrics = lrcJson["syncedLyrics"]?.jsonPrimitive?.contentOrNull
                if (!syncedLyrics.isNullOrBlank()) parseLrc(syncedLyrics)
                else {
                    val plainLyrics = lrcJson["plainLyrics"]?.jsonPrimitive?.contentOrNull
                    if (!plainLyrics.isNullOrBlank()) interpolatePlainLyrics(plainLyrics, duration) else emptyList()
                }
            }.getOrDefault(emptyList())

            if (directRes.isNotEmpty()) return@runCatching directRes
        }

        // 2. LRCLIB search by query
        val searchQueries = listOf(
            "$cleanTitle $cleanArtist".trim(),
            cleanTitle,
            "${track.title} ${track.artistName}".trim()
        ).distinct().filter { it.isNotBlank() }

        for (q in searchQueries) {
            val searchRes = runCatching {
                val response = req.searchLrcLyrics(q).bodyAsText()
                val list = json.parseToJsonElement(response).jsonArray
                for (item in list) {
                    val obj = item.jsonObject
                    val synced = obj["syncedLyrics"]?.jsonPrimitive?.contentOrNull
                    if (!synced.isNullOrBlank()) {
                        val parsed = parseLrc(synced)
                        if (parsed.isNotEmpty()) return@runCatching parsed
                    }
                }
                // If no synced in search, try plain
                for (item in list) {
                    val obj = item.jsonObject
                    val plain = obj["plainLyrics"]?.jsonPrimitive?.contentOrNull
                    if (!plain.isNullOrBlank()) {
                        return@runCatching interpolatePlainLyrics(plain, duration)
                    }
                }
                emptyList()
            }.getOrDefault(emptyList())

            if (searchRes.isNotEmpty()) return@runCatching searchRes
        }

        // 3. InnerTube YouTube Music /next lyrics tab
        if (track.videoId.isNotBlank()) {
            val ytmLyrics = runCatching {
                val nextRaw = req.next(track.videoId).bodyAsText()
                val nextRoot = json.parseToJsonElement(nextRaw)
                val tabs = nextRoot.obj("contents")
                    ?.obj("singleColumnMusicWatchNextResultsRenderer")
                    ?.obj("tabbedRenderer")?.obj("watchNextTabbedResultsRenderer")
                    ?.arr("tabs")

                val lyricsTab = tabs?.mapNotNull { it.obj("tabRenderer") }?.firstOrNull { tab ->
                    tab.str("title")?.equals("Lyrics", ignoreCase = true) == true
                }

                val lyricsBrowseId = lyricsTab?.obj("endpoint")?.obj("browseEndpoint")?.str("browseId")
                if (lyricsBrowseId != null) {
                    val lyricsRaw = req.browse(lyricsBrowseId).bodyAsText()
                    val lyricsRoot = json.parseToJsonElement(lyricsRaw)
                    val descriptionText = lyricsRoot.obj("contents")
                        ?.obj("sectionListRenderer")?.arr("contents")?.idx(0)
                        ?.obj("musicDescriptionShelfRenderer")?.obj("description")?.runsText()

                    if (!descriptionText.isNullOrBlank()) {
                        return@runCatching interpolatePlainLyrics(descriptionText, duration)
                    }
                }
                emptyList()
            }.getOrDefault(emptyList())

            if (ytmLyrics.isNotEmpty()) return@runCatching ytmLyrics
        }

        // 4. Lyrics.ovh API fallback
        if (cleanArtist.isNotBlank() && cleanTitle.isNotBlank()) {
            val ovhLyrics = runCatching {
                val resText = req.fetchOvhLyrics(cleanArtist, cleanTitle).bodyAsText()
                val parsed = json.parseToJsonElement(resText).jsonObject
                val lyricsStr = parsed["lyrics"]?.jsonPrimitive?.contentOrNull
                if (!lyricsStr.isNullOrBlank()) {
                    interpolatePlainLyrics(lyricsStr, duration)
                } else emptyList()
            }.getOrDefault(emptyList())

            if (ovhLyrics.isNotEmpty()) return@runCatching ovhLyrics
        }

        emptyList()
    }.getOrDefault(emptyList())

    private fun interpolatePlainLyrics(raw: String, durationSec: Long): List<LrcLine> {
        val filteredLines = raw.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (filteredLines.isEmpty()) return emptyList()
        val totalMs = if (durationSec > 0) durationSec * 1000L else (filteredLines.size * 4000L)
        val stepMs = (totalMs / (filteredLines.size + 1)).coerceAtLeast(2500L)
        return filteredLines.mapIndexed { idx, line ->
            LrcLine(idx * stepMs, line)
        }
    }
}

@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository
}
