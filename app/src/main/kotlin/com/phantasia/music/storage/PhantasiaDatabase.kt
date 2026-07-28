package com.phantasia.music.storage

import android.content.Context
import androidx.room.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SupportFactory
import javax.inject.Singleton

@Database(entities = [
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class,
        SearchHistoryEntity::class,
        PlayEventEntity::class,
        PlayCountEntity::class
    ,
        DownloadEntity::class
    ], version = 3, exportSchema = true)
abstract class PhantasiaDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun statsDao(): StatsDao
    abstract fun downloadDao(): DownloadDao
}

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): PhantasiaDatabase =
        Room.databaseBuilder(ctx, PhantasiaDatabase::class.java, "phantasia.db")
            .openHelperFactory(SupportFactory(SQLiteDatabase.getBytes("phantasia_key".toCharArray())))
            .fallbackToDestructiveMigration().build()
    @Provides @Singleton fun songDao(db: PhantasiaDatabase)     = db.songDao()
    @Provides @Singleton fun playlistDao(db: PhantasiaDatabase)  = db.playlistDao()
    @Provides @Singleton fun historyDao(db: PhantasiaDatabase)   = db.searchHistoryDao()
    @Provides @Singleton fun statsDao(db: PhantasiaDatabase)     = db.statsDao()
    @Provides @Singleton fun downloadDao(db: PhantasiaDatabase)  = db.downloadDao()
}
