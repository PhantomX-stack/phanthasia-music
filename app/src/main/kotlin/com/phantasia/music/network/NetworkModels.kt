package com.phantasia.music.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackModel(
    val videoId:         String,
    val title:           String,
    val artistName:      String,
    val albumTitle:      String,
    val artworkUrl:      String,
    val durationSeconds: Long    = 0L,
    val isExplicit:      Boolean = false
)

@Serializable
data class AlbumModel(
    val browseId:     String,
    val title:        String,
    val year:         String,
    val thumbnailUrl: String,
    val artistName:   String           = "",
    val tracks:       List<TrackModel> = emptyList()
)

@Serializable
data class ArtistModel(
    val browseId:     String,
    val name:         String,
    val thumbnailUrl: String = ""
)

@Serializable
data class StreamDataModel(
    val videoId:          String,
    val streamUrl:        String,
    val itag:             Int,
    val mimeType:         String,
    val bitrate:          Long   = 0L,
    val contentLength:    Long   = 0L,
    val expiresInSeconds: Long   = 0L,
    val audioQuality:     String = ""
)

@Serializable
sealed interface SearchResultModel {
    @Serializable @SerialName("track")
    data class TrackResult(val track: TrackModel)    : SearchResultModel
    @Serializable @SerialName("album")
    data class AlbumResult(val album: AlbumModel)    : SearchResultModel
    @Serializable @SerialName("artist")
    data class ArtistResult(val artist: ArtistModel) : SearchResultModel
}
