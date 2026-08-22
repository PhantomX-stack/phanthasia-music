package com.phantasia.music.network

import com.phantasia.music.BuildConfig
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
    val KEY = BuildConfig.INNERTUBE_API_KEY
    const val NAME   = "ANDROID_MUSIC"
    const val VER    = "6.45.52"
    const val SDK    = 34
    const val UA     = "com.google.android.apps.youtube.music/6.45.52 (Linux; U; Android 14) gzip"
    const val BASE   = "https://music.youtube.com/youtubei/v1"
    const val YT_BASE= "https://www.youtube.com/youtubei/v1"
    const val PLAYER = "https://www.youtube.com/s/player/4248d3c7/player_ias.vflset/en_US/base.js"
}

data class InnerTubeLocale(val gl: String = "US", val hl: String = "en")

enum class ClientType {
    ANDROID_VR,
    ANDROID_MUSIC,
    ANDROID,
    WEB_REMIX,
    IOS,
    ANDROID_TESTSUITE,
    TVHTML5_SIMPLY_EMBEDDED,
    WEB_EMBEDDED
}

fun buildContext(l: InnerTubeLocale, type: ClientType = ClientType.ANDROID_MUSIC): MutableMap<String, Any> {
    val clientMap = when (type) {
        ClientType.ANDROID_VR -> mapOf(
            "clientName" to "ANDROID_VR",
            "clientVersion" to "1.60.19",
            "deviceModel" to "Quest 3",
            "androidSdkVersion" to 32,
            "hl" to l.hl,
            "gl" to l.gl,
            "userAgent" to "com.google.android.apps.youtube.vr.oculus/1.60.19 (Linux; U; Android 12L; eureka-user Build/SQ3A.220605.009.A1) gzip",
            "timeZone" to "UTC",
            "utcOffsetMinutes" to 0
        )
        ClientType.WEB_EMBEDDED -> mapOf(
            "clientName" to "WEB_EMBEDDED_PLAYER",
            "clientVersion" to "1.20240101.01.00",
            "hl" to l.hl,
            "gl" to l.gl,
            "userAgent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
            "timeZone" to "UTC",
            "utcOffsetMinutes" to 0
        )
        ClientType.ANDROID_MUSIC -> mapOf(
            "clientName" to "ANDROID_MUSIC",
            "clientVersion" to "6.45.52",
            "androidSdkVersion" to 34,
            "hl" to l.hl,
            "gl" to l.gl,
            "userAgent" to IT.UA,
            "timeZone" to "UTC",
            "utcOffsetMinutes" to 0
        )
        ClientType.ANDROID -> mapOf(
            "clientName" to "ANDROID",
            "clientVersion" to "19.09.37",
            "androidSdkVersion" to 34,
            "hl" to l.hl,
            "gl" to l.gl,
            "userAgent" to "com.google.android.youtube/19.09.37 (Linux; U; Android 14) gzip",
            "timeZone" to "UTC",
            "utcOffsetMinutes" to 0
        )
        ClientType.WEB_REMIX -> mapOf(
            "clientName" to "WEB_REMIX",
            "clientVersion" to "1.20240101.01.00",
            "hl" to l.hl,
            "gl" to l.gl,
            "userAgent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:128.0) Gecko/20100101 Firefox/128.0",
            "timeZone" to "UTC",
            "utcOffsetMinutes" to 0
        )
        ClientType.IOS -> mapOf(
            "clientName" to "IOS",
            "clientVersion" to "19.09.3",
            "deviceModel" to "iPhone14,3",
            "hl" to l.hl,
            "gl" to l.gl,
            "userAgent" to "com.google.ios.youtube/19.09.3 (iPhone14,3; U; CPU iOS 17_4 like Mac OS X)",
            "timeZone" to "UTC",
            "utcOffsetMinutes" to 0
        )
        ClientType.ANDROID_TESTSUITE -> mapOf(
            "clientName" to "ANDROID_TESTSUITE",
            "clientVersion" to "1.9",
            "androidSdkVersion" to 30,
            "hl" to l.hl,
            "gl" to l.gl,
            "userAgent" to "GooglePlay/30.0.0 (Linux; Android 11)",
            "timeZone" to "UTC",
            "utcOffsetMinutes" to 0
        )
        ClientType.TVHTML5_SIMPLY_EMBEDDED -> mapOf(
            "clientName" to "TVHTML5_SIMPLY_EMBEDDED_PLAYER",
            "clientVersion" to "2.0",
            "hl" to l.hl,
            "gl" to l.gl,
            "userAgent" to "Mozilla/5.0 (SMART-TV; Linux; Tizen 6.0) AppleWebKit/537.36 (KHTML, like Gecko) SamsungBrowser/4.0 Chrome/76.0.3809.146 TV Safari/537.36",
            "timeZone" to "UTC",
            "utcOffsetMinutes" to 0
        )
    }
    return mutableMapOf("context" to mapOf("client" to clientMap))
}

@Qualifier @Retention(AnnotationRetention.BINARY)
annotation class InnerTubeHttp

@Module @InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides @Singleton
    fun provideLocale(): InnerTubeLocale = InnerTubeLocale()

    @InnerTubeHttp @Provides @Singleton
    fun provideOkHttp(guard: EnvironmentGuard): OkHttpClient =
        OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30,    TimeUnit.SECONDS)
            .writeTimeout(15,   TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(guard.standardNetworkInterceptor())
            .addInterceptor { chain ->
                val origReq = chain.request()
                val existingUa = origReq.header("User-Agent")
                val request = origReq.newBuilder()
                    .apply {
                        if (existingUa.isNullOrBlank()) header("User-Agent", IT.UA)
                        header("X-Goog-Api-Format-Version", "2")
                        if (IT.KEY.isNotBlank() && origReq.header("X-Goog-Api-Key") == null) {
                            header("X-Goog-Api-Key", IT.KEY)
                        }
                    }
                    .build()
                chain.proceed(request)
            }.build()

    @Provides @Singleton
    fun provideDefaultKtor(@InnerTubeHttp ok: OkHttpClient): HttpClient =
        HttpClient(OkHttp) {
            engine { preconfigured = ok }
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis  = 30_000
            }
            install(Logging) { level = LogLevel.NONE }
            defaultRequest { contentType(ContentType.Application.Json) }
        }

    @InnerTubeHttp @Provides @Singleton
    fun provideInnerTubeKtor(defaultClient: HttpClient): HttpClient = defaultClient
}
