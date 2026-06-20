package com.phantasia.music.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

enum class SearchHistoryType { TRACK, ALBUM, ARTIST, QUERY }

@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val query: String,
    val type: SearchHistoryType = SearchHistoryType.QUERY,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao interface SearchHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insert(e: SearchHistoryEntity)
    @Query("DELETE FROM search_history") suspend fun clearAll()
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT :n")
    fun getRecent(n: Int = 10): Flow<List<SearchHistoryEntity>>
    @Query("DELETE FROM search_history WHERE id = :id") suspend fun deleteById(id: Long)
}
