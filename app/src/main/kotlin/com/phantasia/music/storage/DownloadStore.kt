package com.phantasia.music.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

enum class DownloadStatus {
    QUEUED, DOWNLOADING, DONE, FAILED
}

enum class DownloadQuality(val label: String, val itag: Int) {
    HIGH("High (256kbps)",    141),  // m4a 256kbps
    MEDIUM("Medium (128kbps)", 140), // m4a 128kbps
    LOW("Low (48kbps)",        139)  // m4a 48kbps
}

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val videoId:     String,
    val title:       String,
    val artistName:  String,
    val albumTitle:  String,
    val artworkUrl:  String,
    val filePath:    String  = "",   // absolute path once downloaded
    val fileSize:    Long    = 0L,
    val quality:     DownloadQuality = DownloadQuality.HIGH,
    val status:      DownloadStatus  = DownloadStatus.QUEUED,
    val progress:    Int     = 0,    // 0..100
    val addedAt:     Long    = System.currentTimeMillis(),
    val completedAt: Long    = 0L
)

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(d: DownloadEntity)

    @Delete
    suspend fun delete(d: DownloadEntity)

    @Query("SELECT * FROM downloads ORDER BY addedAt DESC")
    fun getAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status = 'DONE' ORDER BY completedAt DESC")
    fun getCompleted(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE status IN ('QUEUED','DOWNLOADING') ORDER BY addedAt ASC")
    fun getActive(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE videoId = :id LIMIT 1")
    suspend fun getById(id: String): DownloadEntity?

    @Query("UPDATE downloads SET status = :status, progress = :progress WHERE videoId = :id")
    suspend fun updateProgress(id: String, status: DownloadStatus, progress: Int)

    @Query("UPDATE downloads SET status = :status, filePath = :path, completedAt = :ts WHERE videoId = :id")
    suspend fun markDone(id: String, status: DownloadStatus, path: String, ts: Long)
}
