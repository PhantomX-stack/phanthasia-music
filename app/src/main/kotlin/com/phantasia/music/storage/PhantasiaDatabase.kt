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

@Database(entities = [SongEntity::class, PlaylistEntity::class,
    PlaylistSongCrossRef::class, SearchHistoryEntity::class],
    version = 1, exportSchema = true)
abstract class PhantasiaDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun searchHistoryDao(): SearchHistoryDao
}

@Module @InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): PhantasiaDatabase =
        Room.databaseBuilder(ctx, PhantasiaDatabase::class.java, "phantasia.db")
            .openHelperFactory(SupportFactory(SQLiteDatabase.getBytes("phantasia_secure_key".toCharArray())))
            .fallbackToDestructiveMigration().build()

    @Provides @Singleton fun songDao(db: PhantasiaDatabase): SongDao = db.songDao()
    @Provides @Singleton fun playlistDao(db: PhantasiaDatabase): PlaylistDao = db.playlistDao()
    @Provides @Singleton fun historyDao(db: PhantasiaDatabase): SearchHistoryDao = db.searchHistoryDao()
}
