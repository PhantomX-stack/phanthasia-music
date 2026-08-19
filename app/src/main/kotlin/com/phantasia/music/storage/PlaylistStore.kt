package com.phantasia.music.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName  = "playlist_song_cross_ref",
    primaryKeys = ["playlistId", "videoId"],
    indices    = [Index("videoId")]
)
data class PlaylistSongCrossRef(val playlistId: Long, val videoId: String)

data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(parentColumn = "playlistId", entityColumn = "videoId",
        associateBy = Junction(PlaylistSongCrossRef::class))
    val songs: List<SongEntity>
)

@Dao
interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun create(p: PlaylistEntity): Long
    @Delete suspend fun delete(p: PlaylistEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addSong(x: PlaylistSongCrossRef)
    @Delete suspend fun removeSong(x: PlaylistSongCrossRef)
    @Query("DELETE FROM playlist_song_cross_ref WHERE playlistId = :playlistId") suspend fun clearSongs(playlistId: Long)
    @Query("DELETE FROM playlists WHERE playlistId = :playlistId") suspend fun deleteById(playlistId: Long)
    @Query("SELECT * FROM playlists WHERE name = :name LIMIT 1") suspend fun getByName(name: String): PlaylistEntity?
    @Transaction @Query("SELECT * FROM playlists WHERE playlistId = :id LIMIT 1") fun getById(id: Long): Flow<PlaylistWithSongs?>
    @Transaction @Query("SELECT * FROM playlists WHERE playlistId = :id LIMIT 1") suspend fun getByIdSync(id: Long): PlaylistWithSongs?
    @Transaction @Query("SELECT * FROM playlists ORDER BY createdAt DESC") fun getAll(): Flow<List<PlaylistWithSongs>>
}
