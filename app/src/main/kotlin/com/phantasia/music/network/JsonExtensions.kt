package com.phantasia.music.network

import kotlinx.serialization.json.*

fun JsonElement?.obj(key: String): JsonElement? =
    runCatching { (this as? JsonObject)?.get(key)?.takeIf { it !is JsonNull } }.getOrNull()

fun JsonElement?.arr(key: String): List<JsonElement>? =
    runCatching { ((this as? JsonObject)?.get(key) as? JsonArray)?.toList() }.getOrNull()

fun JsonElement?.str(key: String): String? =
    runCatching { ((this as? JsonObject)?.get(key) as? JsonPrimitive)?.content }.getOrNull()

fun JsonElement?.asStr(): String? =
    runCatching { (this as? JsonPrimitive)?.content }.getOrNull()

fun JsonElement?.int(key: String): Int? =
    runCatching { ((this as? JsonObject)?.get(key) as? JsonPrimitive)?.intOrNull }.getOrNull()

fun JsonElement?.long(key: String): Long? =
    runCatching { ((this as? JsonObject)?.get(key) as? JsonPrimitive)?.longOrNull }.getOrNull()

fun List<JsonElement>?.idx(i: Int): JsonElement? = this?.getOrNull(i)

fun JsonElement?.runsText(): String? =
    runCatching { arr("runs")?.joinToString("") { it.str("text") ?: "" }?.takeIf { it.isNotEmpty() } }.getOrNull()

fun JsonElement?.bestThumbnailUrl(): String? =
    runCatching { arr("thumbnails")?.mapNotNull { it.str("url") }?.lastOrNull() }.getOrNull()
