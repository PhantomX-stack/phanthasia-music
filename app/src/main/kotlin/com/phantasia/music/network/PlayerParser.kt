package com.phantasia.music.network

import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerParser @Inject constructor() {

    private val ITAG_PRIO = listOf(141, 251, 140, 250, 249, 139)

    fun parse(videoId: String, root: JsonElement): StreamDataModel? = runCatching {
        val sd = root.obj("streamingData") ?: return null
        val fmts = sd.arr("adaptiveFormats")?.ifEmpty { null } ?: sd.arr("formats") ?: return null
        val byItag = fmts.mapNotNull { f ->
            val itag = f.int("itag") ?: return@mapNotNull null
            val mime = f.str("mimeType") ?: return@mapNotNull null
            if (!mime.startsWith("audio/")) return@mapNotNull null
            itag to f
        }.toMap()
        val (itag, f) = ITAG_PRIO.firstNotNullOfOrNull { t -> byItag[t]?.let { t to it } }
            ?: byItag.entries.firstOrNull()?.let { it.key to it.value } ?: return null
        StreamDataModel(videoId, f.str("url") ?: return null, itag,
            f.str("mimeType") ?: "", f.long("bitrate") ?: 0L,
            f.str("contentLength")?.toLongOrNull() ?: 0L,
            f.str("approxDurationMs")?.toLongOrNull()?.div(1000) ?: 3600L,
            f.str("audioQuality") ?: "")
    }.getOrNull()
}
