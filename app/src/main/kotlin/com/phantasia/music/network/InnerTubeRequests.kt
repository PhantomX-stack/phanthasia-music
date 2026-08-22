package com.phantasia.music.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InnerTubeRequests @Inject constructor(
    @InnerTubeHttp private val client: HttpClient,
    private val locale: InnerTubeLocale,
    private val visitorSessionManager: VisitorSessionManager
) {
    var ytmCookie: String? = null

    suspend fun home(continuation: String? = null): HttpResponse {
        visitorSessionManager.ensureWarm()
        return browse("FEmusic_home", cont = continuation)
    }

    suspend fun charts(): HttpResponse {
        visitorSessionManager.ensureWarm()
        return browse("FEmusic_charts")
    }

    suspend fun explore(): HttpResponse {
        visitorSessionManager.ensureWarm()
        return browse("FEmusic_explore")
    }

    suspend fun search(q: String, filter: String? = null, cont: String? = null): HttpResponse {
        visitorSessionManager.ensureWarm()
        val cookieToUse = ytmCookie ?: visitorSessionManager.getSessionCookie()
        return client.post("${IT.BASE}/search") {
            contentType(ContentType.Application.Json)
            cookieToUse?.let { header("Cookie", it) }
            setBody(buildContext(locale, ClientType.ANDROID_MUSIC).apply {
                put("query", q)
                val filterParam = when (filter?.lowercase()) {
                    "songs", "song"       -> "EgWKAQIIAWoKEAMQBBAJEAoQBQ=="
                    "videos", "video"     -> "EgWKAQIQAWoKEAMQBBAJEAoQBQ=="
                    "albums", "album"     -> "EgWKAQIBAWoKEAMQBBAJEAoQBQ=="
                    "artists", "artist"   -> "EgWKAQIgAWoKEAMQBBAJEAoQBQ=="
                    "playlists", "playlist"-> "EgWKAQIoAWoKEAMQBBAJEAoQBQ=="
                    else                  -> null
                }
                if (filterParam != null) put("params", filterParam)
                if (cont != null) put("continuation", cont)
            })
        }
    }

    suspend fun player(videoId: String, clientType: ClientType = ClientType.ANDROID_MUSIC): HttpResponse {
        visitorSessionManager.ensureWarm()
        val endpoint = if (clientType == ClientType.ANDROID || clientType == ClientType.IOS || clientType == ClientType.ANDROID_TESTSUITE || clientType == ClientType.TVHTML5_SIMPLY_EMBEDDED || clientType == ClientType.ANDROID_VR || clientType == ClientType.WEB_EMBEDDED) {
            "${IT.YT_BASE}/player"
        } else {
            "${IT.BASE}/player"
        }
        val contextMap = buildContext(locale, clientType)
        val clientMap = (contextMap["context"] as? Map<*, *>)?.get("client") as? Map<*, *>
        val clientUa = clientMap?.get("userAgent") as? String ?: IT.UA
        val cookieToUse = ytmCookie ?: visitorSessionManager.getSessionCookie()

        return client.post(endpoint) {
            contentType(ContentType.Application.Json)
            header("User-Agent", clientUa)
            cookieToUse?.let { header("Cookie", it) }
            setBody(contextMap.apply {
                put("videoId", videoId)
                put("playbackContext", mapOf("contentPlaybackContext" to
                    mapOf("signatureTimestamp" to 19800, "html5Preference" to "HTML5_PREF_WANTS")))
                put("racyCheckOk", true)
                put("contentCheckOk", true)
            })
        }
    }

    suspend fun browse(browseId: String, cont: String? = null, clientType: ClientType = ClientType.ANDROID_MUSIC): HttpResponse {
        visitorSessionManager.ensureWarm()
        val cookieToUse = ytmCookie ?: visitorSessionManager.getSessionCookie()
        return client.post("${IT.BASE}/browse") {
            contentType(ContentType.Application.Json)
            cookieToUse?.let { header("Cookie", it) }
            setBody(buildContext(locale, clientType).apply {
                put("browseId", browseId)
                if (cont != null) put("continuation", cont)
            })
        }
    }

    suspend fun next(videoId: String, cont: String? = null): HttpResponse {
        visitorSessionManager.ensureWarm()
        val cookieToUse = ytmCookie ?: visitorSessionManager.getSessionCookie()
        return client.post("${IT.BASE}/next") {
            contentType(ContentType.Application.Json)
            cookieToUse?.let { header("Cookie", it) }
            setBody(buildContext(locale, ClientType.ANDROID_MUSIC).apply {
                put("videoId", videoId)
                put("isAudioOnly", true)
                if (cont != null) put("continuation", cont)
            })
        }
    }

    suspend fun suggestions(q: String): HttpResponse {
        visitorSessionManager.ensureWarm()
        val cookieToUse = ytmCookie ?: visitorSessionManager.getSessionCookie()
        return client.post("${IT.BASE}/music/get_search_suggestions") {
            contentType(ContentType.Application.Json)
            cookieToUse?.let { header("Cookie", it) }
            setBody(buildContext(locale, ClientType.ANDROID_MUSIC).apply { put("input", q) })
        }
    }

    suspend fun fetchLrcLyrics(trackName: String, artistName: String, durationSec: Long = 0L): HttpResponse =
        client.get("https://lrclib.net/api/get") {
            header("User-Agent", "PhantasiaMusic/2.0 (https://github.com/phantasia/music)")
            url {
                parameters.append("track_name", trackName)
                parameters.append("artist_name", artistName)
                if (durationSec > 0) parameters.append("duration", durationSec.toString())
            }
        }

    suspend fun searchLrcLyrics(query: String): HttpResponse =
        client.get("https://lrclib.net/api/search") {
            header("User-Agent", "PhantasiaMusic/2.0 (https://github.com/phantasia/music)")
            url {
                parameters.append("q", query)
            }
        }

    suspend fun fetchOvhLyrics(artist: String, title: String): HttpResponse =
        client.get("https://api.lyrics.ovh/v1/$artist/$title")
}
