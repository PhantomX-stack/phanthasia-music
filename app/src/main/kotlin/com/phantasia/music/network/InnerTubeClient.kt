package com.phantasia.music.network

import com.phantasia.music.security.EnvironmentGuard
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.Protocol
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

internal object IT {
    const val KEY     = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
    const val NAME    = "ANDROID_MUSIC"
    const val VER     = "6.45.52"
    const val SDK     = 34
    const val UA      = "com.google.android.apps.youtube.music/6.45.52 (Linux; U; Android 14) gzip"
    const val BASE    = "https://music.youtube.com/youtubei/v1"
    const val PLAYER  = "https://www.youtube.com/s/player/4248d3c7/player_ias.vflset/en_US/base.js"
}

data class InnerTubeLocale(val gl: String = "US", val hl: String = "en")

fun buildContextPayload(l: InnerTubeLocale): MutableMap<String, Any> = mutableMapOf(
    "context" to mapOf(
        "client" to mapOf(
            "clientName"        to IT.NAME,
            "clientVersion"     to IT.VER,
            "androidSdkVersion" to IT.SDK,
            "hl"                to l.hl,
            "gl"                to l.gl,
            "userAgent"         to IT.UA,
            "timeZone"          to "UTC",
            "utcOffsetMinutes"  to 0
        )
    )
)

@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class InnerTubeHttp

@Module @InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideLocale(): InnerTubeLocale = InnerTubeLocale()

    @InnerTubeHttp @Provides @Singleton
    fun provideOkHttpClient(guard: EnvironmentGuard): OkHttpClient =
        OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(guard.sslPinningInterceptor())
            .addInterceptor { chain ->
                chain.proceed(chain.request().newBuilder()
                    .header("User-Agent",                IT.UA)
                    .header("Content-Type",              "application/json")
                    .header("X-Goog-Api-Key",            IT.KEY)
                    .header("X-Goog-Api-Format-Version", "2")
                    .header("Origin",                    "https://music.youtube.com")
                    .header("Referer",                   "https://music.youtube.com/")
                    .build())
            }.build()

    @Provides @Singleton
    fun provideKtorClient(@InnerTubeHttp okHttp: OkHttpClient): HttpClient =
        HttpClient(OkHttp) {
            engine { preconfigured = okHttp }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000; connectTimeoutMillis = 15_000; socketTimeoutMillis = 30_000
            }
            install(Logging) { level = LogLevel.NONE }
            defaultRequest { contentType(ContentType.Application.Json) }
        }
}
