package com.phantasia.music.network

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InnerTubeRequests @Inject constructor(
    private val client: HttpClient,
    private val locale: InnerTubeLocale
) {
    suspend fun search(query: String, cont: String? = null): HttpResponse =
        client.post("${IT.BASE}/search") {
            contentType(ContentType.Application.Json)
            setBody(buildContextPayload(locale).apply {
                put("query", query); put("params", "EgWKAQIIAWoKEAMQBBAJEAoQBQ==")
                if (cont != null) put("continuation", cont)
            })
        }

    suspend fun player(videoId: String): HttpResponse =
        client.post("${IT.BASE}/player") {
            contentType(ContentType.Application.Json)
            setBody(buildContextPayload(locale).apply {
                put("videoId", videoId)
                put("playbackContext", mapOf("contentPlaybackContext" to
                    mapOf("signatureTimestamp" to 0, "html5Preference" to "HTML5_PREF_WANTS")))
                put("racyCheckOk", true); put("contentCheckOk", true)
            })
        }

    suspend fun browse(browseId: String, cont: String? = null): HttpResponse =
        client.post("${IT.BASE}/browse") {
            contentType(ContentType.Application.Json)
            setBody(buildContextPayload(locale).apply {
                put("browseId", browseId)
                if (cont != null) put("continuation", cont)
            })
        }

    suspend fun next(videoId: String, cont: String? = null): HttpResponse =
        client.post("${IT.BASE}/next") {
            contentType(ContentType.Application.Json)
            setBody(buildContextPayload(locale).apply {
                put("videoId", videoId); put("isAudioOnly", true)
                if (cont != null) put("continuation", cont)
            })
        }

    suspend fun suggestions(query: String): HttpResponse =
        client.post("${IT.BASE}/music/get_search_suggestions") {
            contentType(ContentType.Application.Json)
            setBody(buildContextPayload(locale).apply { put("input", query) })
        }
}
