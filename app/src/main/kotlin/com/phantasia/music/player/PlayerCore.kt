package com.phantasia.music.player

import android.content.Context
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.phantasia.music.network.MusicRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StreamResolver @Inject constructor(private val repo: MusicRepository) {
    suspend fun resolve(videoId: String): String? = repo.getStream(videoId)?.streamUrl
}

@Module @InstallIn(SingletonComponent::class)
object PlayerModule {
    @androidx.annotation.OptIn(UnstableApi::class)
    @Provides @Singleton
    fun provideCacheFactory(@ApplicationContext ctx: Context, cache: SimpleCache): CacheDataSource.Factory {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(25_000)
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(mapOf(
                "Referer" to "https://www.youtube.com/",
                "Origin" to "https://www.youtube.com"
            ))

        val defaultDataSourceFactory = androidx.media3.datasource.DefaultDataSource.Factory(ctx, httpFactory)

        return CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(defaultDataSourceFactory)
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR or CacheDataSource.FLAG_BLOCK_ON_CACHE)
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @Provides @Singleton
    fun provideExoPlayer(@ApplicationContext ctx: Context, f: CacheDataSource.Factory): ExoPlayer {
        return ExoPlayer.Builder(ctx)
            .setMediaSourceFactory(DefaultMediaSourceFactory(f))
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true)
            .setHandleAudioBecomingNoisy(true).build()
    }
}
