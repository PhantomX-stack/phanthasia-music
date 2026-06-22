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
    suspend fun search(q: String, cont: String? = null): HttpResponse =
        client.post("${IT.BASE}/search") {
            contentType(ContentType.Application.Json)
            setBody(buildContext(locale).apply {
                put("query", q); put("params", "EgWKAQIIAWoKEAMQBBAJEAoQBQ==")
                if (cont != null) put("continuation", cont)
            })
        }

    suspend fun player(videoId: String): HttpResponse =
        client.post("${IT.BASE}/player") {
            contentType(ContentType.Application.Json)
            setBody(buildContext(locale).apply {
                put("videoId", videoId)
                put("playbackContext", mapOf("contentPlaybackContext" to
                    mapOf("signatureTimestamp" to 0, "html5Preference" to "HTML5_PREF_WANTS")))
                put("racyCheckOk", true); put("contentCheckOk", true)
            })
        }

    suspend fun browse(browseId: String, cont: String? = null): HttpResponse =
        client.post("${IT.BASE}/browse") {
            contentType(ContentType.Application.Json)
            setBody(buildContext(locale).apply {
                put("browseId", browseId)
                if (cont != null) put("continuation", cont)
            })
        }

    suspend fun next(videoId: String, cont: String? = null): HttpResponse =
        client.post("${IT.BASE}/next") {
            contentType(ContentType.Application.Json)
            setBody(buildContext(locale).apply {
                put("videoId", videoId); put("isAudioOnly", true)
                if (cont != null) put("continuation", cont)
            })
        }

    suspend fun suggestions(q: String): HttpResponse =
        client.post("${IT.BASE}/music/get_search_suggestions") {
            contentType(ContentType.Application.Json)
            setBody(buildContext(locale).apply { put("input", q) })
        }
}
