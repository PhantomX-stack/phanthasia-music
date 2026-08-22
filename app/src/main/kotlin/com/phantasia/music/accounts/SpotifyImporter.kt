package com.phantasia.music.accounts

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
        const val CLIENT_ID = "YOUR_SPOTIFY_CLIENT_ID"
        const val REDIRECT_URI = "phantasia://spotify-callback"
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

    suspend fun exchangeSpDcForAccessToken(spDc: String): String? = runCatching {
        val response = client.get("https://open.spotify.com/get_access_token?reason=transport&productType=web_player") {
            header("Cookie", "sp_dc=$spDc")
            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        }.bodyAsText()
        json.parseToJsonElement(response).jsonObject["accessToken"]?.jsonPrimitive?.contentOrNull
    }.getOrNull()

    suspend fun fetchPublicSpotifyPlaylist(playlistId: String): List<Pair<String, String>> = runCatching {
        val html = client.get("https://open.spotify.com/embed/playlist/$playlistId") {
            header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
        }.bodyAsText()
        val jsonMatch = Regex("""<script id="__NEXT_DATA__"[^>]*>(.+?)</script>""", RegexOption.DOT_MATCHES_ALL)
            .find(html) ?: return@runCatching emptyList()
        val root = json.parseToJsonElement(jsonMatch.groupValues[1]).jsonObject
        runCatching {
            root["props"]?.jsonObject?.get("pageProps")?.jsonObject
                ?.get("state")?.jsonObject?.get("data")?.jsonObject
                ?.get("entity")?.jsonObject?.get("trackList")?.jsonArray
                ?.mapNotNull { track ->
                    val obj = track.jsonObject
                    val title = obj["title"]?.jsonPrimitive?.contentOrNull ?: return@mapNotNull null
                    val artist = obj["subtitle"]?.jsonPrimitive?.contentOrNull ?: ""
                    title to artist
                } ?: emptyList()
        }.getOrDefault(emptyList())
    }.getOrDefault(emptyList())

    suspend fun fetchSpotifyEmbedPlaylist(playlistId: String): Pair<String, List<SpotifyTrackItem>> = withContext(Dispatchers.IO) {
        val embedUrl = "https://open.spotify.com/embed/playlist/$playlistId"
        val response = runCatching {
            client.get(embedUrl) {
                header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            }.bodyAsText()
        }.getOrDefault("")

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
            val trackPairs = if (spotifyTracks.isNotEmpty()) {
                spotifyTracks.map { it.title to it.artist }
            } else {
                fetchPublicSpotifyPlaylist(playlistId)
            }

            if (trackPairs.isEmpty()) {
                throw IllegalStateException("Could not extract tracks from Spotify playlist")
            }

            val finalTitle = if (title.isBlank() || title == "Spotify Playlist") "Spotify - $playlistId" else title
            val localPlaylistId = playlistDao.create(PlaylistEntity(name = finalTitle))

            var matched = 0
            trackPairs.forEachIndexed { index, (songTitle, artistName) ->
                onProgress(index + 1, trackPairs.size, "$songTitle - $artistName")
                val searchQuery = "$songTitle $artistName".trim()
                val results = repo.search(searchQuery)
                val bestTrack = results.filterIsInstance<SearchResultModel.TrackResult>()
                    .firstOrNull()?.track

                if (bestTrack != null) {
                    val song = SongEntity(
                        videoId = bestTrack.videoId,
                        title = bestTrack.title,
                        artistName = bestTrack.artistName,
                        albumTitle = bestTrack.albumTitle,
                        artworkUrl = bestTrack.artworkUrl,
                        durationSeconds = bestTrack.durationSeconds
                    )
                    songDao.upsert(song)
                    playlistDao.addSong(PlaylistSongCrossRef(localPlaylistId, bestTrack.videoId))
                    matched++
                }
            }

            SpotifyImportResult(
                playlistName = finalTitle,
                totalTracks = trackPairs.size,
                matchedTracks = matched,
                playlistId = localPlaylistId
            )
        }
    }

    suspend fun importLikedSongsFromSpDc(
        spDc: String,
        onProgress: (current: Int, total: Int, currentTrack: String) -> Unit = { _, _, _ -> }
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val token = exchangeSpDcForAccessToken(spDc)
                ?: throw IllegalStateException("Failed to exchange sp_dc for Spotify Web access token")
            importLikedSongsFromToken(token, onProgress).getOrThrow()
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
