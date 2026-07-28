package com.phantasia.music.network

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
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
        val root = json.parseToJsonElement(req.home().bodyAsText())
        val tabs = root.obj("contents")?.obj("singleColumnBrowseResultsRenderer")?.arr("tabs")
        val content = tabs?.idx(0)?.obj("tabRenderer")?.obj("content")?.obj("sectionListRenderer")?.arr("contents")
            ?: return emptyList()

        content.mapNotNull { section ->
            val carousel = section.obj("musicCarouselShelfRenderer") ?: return@mapNotNull null
            val title = carousel.obj("header")?.obj("musicCarouselShelfBasicHeaderRenderer")
                ?.obj("title")?.arr("runs")?.idx(0)?.str("text") ?: "Recommended"

            val items = carousel.arr("contents")?.mapNotNull { item ->
                val r = item.obj("musicTwoRowItemRenderer") ?: item.obj("musicResponsiveListItemRenderer") ?: return@mapNotNull null
                val titleNode = r.obj("title")?.arr("runs")?.idx(0) ?: r.obj("flexColumns")?.arr("runs")?.idx(0) ?: return@mapNotNull null
                val songTitle = titleNode.str("text") ?: return@mapNotNull null

                val watchId = r.obj("navigationEndpoint")?.obj("watchEndpoint")?.str("videoId")
                    ?: titleNode.obj("navigationEndpoint")?.obj("watchEndpoint")?.str("videoId")
                    ?: return@mapNotNull null

                val flex1 = r.obj("flexColumns")?.arr("flexColumns")?.idx(1)?.obj("musicResponsiveListItemFlexColumnRenderer"); val subtitleRuns = r.obj("subtitle")?.arr("runs") ?: flex1?.obj("text")?.arr("runs")
                val artist = subtitleRuns?.firstOrNull()?.str("text") ?: "Unknown"

                val thumbNode = (r.obj("thumbnailRenderer")?.obj("musicThumbnailRenderer")?.obj("thumbnail") ?: r.obj("thumbnail")?.obj("musicThumbnailRenderer")?.obj("thumbnail"))
                val thumb = thumbNode?.arr("thumbnails")?.lastOrNull()?.str("url") ?: ""

                TrackModel(watchId, songTitle, artist, "", thumb, 0L)
            } ?: emptyList()

            if (items.isEmpty()) null else HomeSection(title, items)
        }.take(8)
    }.getOrDefault(emptyList())

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
