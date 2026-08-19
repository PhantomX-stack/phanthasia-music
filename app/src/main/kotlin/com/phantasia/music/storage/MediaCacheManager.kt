package com.phantasia.music.storage

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton

@Module @InstallIn(SingletonComponent::class)
object CacheModule {
    @androidx.annotation.OptIn(UnstableApi::class)
    @Provides @Singleton
    fun provideSimpleCache(@ApplicationContext ctx: Context): SimpleCache {
        val cacheDir = File(ctx.cacheDir, "media_stream_cache")
        if (!cacheDir.exists()) cacheDir.mkdirs()
        val evictor = LeastRecentlyUsedCacheEvictor(512L * 1024L * 1024L) // 512MB
        val databaseProvider = StandaloneDatabaseProvider(ctx)
        return SimpleCache(cacheDir, evictor, databaseProvider)
    }
}
