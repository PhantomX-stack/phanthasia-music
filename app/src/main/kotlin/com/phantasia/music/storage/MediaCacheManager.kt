package com.phantasia.music.storage

import android.content.Context
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
    @Provides @Singleton
    fun provideSimpleCache(@ApplicationContext ctx: Context): SimpleCache =
        SimpleCache(File(ctx.cacheDir, "media_stream_cache"),
            LeastRecentlyUsedCacheEvictor(2L * 1024L * 1024L * 1024L))
}
