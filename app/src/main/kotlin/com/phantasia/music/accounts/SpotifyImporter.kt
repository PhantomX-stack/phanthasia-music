package com.phantasia.music.accounts

import com.phantasia.music.BuildConfig
import com.phantasia.music.network.MusicRepository
import com.phantasia.music.network.SearchResultModel
import com.phantasia.music.storage.*
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton

data class SpotifyImportResult(
    val playlistName: String,
    val totalTracks: Int,
    val matchedTracks: Int,
    val playlistId: Long
)

data class SpotifyTrackItem(
    val title: String,
    val artist: String,
    val album: String = "",
    val durationMs: Long = 0L,
    val artworkUrl: String = ""
)

@Singleton
class SpotifyImporter @Inject constructor(
    private val client: HttpClient,
    private val repo: MusicRepository,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) {
    companion object {
        const val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
        const val REDIRECT_URI = "phantasia://spotify-callback"
        const val SCOPES = "playlist-read-private user-library-read"
    }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    fun extractPlaylistId(urlOrId: String): String? {
        val trimmed = urlOrId.trim()
        if (trimmed.isBlank()) return null
        if (!trimmed.contains("/") && !trimmed.contains(":") && trimmed.length > 10) {
            return trimmed
        }
        return try {
            val uri = android.net.Uri.parse(trimmed)
            val segments = uri.pathSegments
            val idx = segments.indexOf("playlist")
            if (idx != -1 && idx + 1 < segments.size) {
                segments[idx + 1].substringBefore("?")
            } else if (trimmed.contains("spotify:playlist:")) {
                trimmed.substringAfter("spotify:playlist:").substringBefore("?")
            } else null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun fetchSpotifyEmbedPlaylist(playlistId: String): Pair<String, List<SpotifyTrackItem>> = withContext(Dispatchers.IO) {
        val embedUrl = "https://open.spotify.com/embed/playlist/$playlistId"
        val response = client.get(embedUrl) {
            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
        }.bodyAsText()

        var title = "Spotify Playlist"
        val tracks = mutableListOf<SpotifyTrackItem>()

        try {
            if (response.contains("__NEXT_DATA__")) {
                val nextDataJson = response.substringAfter("id=\"__NEXT_DATA__\" type=\"application/json\">")
                    .substringBefore("</script>")
                val root = json.parseToJsonElement(nextDataJson).jsonObject
                val entity = root["props"]?.jsonObject?.get("pageProps")?.jsonObject
                    ?.get("state")?.jsonObject?.get("data")?.jsonObject?.get("entity")?.jsonObject

                entity?.get("name")?.jsonPrimitive?.contentOrNull?.let { title = it }

                val trackList = entity?.get("trackList")?.jsonArray
                trackList?.forEach { t ->
                    val tObj = t.jsonObject
                    val tTitle = tObj["title"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                    val tArtist = tObj["subtitle"]?.jsonPrimitive?.contentOrNull ?: "Unknown"
                    val dur = tObj["duration"]?.jsonPrimitive?.longOrNull ?: 0L
                    tracks.add(SpotifyTrackItem(title = tTitle, artist = tArtist, durationMs = dur))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback: If embed scraping yielded no tracks, try fetching anonymous token and querying Web API
        if (tracks.isEmpty()) {
            try {
                val tokenResponse = client.get("https://open.spotify.com/get_access_token") {
                    header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                }.bodyAsText()
                val tokenObj = json.parseToJsonElement(tokenResponse).jsonObject
                val accessToken = tokenObj["accessToken"]?.jsonPrimitive?.contentOrNull

                if (!accessToken.isNullOrBlank()) {
                    val apiRes = client.get("https://api.spotify.com/v1/playlists/$playlistId") {
                        header("Authorization", "Bearer $accessToken")
                    }.bodyAsText()
                    val apiObj = json.parseToJsonElement(apiRes).jsonObject
                    apiObj["name"]?.jsonPrimitive?.contentOrNull?.let { title = it }
                    val items = apiObj["tracks"]?.jsonObject?.get("items")?.jsonArray
                    items?.forEach { item ->
                        val track = item.jsonObject["track"]?.jsonObject ?: return@forEach
                        val tTitle = track["name"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                        val artists = track["artists"]?.jsonArray
                            ?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.contentOrNull }
                            ?.joinToString(", ") ?: "Unknown"
                        val albumName = track["album"]?.jsonObject?.get("name")?.jsonPrimitive?.contentOrNull ?: ""
                        val dur = track["duration_ms"]?.jsonPrimitive?.longOrNull ?: 0L
                        tracks.add(SpotifyTrackItem(title = tTitle, artist = artists, album = albumName, durationMs = dur))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Pair(title, tracks)
    }

    suspend fun importPlaylistFromUrl(
        urlOrId: String,
        onProgress: (current: Int, total: Int, currentTrack: String) -> Unit = { _, _, _ -> }
    ): Result<SpotifyImportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val playlistId = extractPlaylistId(urlOrId)
                ?: throw IllegalArgumentException("Invalid Spotify playlist URL or ID")

            val (title, spotifyTracks) = fetchSpotifyEmbedPlaylist(playlistId)
            if (spotifyTracks.isEmpty()) {
                throw IllegalStateException("Could not extract tracks from Spotify playlist")
            }

            val finalTitle = if (title.isBlank()) "Spotify - $playlistId" else title
            val localPlaylistId = playlistDao.create(PlaylistEntity(name = finalTitle))

            var matched = 0
            spotifyTracks.forEachIndexed { index, track ->
                onProgress(index + 1, spotifyTracks.size, "${track.title} - ${track.artist}")
                val searchQuery = "${track.title} ${track.artist}".trim()
                val results = repo.search(searchQuery)
                val bestTrack = results.filterIsInstance<SearchResultModel.TrackResult>()
                    .firstOrNull()?.track

                if (bestTrack != null) {
                    val song = SongEntity(
                        videoId = bestTrack.videoId,
                        title = bestTrack.title,
                        artistName = bestTrack.artistName,
                        albumTitle = bestTrack.albumTitle.ifBlank { track.album },
                        artworkUrl = bestTrack.artworkUrl,
                        durationSeconds = if (bestTrack.durationSeconds > 0) bestTrack.durationSeconds else track.durationMs / 1000
                    )
                    songDao.upsert(song)
                    playlistDao.addSong(PlaylistSongCrossRef(localPlaylistId, bestTrack.videoId))
                    matched++
                }
            }

            SpotifyImportResult(
                playlistName = finalTitle,
                totalTracks = spotifyTracks.size,
                matchedTracks = matched,
                playlistId = localPlaylistId
            )
        }
    }

    suspend fun importLikedSongsFromToken(
        accessToken: String,
        onProgress: (current: Int, total: Int, currentTrack: String) -> Unit = { _, _, _ -> }
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val res = client.get("https://api.spotify.com/v1/me/tracks?limit=50") {
                header("Authorization", "Bearer $accessToken")
            }.bodyAsText()
            val root = json.parseToJsonElement(res).jsonObject
            val items = root["items"]?.jsonArray ?: emptyList()

            var count = 0
            items.forEachIndexed { index, item ->
                val track = item.jsonObject["track"]?.jsonObject ?: return@forEachIndexed
                val title = track["name"]?.jsonPrimitive?.contentOrNull ?: return@forEachIndexed
                val artist = track["artists"]?.jsonArray
                    ?.mapNotNull { it.jsonObject["name"]?.jsonPrimitive?.contentOrNull }
                    ?.joinToString(", ") ?: "Unknown"

                onProgress(index + 1, items.size, "$title - $artist")

                val searchRes = repo.search("$title $artist")
                val best = searchRes.filterIsInstance<SearchResultModel.TrackResult>().firstOrNull()?.track
                if (best != null) {
                    val song = SongEntity(
                        videoId = best.videoId,
                        title = best.title,
                        artistName = best.artistName,
                        albumTitle = best.albumTitle,
                        artworkUrl = best.artworkUrl,
                        durationSeconds = best.durationSeconds,
                        isFavourite = 1
                    )
                    songDao.upsert(song)
                    count++
                }
            }
            count
        }
    }
}
