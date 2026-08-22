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
    fun provideDefaultDataSourceFactory(@ApplicationContext ctx: Context): androidx.media3.datasource.DefaultDataSource.Factory {
        val httpFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
            .setConnectTimeoutMs(20_000)
            .setReadTimeoutMs(30_000)
            .setAllowCrossProtocolRedirects(true)
            .setDefaultRequestProperties(mapOf(
                "Referer" to "https://www.youtube.com/",
                "Origin" to "https://www.youtube.com"
            ))

        return androidx.media3.datasource.DefaultDataSource.Factory(ctx, httpFactory)
    }

    @androidx.annotation.OptIn(UnstableApi::class)
    @Provides @Singleton
    fun provideExoPlayer(@ApplicationContext ctx: Context, dataSourceFactory: androidx.media3.datasource.DefaultDataSource.Factory): ExoPlayer {
        return ExoPlayer.Builder(ctx)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSourceFactory))
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_NETWORK)
            .build()
    }
}
