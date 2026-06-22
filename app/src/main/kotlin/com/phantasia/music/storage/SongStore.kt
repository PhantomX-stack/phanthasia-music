package com.phantasia.music.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val videoId: String,
    val title: String,
    val artistName: String,
    val albumTitle: String,
    val artworkUrl: String,
    val durationSeconds: Long,
    @ColumnInfo(defaultValue = "0") val isFavourite: Int = 0,
    val cachedAt: Long = System.currentTimeMillis()
)

@Dao
interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(s: SongEntity)
    @Query("DELETE FROM songs WHERE videoId = :id") suspend fun delete(id: String)
    @Query("SELECT * FROM songs WHERE videoId = :id LIMIT 1") suspend fun getById(id: String): SongEntity?
    @Query("SELECT * FROM songs WHERE isFavourite = 1 ORDER BY cachedAt DESC") fun getFavourites(): Flow<List<SongEntity>>
    @Query("SELECT * FROM songs ORDER BY cachedAt DESC") fun getAll(): Flow<List<SongEntity>>
    @Query("UPDATE songs SET isFavourite = :fav WHERE videoId = :id") suspend fun setFavourite(id: String, fav: Int)
}
