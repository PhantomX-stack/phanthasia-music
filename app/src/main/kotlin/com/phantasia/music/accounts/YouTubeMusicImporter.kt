package com.phantasia.music.accounts

import com.phantasia.music.network.MusicRepository
import com.phantasia.music.network.TrackModel
import com.phantasia.music.storage.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class YtmImportResult(
    val playlistName: String,
    val totalTracks: Int,
    val playlistId: Long
)

@Singleton
class YouTubeMusicImporter @Inject constructor(
    private val repo: MusicRepository,
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) {

    fun extractPlaylistId(urlOrId: String): String? {
        val trimmed = urlOrId.trim()
        if (trimmed.isBlank()) return null

        // Direct list ID
        if (trimmed.startsWith("PL") || trimmed.startsWith("RD") || trimmed.startsWith("OLAK5uy_") || trimmed.startsWith("VL")) {
            return trimmed.removePrefix("VL")
        }

        // URL parsing
        return try {
            val uri = android.net.Uri.parse(trimmed)
            uri.getQueryParameter("list")
                ?: uri.pathSegments?.let { segments ->
                    val idx = segments.indexOf("playlist")
                    if (idx != -1 && idx + 1 < segments.size) segments[idx + 1] else null
                }
                ?: if (trimmed.contains("list=")) trimmed.substringAfter("list=").substringBefore("&") else null
        } catch (_: Exception) {
            if (trimmed.contains("list=")) trimmed.substringAfter("list=").substringBefore("&") else null
        }
    }

    suspend fun importPlaylistFromUrl(
        urlOrId: String,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<YtmImportResult> = withContext(Dispatchers.IO) {
        runCatching {
            val listId = extractPlaylistId(urlOrId)
                ?: throw IllegalArgumentException("Invalid YouTube Music playlist URL or ID")

            val (title, tracks) = repo.getPlaylist(listId)
            if (tracks.isEmpty()) {
                throw IllegalStateException("No tracks found in playlist or playlist is private")
            }

            val finalTitle = if (title.isBlank() || title == "Imported Playlist") "YTM - $listId" else title
            val playlistId = playlistDao.create(PlaylistEntity(name = finalTitle))

            tracks.forEachIndexed { index, track ->
                val song = SongEntity(
                    videoId = track.videoId,
                    title = track.title,
                    artistName = track.artistName,
                    albumTitle = track.albumTitle,
                    artworkUrl = track.artworkUrl,
                    durationSeconds = track.durationSeconds
                )
                songDao.upsert(song)
                playlistDao.addSong(PlaylistSongCrossRef(playlistId, track.videoId))
                onProgress(index + 1, tracks.size)
            }

            YtmImportResult(
                playlistName = finalTitle,
                totalTracks = tracks.size,
                playlistId = playlistId
            )
        }
    }

    suspend fun importLikedSongs(
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): Result<Int> = withContext(Dispatchers.IO) {
        runCatching {
            val tracks = repo.getLikedSongs()
            if (tracks.isEmpty()) {
                throw IllegalStateException("No liked songs found. Make sure you are signed in.")
            }

            tracks.forEachIndexed { index, track ->
                val song = SongEntity(
                    videoId = track.videoId,
                    title = track.title,
                    artistName = track.artistName,
                    albumTitle = track.albumTitle,
                    artworkUrl = track.artworkUrl,
                    durationSeconds = track.durationSeconds,
                    isFavourite = 1
                )
                songDao.upsert(song)
                onProgress(index + 1, tracks.size)
            }

            tracks.size
        }
    }
}
