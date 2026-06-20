package com.phantasia.music.network

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

interface MusicRepository {
    suspend fun search(query: String): List<SearchResultModel>
    suspend fun getStream(videoId: String): StreamDataModel?
    suspend fun getAlbum(browseId: String): AlbumModel?
    suspend fun getSuggestions(videoId: String): List<TrackModel>
    suspend fun getSearchSuggestions(query: String): List<String>
}

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val req:     InnerTubeRequests,
    private val sParse:  SearchParser,
    private val pParse:  PlayerParser,
    private val aParse:  AlbumParser,
    private val cipher:  CipherEngine
) : MusicRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    override suspend fun search(query: String) = runCatching {
        sParse.parse(json.parseToJsonElement(req.search(query).bodyAsText()))
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

    override suspend fun getSearchSuggestions(query: String) = runCatching {
        val body = json.parseToJsonElement(req.suggestions(query).bodyAsText())
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
