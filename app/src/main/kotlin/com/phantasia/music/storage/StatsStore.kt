package com.phantasia.music.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "play_events")
data class PlayEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val videoId:    String,
    val title:      String,
    val artistName: String,
    val artworkUrl: String,
    val playedAt:   Long = System.currentTimeMillis(),
    val durationMs: Long = 0L
)

@Entity(tableName = "play_counts")
data class PlayCountEntity(
    @PrimaryKey val videoId:  String,
    val title:      String,
    val artistName: String,
    val artworkUrl: String,
    val count:      Int  = 0,
    val totalMs:    Long = 0L
)

@Dao
interface StatsDao {
    @Insert
    suspend fun insertEvent(e: PlayEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCount(c: PlayCountEntity)

    @Query("SELECT * FROM play_counts ORDER BY count DESC LIMIT :n")
    fun getTopSongs(n: Int = 20): Flow<List<PlayCountEntity>>

    @Query("SELECT videoId, title, artistName, artworkUrl, COUNT(*) as count, SUM(durationMs) as totalMs FROM play_events WHERE playedAt >= :since GROUP BY videoId ORDER BY count DESC LIMIT :n")
    fun getTopSongsSince(since: Long, n: Int = 20): Flow<List<PlayCountEntity>>

    @Query("SELECT SUM(durationMs) FROM play_events WHERE playedAt >= :since")
    fun getTotalListenTimeMs(since: Long): Flow<Long?>

    @Query("SELECT COUNT(*) FROM play_events WHERE playedAt >= :since")
    fun getPlayCountSince(since: Long): Flow<Int>

    @Query("SELECT COUNT(DISTINCT videoId) FROM play_events WHERE playedAt >= :since")
    fun getUniqueSongsSince(since: Long): Flow<Int>
}
