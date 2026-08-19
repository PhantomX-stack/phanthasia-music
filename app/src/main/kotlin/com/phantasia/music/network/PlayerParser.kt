package com.phantasia.music.network

import kotlinx.serialization.json.JsonElement
import java.net.URLDecoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerParser @Inject constructor() {
    private val AUDIO_PRIO = listOf(140, 251, 250, 249, 139, 171, 141)
    private val VIDEO_PRIO = listOf(18, 22, 136, 137)

    fun parse(videoId: String, root: JsonElement): StreamDataModel? = runCatching {
        val sd = root.obj("streamingData") ?: return null
        val adaptive = sd.arr("adaptiveFormats") ?: emptyList()
        val direct = sd.arr("formats") ?: emptyList()
        val allFormats = (adaptive + direct)
        if (allFormats.isEmpty()) return null

        data class ParsedFormat(
            val itag: Int,
            val url: String,
            val mimeType: String,
            val bitrate: Long,
            val contentLength: Long,
            val durationSec: Long,
            val audioQuality: String,
            val isAudio: Boolean
        )

        val parsedList = allFormats.mapNotNull { f ->
            val itag = f.int("itag") ?: return@mapNotNull null
            val mime = f.str("mimeType") ?: ""
            val isAudio = mime.startsWith("audio/") || itag in AUDIO_PRIO

            var resolvedUrl = f.str("url")
            if (resolvedUrl.isNullOrBlank()) {
                val cipherStr = f.str("signatureCipher") ?: f.str("cipher")
                if (!cipherStr.isNullOrBlank()) {
                    val params = cipherStr.split("&").associate { param ->
                        val parts = param.split("=", limit = 2)
                        val k = parts.getOrNull(0)?.let { runCatching { URLDecoder.decode(it, "UTF-8") }.getOrDefault(it) } ?: ""
                        val v = parts.getOrNull(1)?.let { runCatching { URLDecoder.decode(it, "UTF-8") }.getOrDefault(it) } ?: ""
                        k to v
                    }
                    val baseUrl = params["url"]
                    val s = params["s"]
                    val sp = params["sp"] ?: "sig"
                    if (!baseUrl.isNullOrBlank()) {
                        resolvedUrl = if (!s.isNullOrBlank()) {
                            if (baseUrl.contains("?")) "$baseUrl&$sp=$s" else "$baseUrl?$sp=$s"
                        } else {
                            baseUrl
                        }
                    }
                }
            }

            if (resolvedUrl.isNullOrBlank()) return@mapNotNull null

            ParsedFormat(
                itag = itag,
                url = resolvedUrl,
                mimeType = mime,
                bitrate = f.long("bitrate") ?: 0L,
                contentLength = f.str("contentLength")?.toLongOrNull() ?: 0L,
                durationSec = f.str("approxDurationMs")?.toLongOrNull()?.div(1000) ?: 180L,
                audioQuality = f.str("audioQuality") ?: "",
                isAudio = isAudio
            )
        }

        if (parsedList.isEmpty()) return null

        // 1. Pick highest priority audio format
        val audioFormat = AUDIO_PRIO.firstNotNullOfOrNull { targetItag ->
            parsedList.firstOrNull { it.itag == targetItag }
        } ?: parsedList.firstOrNull { it.isAudio }

        // 2. Pick fallback video format if no audio format
        val chosen = audioFormat ?: VIDEO_PRIO.firstNotNullOfOrNull { targetItag ->
            parsedList.firstOrNull { it.itag == targetItag }
        } ?: parsedList.first()

        StreamDataModel(
            videoId = videoId,
            streamUrl = chosen.url,
            itag = chosen.itag,
            mimeType = chosen.mimeType,
            bitrate = chosen.bitrate,
            contentLength = chosen.contentLength,
            expiresInSeconds = chosen.durationSec.coerceAtLeast(3600L),
            audioQuality = chosen.audioQuality
        )
    }.getOrNull()
}
