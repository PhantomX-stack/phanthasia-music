package com.phantasia.music.network

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton
import com.phantasia.music.network.bestThumbnailUrl

data class HomeSection(val title: String, val items: List<TrackModel>)

interface MusicRepository {
    suspend fun getHomeSections(): List<HomeSection>
    suspend fun search(query: String): List<SearchResultModel>
    suspend fun getStream(videoId: String): StreamDataModel?
    suspend fun getAlbum(browseId: String): AlbumModel?
    suspend fun getSuggestions(videoId: String): List<TrackModel>
    suspend fun getSearchSuggestions(query: String): List<String>
}

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val req:    InnerTubeRequests,
    private val sParse: SearchParser,
    private val pParse: PlayerParser,
    private val aParse: AlbumParser,
    private val cipher: CipherEngine
) : MusicRepository {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    override suspend fun getHomeSections(): List<HomeSection> = runCatching {
        val raw  = req.home().bodyAsText()
        val root = json.parseToJsonElement(raw).jsonObject
        val sections = mutableListOf<HomeSection>()

        // Strategy 1: singleColumnBrowseResultsRenderer (WEB_REMIX logged-in)
        val singleCol = root["contents"]?.jsonObject
            ?.get("singleColumnBrowseResultsRenderer")?.jsonObject
            ?.get("tabs")?.jsonArray?.getOrNull(0)?.jsonObject
            ?.get("tabRenderer")?.jsonObject?.get("content")?.jsonObject
            ?.get("sectionListRenderer")?.jsonObject?.get("contents")?.jsonArray

        singleCol?.forEach { section ->
            parseCarouselSection(section.jsonObject)?.let { sections.add(it) }
        }

        // Strategy 2: tabbedBrowseResultsRenderer fallback (anonymous)
        if (sections.isEmpty()) {
            val tabbed = root["contents"]?.jsonObject
                ?.get("tabbedBrowseResultsRenderer")?.jsonObject
                ?.get("tabs")?.jsonArray?.getOrNull(0)?.jsonObject
                ?.get("tabRenderer")?.jsonObject?.get("content")?.jsonObject
                ?.get("sectionListRenderer")?.jsonObject?.get("contents")?.jsonArray

            tabbed?.forEach { section ->
                parseCarouselSection(section.jsonObject)?.let { sections.add(it) }
            }
        }

        // Strategy 3: Direct contents array
        if (sections.isEmpty()) {
            root["contents"]?.jsonObject?.entries?.firstOrNull()?.value?.jsonObject
                ?.get("tabs")?.jsonArray?.getOrNull(0)?.jsonObject
                ?.get("tabRenderer")?.jsonObject?.get("content")?.jsonObject
                ?.get("sectionListRenderer")?.jsonObject?.get("contents")?.jsonArray
                ?.forEach { section ->
                    parseCarouselSection(section.jsonObject)?.let { sections.add(it) }
                }
        }

        sections.take(8)
    }.onFailure { it.printStackTrace() }.getOrDefault(emptyList())

    private fun parseCarouselSection(obj: JsonObject): HomeSection? {
        val carousel = obj["musicCarouselShelfRenderer"]?.jsonObject ?: return null
        val title    = carousel["header"]?.jsonObject
            ?.get("musicCarouselShelfBasicHeaderRenderer")?.jsonObject
            ?.get("title")?.jsonObject?.get("runs")?.jsonArray
            ?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: return null

        val items = mutableListOf<TrackModel>()
        carousel["contents"]?.jsonArray?.forEach inner@{ content ->
            val obj2 = content.jsonObject

            // Two-row item (album/playlist tiles)
            obj2["musicTwoRowItemRenderer"]?.jsonObject?.let { r ->
                val t = r["title"]?.jsonObject?.get("runs")?.jsonArray
                    ?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: return@let
                val thumb = r["thumbnailRenderer"]?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject
                    ?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray
                    ?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull ?: ""
                val vid = r["navigationEndpoint"]?.jsonObject
                    ?.get("watchEndpoint")?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull
                val sub = r["subtitle"]?.jsonObject?.get("runs")?.jsonArray
                    ?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: ""
                if (vid != null) items.add(TrackModel(vid, t, sub, "", thumb, 0L))
            }

            // Responsive list item
            obj2["musicResponsiveListItemRenderer"]?.jsonObject?.let { r ->
                val titleNode = r["flexColumns"]?.jsonArray?.getOrNull(0)?.jsonObject
                    ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                    ?.get("text")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject
                    ?: return@let
                val songTitle = titleNode["text"]?.jsonPrimitive?.contentOrNull ?: return@let
                val watchId = r["playlistItemData"]?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull
                    ?: titleNode["navigationEndpoint"]?.jsonObject?.get("watchEndpoint")?.jsonObject
                        ?.get("videoId")?.jsonPrimitive?.contentOrNull ?: return@let
                val artist = r["flexColumns"]?.jsonArray?.getOrNull(1)?.jsonObject
                    ?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
                    ?.get("text")?.jsonObject?.get("runs")?.jsonArray?.getOrNull(0)?.jsonObject
                    ?.get("text")?.jsonPrimitive?.contentOrNull ?: "Unknown"
                val thumb = r["thumbnail"]?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject
                    ?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray?.lastOrNull()?.jsonObject
                    ?.get("url")?.jsonPrimitive?.contentOrNull ?: ""
                items.add(TrackModel(watchId, songTitle, artist, "", thumb, 0L))
            }
        }

        return if (items.isNotEmpty()) HomeSection(title, items) else null
    }

    override suspend fun search(q: String) = runCatching {
        sParse.parse(json.parseToJsonElement(req.search(q).bodyAsText()))
    }.getOrDefault(emptyList())

    override suspend fun getStream(videoId: String) = runCatching {
        val root = json.parseToJsonElement(req.player(videoId).bodyAsText())
        val raw  = pParse.parse(videoId, root) ?: return@runCatching null
        raw.copy(streamUrl = cipher.resolveStreamUrl(raw.streamUrl, IT.PLAYER))
    }.getOrNull()

    override suspend fun getAlbum(browseId: String) = runCatching {
        aParse.parse(browseId, json.parseToJsonElement(req.browse(browseId).bodyAsText()))
    }.getOrNull()

    override suspend fun getSuggestions(videoId: String) = runCatching {
        sParse.parse(json.parseToJsonElement(req.next(videoId).bodyAsText()))
            .filterIsInstance<SearchResultModel.TrackResult>().map { it.track }
    }.getOrDefault(emptyList())

    override suspend fun getSearchSuggestions(q: String) = runCatching {
        val body = json.parseToJsonElement(req.suggestions(q).bodyAsText())
        body.arr("contents")?.flatMap { s ->
            s.obj("searchSuggestionsSectionRenderer")?.arr("contents")
                ?.mapNotNull { it.obj("searchSuggestionRenderer")?.obj("suggestion")?.runsText() }
                ?: emptyList()
        } ?: emptyList()
    }.getOrDefault(emptyList())
}

@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository
}
