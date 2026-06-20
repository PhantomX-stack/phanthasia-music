#!/usr/bin/env python3
import html
import os

FILES = {
    '.gitignore': '''\
/build
**/build/
**/.gradle/
.kotlin/
*.class
*.apk
*.aab
local.properties
/captures
*.jks
*.keystore
.env
.idea/
*.iml
.DS_Store
README.md
''',
    'README.md': '''\
# Phantasia Music
Offline-first M3 music streaming — InnerTube powered.

## Structure
```
app/
 src/main/
  kotlin/com/phantasia/music/
   network/    HTTP client, cipher, parsers, models, repo
   storage/    SQLCipher Room DB + 2 GB LRU cache
   player/     Media3 ExoPlayer service + queue
   security/   AES-256-GCM prefs + SSL pinning
   ui/         M3 Compose screens + MVI states
  res/
   xml/        network_security_config.xml
   values/     themes.xml
  AndroidManifest.xml
 build.gradle.kts
 proguard-rules.pro
build.gradle.kts
libs.versions.toml
settings.gradle.kts
```

## Build
```bash
./gradlew assembleDebug
```
''',
    'settings.gradle.kts': '''\
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "PhantasiaMusic"
include(":app")
''',
    'build.gradle.kts': '''\
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android)      apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android)        apply false
    alias(libs.plugins.ksp)                 apply false
}
''',
    'libs.versions.toml': '''\
[versions]
agp                   = "8.5.2"
kotlin                = "2.0.20"
ksp                   = "2.0.20-1.0.25"
coreKtx               = "1.13.1"
lifecycleRuntime      = "2.8.5"
activityCompose       = "1.9.2"
composeBom            = "2024.09.02"
navigationCompose     = "2.8.0"
hilt                  = "2.52"
hiltNavigationCompose = "1.2.0"
room                  = "2.6.1"
sqlcipher             = "4.5.4"
media3                = "1.4.1"
ktor                  = "2.3.12"
okhttp                = "4.12.0"
kotlinxSerialization  = "1.7.2"
coil                  = "2.7.0"
palette               = "1.0.0"
securityCrypto        = "1.1.0-alpha06"
workRuntime           = "2.9.1"

[libraries]
androidx-core-ktx                   = { group = "androidx.core",              name = "core-ktx",                        version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx      = { group = "androidx.lifecycle",         name = "lifecycle-runtime-ktx",           version.ref = "lifecycleRuntime" }
androidx-activity-compose           = { group = "androidx.activity",          name = "activity-compose",                version.ref = "activityCompose" }
androidx-compose-bom                = { group = "androidx.compose",           name = "compose-bom",                     version.ref = "composeBom" }
androidx-compose-ui                 = { group = "androidx.compose.ui",        name = "ui" }
androidx-compose-ui-graphics        = { group = "androidx.compose.ui",        name = "ui-graphics" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui",        name = "ui-tooling-preview" }
androidx-compose-material3          = { group = "androidx.compose.material3", name = "material3" }
androidx-navigation-compose         = { group = "androidx.navigation",        name = "navigation-compose",              version.ref = "navigationCompose" }
hilt-android                        = { group = "com.google.dagger",          name = "hilt-android",                    version.ref = "hilt" }
hilt-compiler                       = { group = "com.google.dagger",          name = "hilt-android-compiler",           version.ref = "hilt" }
hilt-navigation-compose             = { group = "androidx.hilt",              name = "hilt-navigation-compose",         version.ref = "hiltNavigationCompose" }
room-runtime                        = { group = "androidx.room",              name = "room-runtime",                    version.ref = "room" }
room-ktx                            = { group = "androidx.room",              name = "room-ktx",                        version.ref = "room" }
room-compiler                       = { group = "androidx.room",              name = "room-compiler",                   version.ref = "room" }
sqlcipher                           = { group = "net.zetetic",                name = "android-database-sqlcipher",      version.ref = "sqlcipher" }
media3-exoplayer                    = { group = "androidx.media3",            name = "media3-exoplayer",                version.ref = "media3" }
media3-exoplayer-dash               = { group = "androidx.media3",            name = "media3-exoplayer-dash",           version.ref = "media3" }
media3-session                      = { group = "androidx.media3",            name = "media3-session",                  version.ref = "media3" }
media3-ui                           = { group = "androidx.media3",            name = "media3-ui",                       version.ref = "media3" }
media3-datasource-cache             = { group = "androidx.media3",            name = "media3-datasource",               version.ref = "media3" }
ktor-client-core                    = { group = "io.ktor",                    name = "ktor-client-core",                version.ref = "ktor" }
ktor-client-okhttp                  = { group = "io.ktor",                    name = "ktor-client-okhttp",              version.ref = "ktor" }
ktor-client-content-negotiation     = { group = "io.ktor",                    name = "ktor-client-content-negotiation", version.ref = "ktor" }
ktor-serialization-json             = { group = "io.ktor",                    name = "ktor-serialization-kotlinx-json", version.ref = "ktor" }
ktor-client-logging                 = { group = "io.ktor",                    name = "ktor-client-logging",             version.ref = "ktor" }
okhttp                              = { group = "com.squareup.okhttp3",       name = "okhttp",                          version.ref = "okhttp" }
okhttp-logging                      = { group = "com.squareup.okhttp3",       name = "logging-interceptor",             version.ref = "okhttp" }
kotlinx-serialization-json          = { group = "org.jetbrains.kotlinx",     name = "kotlinx-serialization-json",      version.ref = "kotlinxSerialization" }
coil-compose                        = { group = "io.coil-kt",                 name = "coil-compose",                    version.ref = "coil" }
palette                             = { group = "androidx.palette",           name = "palette-ktx",                     version.ref = "palette" }
security-crypto                     = { group = "androidx.security",          name = "security-crypto",                 version.ref = "securityCrypto" }
work-runtime-ktx                    = { group = "androidx.work",              name = "work-runtime-ktx",               version.ref = "workRuntime" }

[plugins]
android-application  = { id = "com.android.application",                   version.ref = "agp" }
kotlin-android       = { id = "org.jetbrains.kotlin.android",              version.ref = "kotlin" }
kotlin-serialization = { id = "org.jetbrains.kotlin.plugin.serialization", version.ref = "kotlin" }
hilt-android         = { id = "com.google.dagger.hilt.android",            version.ref = "hilt" }
ksp                  = { id = "com.google.devtools.ksp",                   version.ref = "ksp" }
''',
    'app/build.gradle.kts': '''\
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.ksp)
}

android {
    namespace  = "com.phantasia.music"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.phantasia.music"
        minSdk        = 26
        targetSdk     = 35
        versionCode   = 1
        versionName   = "1.0.0"
    }

    signingConfigs {
        create("release") {
            storeFile     = file(System.getenv("KEYSTORE_PATH")     ?: "placeholder.jks")
            storePassword = System.getenv("KEYSTORE_PASSWORD")      ?: ""
            keyAlias      = System.getenv("KEY_ALIAS")              ?: ""
            keyPassword   = System.getenv("KEY_PASSWORD")           ?: ""
        }
    }

    buildTypes {
        release {
            isMinifyEnabled   = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        debug {
            isMinifyEnabled     = false
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.sqlcipher)
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.exoplayer.dash)
    implementation(libs.media3.session)
    implementation(libs.media3.ui)
    implementation(libs.media3.datasource.cache)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client.logging)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.palette)
    implementation(libs.security.crypto)
    implementation(libs.work.runtime.ktx)
}
''',
    'app/proguard-rules.pro': '''\
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep @kotlinx.serialization.Serializable class com.phantasia.music.network.** { *; }
-keep class com.phantasia.music.storage.** { *; }
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class net.sqlcipher.** { *; }
-repackageclasses 'p'
-allowaccessmodification
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
''',
    'app/src/main/AndroidManifest.xml': '''\
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <application
        android:name=".PhantasiaApp"
        android:allowBackup="false"
        android:networkSecurityConfig="@xml/network_security_config"
        android:theme="@style/Theme.PhantasiaMusic">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <service
            android:name=".player.PhantasiaMediaService"
            android:exported="true"
            android:foregroundServiceType="mediaPlayback"
            android:stopWithTask="false">
            <intent-filter>
                <action android:name="androidx.media3.session.MediaSessionService" />
            </intent-filter>
        </service>
    </application>
</manifest>
''',
    'app/src/main/res/xml/network_security_config.xml': '''\
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">youtube.com</domain>
        <domain includeSubdomains="true">music.youtube.com</domain>
        <domain includeSubdomains="false">lrclib.net</domain>
        <pin-set expiration="2027-01-01">
            <pin digest="SHA-256">YZPgTZ+woNCCCIW3LH2CxQeLzB/1m42QcCTBSdgayjs=</pin>
            <pin digest="SHA-256">++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
''',
    'app/src/main/res/values/themes.xml': '''\
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.PhantasiaMusic" parent="android:Theme.Material.Light.NoActionBar" />
</resources>
''',
    'app/src/main/kotlin/com/phantasia/music/PhantasiaApp.kt': '''\
package com.phantasia.music

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import java.io.File
import javax.inject.Inject

@HiltAndroidApp
class PhantasiaApp : Application() {
    @Inject lateinit var okHttpClient: OkHttpClient

    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .okHttpClient(okHttpClient)
                .memoryCache {
                    MemoryCache.Builder(this).maxSizePercent(0.20).build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(File(cacheDir, "coil_cache"))
                        .maxSizeBytes(256L * 1024 * 1024)
                        .build()
                }
                .crossfade(true)
                .build()
        )
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/MainActivity.kt': '''\
package com.phantasia.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.phantasia.music.ui.PhantasiaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PhantasiaTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    AppNavigation(innerPadding = padding)
                }
            }
        }
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/AppNavigation.kt': '''\
package com.phantasia.music

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.phantasia.music.ui.HomeScreen
import com.phantasia.music.ui.LibraryScreen
import com.phantasia.music.ui.PlayerScreen
import com.phantasia.music.ui.SearchScreen

sealed class Route(val path: String) {
    object Home    : Route("home")
    object Search  : Route("search")
    object Library : Route("library")
    object Player  : Route("player/{videoId}") {
        fun build(videoId: String) = "player/$videoId"
    }
}

@Composable
fun AppNavigation(innerPadding: PaddingValues) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Route.Home.path) {
        composable(Route.Home.path)    { HomeScreen(nav, innerPadding) }
        composable(Route.Search.path)  { SearchScreen(nav, innerPadding) }
        composable(Route.Library.path) { LibraryScreen(nav, innerPadding) }
        composable(
            route     = Route.Player.path,
            arguments = listOf(navArgument("videoId") { type = NavType.StringType })
        ) { back ->
            val videoId = back.arguments?.getString("videoId") ?: return@composable
            PlayerScreen(videoId, nav, innerPadding)
        }
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/network/NetworkModels.kt': '''\
package com.phantasia.music.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackModel(
    val videoId:         String,
    val title:           String,
    val artistName:      String,
    val albumTitle:      String,
    val artworkUrl:      String,
    val durationSeconds: Long    = 0L,
    val isExplicit:      Boolean = false
)

@Serializable
data class AlbumModel(
    val browseId:     String,
    val title:        String,
    val year:         String,
    val thumbnailUrl: String,
    val artistName:   String           = "",
    val tracks:       List<TrackModel> = emptyList()
)

@Serializable
data class ArtistModel(
    val browseId:     String,
    val name:         String,
    val thumbnailUrl: String = ""
)

@Serializable
data class StreamDataModel(
    val videoId:          String,
    val streamUrl:        String,
    val itag:             Int,
    val mimeType:         String,
    val bitrate:          Long   = 0L,
    val contentLength:    Long   = 0L,
    val expiresInSeconds: Long   = 0L,
    val audioQuality:     String = ""
)

@Serializable
sealed interface SearchResultModel {
    @Serializable @SerialName("track")
    data class TrackResult(val track: TrackModel)    : SearchResultModel
    @Serializable @SerialName("album")
    data class AlbumResult(val album: AlbumModel)    : SearchResultModel
    @Serializable @SerialName("artist")
    data class ArtistResult(val artist: ArtistModel) : SearchResultModel
}
''',
    'app/src/main/kotlin/com/phantasia/music/network/JsonExtensions.kt': '''\
package com.phantasia.music.network

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

fun JsonElement?.obj(key: String): JsonElement? = runCatching {
    (this as? JsonObject)?.get(key)?.takeIf { it !is JsonNull }
}.getOrNull()

fun JsonElement?.arr(key: String): List<JsonElement>? = runCatching {
    ((this as? JsonObject)?.get(key) as? JsonArray)?.toList()
}.getOrNull()

fun JsonElement?.str(key: String): String? = runCatching {
    ((this as? JsonObject)?.get(key) as? JsonPrimitive)?.content
}.getOrNull()

fun JsonElement?.asStr(): String? = runCatching {
    (this as? JsonPrimitive)?.content
}.getOrNull()

fun JsonElement?.int(key: String): Int? = runCatching {
    ((this as? JsonObject)?.get(key) as? JsonPrimitive)?.intOrNull
}.getOrNull()

fun JsonElement?.long(key: String): Long? = runCatching {
    ((this as? JsonObject)?.get(key) as? JsonPrimitive)?.longOrNull
}.getOrNull()

fun List<JsonElement>?.idx(i: Int): JsonElement? = this?.getOrNull(i)

fun JsonElement?.runsText(): String? = runCatching {
    arr("runs")?.joinToString("") { it.str("text") ?: "" }?.takeIf { it.isNotEmpty() }
}.getOrNull()

fun JsonElement?.bestThumbnailUrl(): String? = runCatching {
    arr("thumbnails")?.mapNotNull { it.str("url") }?.lastOrNull()
}.getOrNull()
''',
    'app/src/main/kotlin/com/phantasia/music/network/InnerTubeClient.kt': '''\
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
''',
    'app/src/main/kotlin/com/phantasia/music/network/InnerTubeRequests.kt': '''\
package com.phantasia.music.network

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InnerTubeRequests @Inject constructor(
    private val client: HttpClient,
    private val locale: InnerTubeLocale
) {
    suspend fun search(query: String, cont: String? = null): HttpResponse =
        client.post("${IT.BASE}/search") {
            contentType(ContentType.Application.Json)
            setBody(buildContextPayload(locale).apply {
                put("query", query); put("params", "EgWKAQIIAWoKEAMQBBAJEAoQBQ==")
                if (cont != null) put("continuation", cont)
            })
        }

    suspend fun player(videoId: String): HttpResponse =
        client.post("${IT.BASE}/player") {
            contentType(ContentType.Application.Json)
            setBody(buildContextPayload(locale).apply {
                put("videoId", videoId)
                put("playbackContext", mapOf("contentPlaybackContext" to
                    mapOf("signatureTimestamp" to 0, "html5Preference" to "HTML5_PREF_WANTS")))
                put("racyCheckOk", true); put("contentCheckOk", true)
            })
        }

    suspend fun browse(browseId: String, cont: String? = null): HttpResponse =
        client.post("${IT.BASE}/browse") {
            contentType(ContentType.Application.Json)
            setBody(buildContextPayload(locale).apply {
                put("browseId", browseId)
                if (cont != null) put("continuation", cont)
            })
        }

    suspend fun next(videoId: String, cont: String? = null): HttpResponse =
        client.post("${IT.BASE}/next") {
            contentType(ContentType.Application.Json)
            setBody(buildContextPayload(locale).apply {
                put("videoId", videoId); put("isAudioOnly", true)
                if (cont != null) put("continuation", cont)
            })
        }

    suspend fun suggestions(query: String): HttpResponse =
        client.post("${IT.BASE}/music/get_search_suggestions") {
            contentType(ContentType.Application.Json)
            setBody(buildContextPayload(locale).apply { put("input", query) })
        }
}
''',
    'app/src/main/kotlin/com/phantasia/music/network/SearchParser.kt': '''\
package com.phantasia.music.network

import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchParser @Inject constructor() {

    fun parse(root: JsonElement): List<SearchResultModel> {
        val out = mutableListOf<SearchResultModel>()
        try {
            val sections = root.obj("contents")?.obj("tabbedSearchResultsRenderer")
                ?.arr("tabs")?.idx(0)?.obj("tabRenderer")?.obj("content")
                ?.obj("sectionListRenderer")?.arr("contents") ?: return out
            for (section in sections) {
                val contents = section.obj("musicShelfRenderer")?.arr("contents") ?: continue
                for (item in contents) {
                    val r = item.obj("musicResponsiveListItemRenderer") ?: continue
                    parseItem(r)?.let { out.add(it) }
                }
            }
        } catch (_: Exception) {}
        return out
    }

    private fun parseItem(r: JsonElement): SearchResultModel? = try {
        val cols  = r.arr("flexColumns") ?: return null
        val col0  = cols.idx(0)?.obj("musicResponsiveListItemFlexColumnRenderer")
        val col1  = cols.idx(1)?.obj("musicResponsiveListItemFlexColumnRenderer")
        val title = col0?.obj("text")?.runsText() ?: return null
        val thumb = r.obj("thumbnail")?.obj("musicThumbnailRenderer")
            ?.obj("thumbnail")?.bestThumbnailUrl() ?: ""
        val pageType = col0?.obj("text")?.arr("runs")?.idx(0)
            ?.obj("navigationEndpoint")?.obj("browseEndpoint")
            ?.obj("browseEndpointContextSupportedConfigs")
            ?.obj("browseEndpointContextMusicConfig")?.str("pageType")
            ?: col0?.obj("text")?.arr("runs")?.idx(0)
                ?.obj("navigationEndpoint")?.obj("watchEndpoint")
                ?.str("videoId")?.let { "SONG" } ?: "SONG"
        when {
            pageType.contains("ALBUM") || pageType.contains("PLAYLIST") -> {
                val bid = col0?.obj("text")?.arr("runs")?.idx(0)
                    ?.obj("navigationEndpoint")?.obj("browseEndpoint")?.str("browseId") ?: return null
                SearchResultModel.AlbumResult(AlbumModel(bid, title,
                    col1?.obj("text")?.arr("runs")?.lastOrNull()?.asStr() ?: "",
                    thumb,
                    col1?.obj("text")?.arr("runs")?.idx(0)?.asStr() ?: ""))
            }
            pageType.contains("ARTIST") -> {
                val bid = col0?.obj("text")?.arr("runs")?.idx(0)
                    ?.obj("navigationEndpoint")?.obj("browseEndpoint")?.str("browseId") ?: return null
                SearchResultModel.ArtistResult(ArtistModel(bid, title, thumb))
            }
            else -> {
                val vid = col0?.obj("text")?.arr("runs")?.idx(0)
                    ?.obj("navigationEndpoint")?.obj("watchEndpoint")?.str("videoId") ?: return null
                val artist = col1?.obj("text")?.arr("runs")?.idx(0)?.asStr() ?: "Unknown"
                val album  = col1?.obj("text")?.arr("runs")?.idx(2)?.asStr() ?: ""
                val dur    = col1?.obj("text")?.arr("runs")?.lastOrNull()?.asStr() ?: "0:00"
                SearchResultModel.TrackResult(TrackModel(vid, title, artist, album, thumb, dur(dur)))
            }
        }
    } catch (_: Exception) { null }

    private fun dur(raw: String): Long = runCatching {
        val p = raw.trim().split(":").map { it.toLong() }
        when (p.size) { 2 -> p[0]*60+p[1]; 3 -> p[0]*3600+p[1]*60+p[2]; else -> 0L }
    }.getOrDefault(0L)
}
''',
    'app/src/main/kotlin/com/phantasia/music/network/PlayerParser.kt': '''\
package com.phantasia.music.network

import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerParser @Inject constructor() {

    private val ITAG_PRIO = listOf(141, 251, 140, 250, 249, 139)

    fun parse(videoId: String, root: JsonElement): StreamDataModel? = runCatching {
        val sd = root.obj("streamingData") ?: return null
        val fmts = sd.arr("adaptiveFormats")?.ifEmpty { null } ?: sd.arr("formats") ?: return null
        val byItag = fmts.mapNotNull { f ->
            val itag = f.int("itag") ?: return@mapNotNull null
            val mime = f.str("mimeType") ?: return@mapNotNull null
            if (!mime.startsWith("audio/")) return@mapNotNull null
            itag to f
        }.toMap()
        val (itag, f) = ITAG_PRIO.firstNotNullOfOrNull { t -> byItag[t]?.let { t to it } }
            ?: byItag.entries.firstOrNull()?.let { it.key to it.value } ?: return null
        StreamDataModel(videoId, f.str("url") ?: return null, itag,
            f.str("mimeType") ?: "", f.long("bitrate") ?: 0L,
            f.str("contentLength")?.toLongOrNull() ?: 0L,
            f.str("approxDurationMs")?.toLongOrNull()?.div(1000) ?: 3600L,
            f.str("audioQuality") ?: "")
    }.getOrNull()
}
''',
    'app/src/main/kotlin/com/phantasia/music/network/AlbumParser.kt': '''\
package com.phantasia.music.network

import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlbumParser @Inject constructor() {

    fun parse(browseId: String, root: JsonElement): AlbumModel? = runCatching {
        val header = root.obj("header")?.obj("musicDetailHeaderRenderer")
            ?: root.obj("header")?.obj("musicImmersiveHeaderRenderer") ?: return null
        val sub    = header.obj("subtitle")?.arr("runs")
        AlbumModel(
            browseId     = browseId,
            title        = header.obj("title")?.runsText() ?: "Unknown",
            year         = sub?.lastOrNull()?.asStr() ?: "",
            thumbnailUrl = header.obj("thumbnail")?.obj("croppedSquareThumbnailRenderer")
                ?.obj("thumbnail")?.bestThumbnailUrl()
                ?: header.obj("thumbnail")?.obj("musicThumbnailRenderer")
                    ?.obj("thumbnail")?.bestThumbnailUrl() ?: "",
            artistName   = sub?.idx(0)?.asStr() ?: "",
            tracks       = parseTracks(root)
        )
    }.getOrNull()

    private fun parseTracks(root: JsonElement): List<TrackModel> = runCatching {
        root.obj("contents")?.obj("singleColumnBrowseResultsRenderer")
            ?.arr("tabs")?.idx(0)?.obj("tabRenderer")?.obj("content")
            ?.obj("sectionListRenderer")?.arr("contents")?.idx(0)
            ?.obj("musicShelfRenderer")?.arr("contents")
            ?.mapNotNull { item ->
                val r    = item.obj("musicResponsiveListItemRenderer") ?: return@mapNotNull null
                val cols = r.arr("flexColumns") ?: return@mapNotNull null
                val title = cols.idx(0)?.obj("musicResponsiveListItemFlexColumnRenderer")
                    ?.obj("text")?.runsText() ?: return@mapNotNull null
                val vid   = cols.idx(0)?.obj("musicResponsiveListItemFlexColumnRenderer")
                    ?.obj("text")?.arr("runs")?.idx(0)
                    ?.obj("navigationEndpoint")?.obj("watchEndpoint")?.str("videoId") ?: return@mapNotNull null
                val artist = cols.idx(1)?.obj("musicResponsiveListItemFlexColumnRenderer")
                    ?.obj("text")?.arr("runs")?.idx(0)?.asStr() ?: ""
                val dur    = cols.idx(1)?.obj("musicResponsiveListItemFlexColumnRenderer")
                    ?.obj("text")?.arr("runs")?.lastOrNull()?.asStr() ?: "0:00"
                val thumb  = r.obj("thumbnail")?.obj("musicThumbnailRenderer")
                    ?.obj("thumbnail")?.bestThumbnailUrl() ?: ""
                val dSec   = runCatching { val p = dur.split(":").map { it.toLong() }
                    when (p.size) { 2 -> p[0]*60+p[1]; 3 -> p[0]*3600+p[1]*60+p[2]; else -> 0L } }.getOrDefault(0L)
                TrackModel(vid, title, artist, "", thumb, dSec)
            } ?: emptyList()
    }.getOrDefault(emptyList())
}
''',
    'app/src/main/kotlin/com/phantasia/music/network/CipherEngine.kt': r'''
package com.phantasia.music.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

sealed interface CipherOp {
    object Reverse                : CipherOp
    data class Splice(val n: Int) : CipherOp
    data class Swap(val b: Int)   : CipherOp
}

@Singleton
class CipherEngine @Inject constructor(private val client: HttpClient) {

    private val opsCache = ConcurrentHashMap<String, List<CipherOp>>()
    private val nCache   = ConcurrentHashMap<String, String>()

    suspend fun decryptSignature(sig: String, playerUrl: String): String {
        if (sig.isBlank()) return sig
        val ops = opsCache.getOrPut(playerUrl) {
            runCatching { parseOps(client.get(playerUrl).bodyAsText()) }.getOrDefault(emptyList())
        }
        return applyOps(sig, ops)
    }

    suspend fun decryptNParam(n: String, playerUrl: String): String {
        if (n.isBlank()) return n
        return nCache.getOrPut(playerUrl + "::" + n) {
            runCatching { extractN(client.get(playerUrl).bodyAsText(), n) }.getOrDefault(n)
        }
    }

    suspend fun resolveStreamUrl(rawUrl: String, playerUrl: String): String {
        var url = rawUrl
        Regex("[?&]n=([^&]+)").find(url)?.let {
            val raw = it.groupValues[1]
            val dec = decryptNParam(raw, playerUrl)
            if (dec != raw) url = url.replace("n=" + raw, "n=" + dec)
        }
        Regex("[?&]s=([^&]+)").find(url)?.let {
            val raw = it.groupValues[1]
            val dec = decryptSignature(raw, playerUrl)
            url = url.replace("s=" + raw, "").trimEnd('&', '?') + "&sig=" + dec
        }
        return url
    }

    private fun parseOps(js: String): List<CipherOp> {
        val ops  = mutableListOf<CipherOp>()
        val id   = "[a-zA-Z_][a-zA-Z0-9_]*"
        val body = Regex(id + "=function\\(a\\)\\{a=a\\.split\\(\\\"\\\")(.*?)return a\\.join\\(\\\"\\\"\\\)\\}")
            .find(js)?.groupValues?.getOrNull(1) ?: return ops
        val hName = Regex(";($" + id + ")\\.")
            .find(body)?.groupValues?.getOrNull(1) ?: return ops
        val esc   = Regex.escapeReplacement(hName)
        val hBody = Regex("var " + esc + "=\\{(.+?)\\};", RegexOption.DOT_MATCHES_ALL)
            .find(js)?.groupValues?.getOrNull(1) ?: return ops
        val revF = Regex("(" + id + ")\\.function\\(a\\)\\{a\\.reverse\\(\\)"\\"").find(hBody)?.groupValues?.getOrNull(1)
        val splF = Regex("(" + id + ")\\.function\\(a,b\\)\\{a\\.splice\\(0"\\"").find(hBody)?.groupValues?.getOrNull(1)
        val swpF = Regex("(" + id + ")\\.function\\(a,b\\)\\{var c=a"\\"").find(hBody)?.groupValues?.getOrNull(1)
        Regex(esc + "\\." + id + "\\(a,(\\\\d+)\\)").findAll(body).forEach { c ->
            val m = c.groupValues[1]; val n = c.groupValues[2].toIntOrNull() ?: return@forEach
            when (m) { revF->ops.add(CipherOp.Reverse); splF->ops.add(CipherOp.Splice(n)); swpF->ops.add(CipherOp.Swap(n)) }
        }
        return ops
    }

    private fun applyOps(sig: String, ops: List<CipherOp>): String {
        val c = sig.toMutableList()
        for (op in ops) when (op) {
            is CipherOp.Reverse -> c.reverse()
            is CipherOp.Splice  -> repeat(op.n.coerceAtMost(c.size)) { c.removeAt(0) }
            is CipherOp.Swap    -> if (c.size>1) { val i=op.b%c.size; val t=c[0]; c[0]=c[i]; c[i]=t }
        }
        return c.joinToString("")
    }

    private fun extractN(js: String, n: String): String = try {
        val id   = "[a-zA-Z_][a-zA-Z0-9_]*"
        val name = Regex("\\.get\\(\\\"n\\\"\\)\\)\\&\\&\\(b=(" + id + ")\\[\\\"\\\"").find(js)?.groupValues?.getOrNull(1) ?: return n
        val esc  = Regex.escapeReplacement(name)
        val funcBody = Regex(esc + "\\s*=\\s*function\\s*\\(a\\)\\s*\\{(.+?);\\s*return\\s*b\\.join\\(\\\"\\\")\\s*\\}",
            RegexOption.DOT_MATCHES_ALL).find(js)?.groupValues?.getOrNull(1) ?: return n
        val eng = javax.script.ScriptEngineManager().getEngineByName("rhino") ?: return n
        (eng.eval("(function(a){" + funcBody + ";return b.join(\"\")})('" + n + "')") as? String ?: n
    } catch (_: Exception) { n }
}
''',
    'app/src/main/kotlin/com/phantasia/music/network/MusicRepository.kt': '''\
package com.phantasia.music.network

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

interface MusicRepository {
    suspend fun search(query: String): List<SearchResultModel>
    suspend fun getStream(videoId: String): StreamDataModel?
    suspend fun getAlbum(browseId: String): AlbumModel?
    suspend fun getSuggestions(videoId: String): List<TrackModel>
    suspend fun getSearchSuggestions(query: String): List<String>
}

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val req:     InnerTubeRequests,
    private val sParse:  SearchParser,
    private val pParse:  PlayerParser,
    private val aParse:  AlbumParser,
    private val cipher:  CipherEngine
) : MusicRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    override suspend fun search(query: String) = runCatching {
        sParse.parse(json.parseToJsonElement(req.search(query).bodyAsText()))
    }.getOrDefault(emptyList())

    override suspend fun getStream(videoId: String) = runCatching {
        val root = json.parseToJsonElement(req.player(videoId).bodyAsText())
        val raw  = pParse.parse(videoId, root) ?: return@runCatching null
        raw.copy(streamUrl = cipher.resolveStreamUrl(raw.streamUrl, IT.PLAYER))
    }.getOrNull()

    override suspend fun getAlbum(browseId: String) = runCatching {
        aParse.parse(browseId, json.parseToJsonElement(req.browse(browseId).bodyAsText()))
    }.getOrNull()

    override suspend fun getSuggestions(videoId: String) = runCatching {
        sParse.parse(json.parseToJsonElement(req.next(videoId).bodyAsText()))
            .filterIsInstance<SearchResultModel.TrackResult>().map { it.track }
    }.getOrDefault(emptyList())

    override suspend fun getSearchSuggestions(query: String) = runCatching {
        val body = json.parseToJsonElement(req.suggestions(query).bodyAsText())
        body.arr("contents")?.flatMap { s ->
            s.obj("searchSuggestionsSectionRenderer")?.arr("contents")
                ?.mapNotNull { it.obj("searchSuggestionRenderer")?.obj("suggestion")?.runsText() }
                ?: emptyList()
        } ?: emptyList()
    }.getOrDefault(emptyList())
}

@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository
}
''',
    'app/src/main/kotlin/com/phantasia/music/storage/SongStore.kt': '''\
package com.phantasia.music.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val videoId: String,
    val title: String, val artistName: String,
    val albumTitle: String, val artworkUrl: String,
    val durationSeconds: Long,
    @ColumnInfo(defaultValue = "0") val isFavourite: Int = 0,
    val cachedAt: Long = System.currentTimeMillis()
)

@Dao interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(s: SongEntity)
    @Query("DELETE FROM songs WHERE videoId = :id") suspend fun delete(id: String)
    @Query("SELECT * FROM songs WHERE videoId = :id LIMIT 1") suspend fun getById(id: String): SongEntity?
    @Query("SELECT * FROM songs WHERE isFavourite = 1 ORDER BY cachedAt DESC") fun getFavourites(): Flow<List<SongEntity>>
    @Query("SELECT * FROM songs ORDER BY cachedAt DESC") fun getAll(): Flow<List<SongEntity>>
    @Query("UPDATE songs SET isFavourite = :fav WHERE videoId = :id") suspend fun setFavourite(id: String, fav: Int)
}
''',
    'app/src/main/kotlin/com/phantasia/music/storage/PlaylistStore.kt': '''\
package com.phantasia.music.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val playlistId: Long = 0,
    val name: String, val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlist_song_cross_ref", primaryKeys = ["playlistId", "videoId"])
data class PlaylistSongCrossRef(val playlistId: Long, val videoId: String)

data class PlaylistWithSongs(
    @Embedded val playlist: PlaylistEntity,
    @Relation(parentColumn = "playlistId", entityColumn = "videoId",
        associateBy = Junction(PlaylistSongCrossRef::class))
    val songs: List<SongEntity>
)

@Dao interface PlaylistDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun create(p: PlaylistEntity): Long
    @Delete suspend fun delete(p: PlaylistEntity)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun addSong(x: PlaylistSongCrossRef)
    @Delete suspend fun removeSong(x: PlaylistSongCrossRef)
    @Transaction @Query("SELECT * FROM playlists ORDER BY createdAt DESC")
    fun getAll(): Flow<List<PlaylistWithSongs>>
}
''',
    'app/src/main/kotlin/com/phantasia/music/storage/SearchHistoryStore.kt': '''\
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
''',
    'app/src/main/kotlin/com/phantasia/music/storage/PhantasiaDatabase.kt': '''\
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
''',
    'app/src/main/kotlin/com/phantasia/music/storage/MediaCacheManager.kt': '''\
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
''',
    'app/src/main/kotlin/com/phantasia/music/security/SecurePreferenceManager.kt': '''\
package com.phantasia.music.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferenceManager @Inject constructor(@ApplicationContext private val ctx: Context) {
    private val prefs: SharedPreferences by lazy {
        val key = MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(ctx, "phantasia_secure_prefs", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }
    fun putString(k: String, v: String)            { prefs.edit().putString(k, v).apply() }
    fun getString(k: String, d: String? = null)    = prefs.getString(k, d)
    fun putBoolean(k: String, v: Boolean)          { prefs.edit().putBoolean(k, v).apply() }
    fun getBoolean(k: String, d: Boolean = false)  = prefs.getBoolean(k, d)
    fun remove(k: String)                          { prefs.edit().remove(k).apply() }
}

@Module @InstallIn(SingletonComponent::class)
object SecurityModule {
    @Provides @Singleton
    fun provideSecurePrefs(@ApplicationContext ctx: Context) = SecurePreferenceManager(ctx)
}
''',
    'app/src/main/kotlin/com/phantasia/music/security/EnvironmentGuard.kt': '''\
package com.phantasia.music.security

import android.os.Build
import android.os.Debug
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class IntegrityResult(val passed: Boolean, val reason: String?)

@Singleton
class EnvironmentGuard @Inject constructor() {
    fun checkIntegrity(): IntegrityResult {
        if (isRooted())            return IntegrityResult(false, "Root detected")
        if (isTestKeysBuild())     return IntegrityResult(false, "Test-keys build")
        if (isDebuggerConnected()) return IntegrityResult(false, "Debugger attached")
        return IntegrityResult(true, null)
    }
    private fun isRooted() = listOf(
        "/system/xbin/su", "/system/bin/su", "/sbin/su",
        "/su/bin/su", "/data/local/xbin/su"
    ).any { File(it).exists() }
    private fun isTestKeysBuild()     = Build.TAGS?.contains("test-keys") == true
    private fun isDebuggerConnected() = Debug.isDebuggerConnected() || Debug.waitingForDebugger()

    fun sslPinningInterceptor(): Interceptor {
        val pinner = CertificatePinner.Builder()
            .add("*.youtube.com",     "sha256/YZPgTZ+woNCCCIW3LH2CxQeLzB/1m42QcCTBSdgayjs=")
            .add("*.youtube.com",     "sha256/++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI=")
            .add("music.youtube.com", "sha256/YZPgTZ+woNCCCIW3LH2CxQeLzB/1m42QcCTBSdgayjs=")
            .build()
        return Interceptor { chain ->
            val req = chain.request()
            pinner.check(req.url.host, chain.connection()!!.handshake()!!.peerCertificates())
            chain.proceed(req)
        }
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/player/PhantasiaMediaService.kt': '''\
package com.phantasia.music.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PhantasiaMediaService : MediaSessionService() {
    @Inject lateinit var exoPlayer: ExoPlayer
    private var mediaSession: MediaSession? = null

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(NotificationChannel(
                    "phantasia_playback", "Phantasia Playback",
                    NotificationManager.IMPORTANCE_LOW))
        }
        mediaSession = MediaSession.Builder(this, exoPlayer).setId("PhantasiaSession").build()
    }
    override fun onGetSession(info: MediaSession.ControllerInfo) = mediaSession
    override fun onDestroy() {
        mediaSession?.run { player.release(); release() }
        mediaSession = null; super.onDestroy()
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/player/PlayerCore.kt': '''\
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
    fun provideCacheFactory(cache: SimpleCache): CacheDataSource.Factory =
        CacheDataSource.Factory().setCache(cache)
            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory()
                .setUserAgent("PhantasiaMusic/1.0")
                .setConnectTimeoutMs(15_000).setReadTimeoutMs(20_000))
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

    @androidx.annotation.OptIn(UnstableApi::class)
    @Provides @Singleton
    fun provideExoPlayer(@ApplicationContext ctx: Context, f: CacheDataSource.Factory): ExoPlayer =
        ExoPlayer.Builder(ctx).setMediaSourceFactory(DefaultMediaSourceFactory(f))
            .setAudioAttributes(AudioAttributes.Builder()
                .setUsage(C.USAGE_MEDIA).setContentType(C.AUDIO_CONTENT_TYPE_MUSIC).build(), true)
            .setHandleAudioBecomingNoisy(true).build()
}
''',
    'app/src/main/kotlin/com/phantasia/music/player/QueueManager.kt': '''\
package com.phantasia.music.player

import com.phantasia.music.network.TrackModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

enum class RepeatMode { NONE, ONE, ALL }

data class QueueState(
    val tracks: List<TrackModel> = emptyList(),
    val currentIndex: Int = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.NONE
) {
    val currentTrack get() = tracks.getOrNull(currentIndex)
    val hasNext get() = currentIndex < tracks.size - 1 || repeatMode == RepeatMode.ALL
    val hasPrev get() = currentIndex > 0
}

@Singleton
class QueueManager @Inject constructor() {
    private val _state = MutableStateFlow(QueueState())
    val state: StateFlow<QueueState> = _state.asStateFlow()

    fun setQueue(tracks: List<TrackModel>, startIndex: Int = 0) {
        _state.value = _state.value.copy(tracks = tracks,
            currentIndex = startIndex.coerceIn(0, tracks.lastIndex.coerceAtLeast(0)))
    }
    fun addTrack(t: TrackModel) { _state.value = _state.value.copy(tracks = _state.value.tracks + t) }
    fun skipToNext() {
        val s = _state.value
        val next = when {
            s.currentIndex < s.tracks.lastIndex -> s.currentIndex + 1
            s.repeatMode == RepeatMode.ALL -> 0
            else -> return
        }
        _state.value = s.copy(currentIndex = next)
    }
    fun skipToPrev() { val s = _state.value; if (s.hasPrev) _state.value = s.copy(currentIndex = s.currentIndex - 1) }
    fun toggleShuffle() { _state.value = _state.value.copy(shuffleEnabled = !_state.value.shuffleEnabled) }
    fun cycleRepeat() {
        val next = RepeatMode.values()[(_state.value.repeatMode.ordinal + 1) % RepeatMode.values().size]
        _state.value = _state.value.copy(repeatMode = next)
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/player/PlayerStateHolder.kt': '''\
package com.phantasia.music.player

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.phantasia.music.network.TrackModel
import com.phantasia.music.ui.PlayerUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerStateHolder @Inject constructor(private val player: ExoPlayer) {
    private val _state = MutableStateFlow<PlayerUiState>(PlayerUiState.Idle)
    val uiState: StateFlow<PlayerUiState> = _state.asStateFlow()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var progressJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(s: Int) { sync() }
            override fun onIsPlayingChanged(playing: Boolean) { sync(); if (playing) startProgress() else progressJob?.cancel() }
        })
    }

    fun loadTrack(track: TrackModel, url: String) {
        _state.value = PlayerUiState.Loading
        player.setMediaItem(MediaItem.fromUri(url)); player.prepare(); player.play()
        _state.value = PlayerUiState.Playing(track, 0L, player.duration.takeIf { it>0 } ?: 0L, true, false, RepeatMode.NONE)
    }

    private fun sync() {
        val c = _state.value
        if (c is PlayerUiState.Playing) _state.value = c.copy(
            isPlaying = player.isPlaying,
            durationMs = player.duration.takeIf { it>0 } ?: c.durationMs)
    }

    private fun startProgress() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                (_state.value as? PlayerUiState.Playing)?.let { _state.value = it.copy(positionMs = player.currentPosition) }
                delay(500)
            }
        }
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/PlayerState.kt': '''\
package com.phantasia.music.ui

import com.phantasia.music.network.TrackModel
import com.phantasia.music.player.RepeatMode

sealed interface PlayerUiState {
    object Idle    : PlayerUiState
    object Loading : PlayerUiState
    data class Playing(
        val track: TrackModel,
        val positionMs: Long,
        val durationMs: Long,
        val isPlaying: Boolean,
        val shuffleEnabled: Boolean,
        val repeatMode: RepeatMode
    ) : PlayerUiState
}

sealed interface PlayerUiEvent {
    object Play; object Pause; object SkipNext; object SkipPrev
    object ToggleShuffle; object CycleRepeat; object ToggleFavourite
    data class Seek(val positionMs: Long)       : PlayerUiEvent
    data class PlayTrack(val track: TrackModel) : PlayerUiEvent
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/SearchState.kt': '''\
package com.phantasia.music.ui

import com.phantasia.music.network.SearchResultModel

sealed interface SearchUiState {
    object Idle; object Loading
    data class Results(val items: List<SearchResultModel>) : SearchUiState
    data class Error(val message: String)                  : SearchUiState
}
sealed interface SearchUiEvent {
    data class QueryChanged(val query: String)    : SearchUiEvent
    data class TrackSelected(val videoId: String) : SearchUiEvent
    object ClearHistory                           : SearchUiEvent
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/Theme.kt': '''\
package com.phantasia.music.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

private val Dark  = darkColorScheme(primary=Color(0xFFCFBCFF), onPrimary=Color(0xFF381E72),
    primaryContainer=Color(0xFF4F378A), background=Color(0xFF1C1B1F), surface=Color(0xFF1C1B1F))
private val Light = lightColorScheme(primary=Color(0xFF6650A4), onPrimary=Color(0xFFFFFFFF),
    primaryContainer=Color(0xFFEADDFF), background=Color(0xFFFFFBFE), surface=Color(0xFFFFFBFE))
private val Type  = Typography(
    bodyLarge   = TextStyle(fontWeight=FontWeight.Normal,   fontSize=16.sp, lineHeight=24.sp),
    titleMedium = TextStyle(fontWeight=FontWeight.Medium,   fontSize=16.sp, lineHeight=24.sp),
    titleLarge  = TextStyle(fontWeight=FontWeight.SemiBold, fontSize=22.sp, lineHeight=28.sp),
    labelSmall  = TextStyle(fontWeight=FontWeight.Medium,   fontSize=11.sp, lineHeight=16.sp))

@Composable
fun PhantasiaTheme(dark: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val cs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val ctx = LocalContext.current
        if (dark) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
    } else if (dark) Dark else Light
    val view = LocalView.current
    if (!view.isInEditMode) SideEffect {
        val w = (view.context as Activity).window
        w.statusBarColor = cs.background.toArgb()
        WindowCompat.getInsetsController(w, view).isAppearanceLightStatusBars = !dark
    }
    MaterialTheme(colorScheme = cs, typography = Type, content = content)
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/PlayerViewModel.kt': '''\
package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import com.phantasia.music.network.MusicRepository
import com.phantasia.music.network.TrackModel
import com.phantasia.music.player.PlayerStateHolder
import com.phantasia.music.player.QueueManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val repo: MusicRepository,
    private val sh: PlayerStateHolder,
    private val player: ExoPlayer,
    private val queue: QueueManager
) : ViewModel() {
    val uiState: StateFlow<PlayerUiState> = sh.uiState

    fun onEvent(e: PlayerUiEvent) = when (e) {
        is PlayerUiEvent.Play          -> player.play()
        is PlayerUiEvent.Pause         -> player.pause()
        is PlayerUiEvent.SkipNext      -> { queue.skipToNext(); playCurrentTrack() }
        is PlayerUiEvent.SkipPrev      -> { queue.skipToPrev(); playCurrentTrack() }
        is PlayerUiEvent.Seek          -> player.seekTo(e.positionMs)
        is PlayerUiEvent.ToggleShuffle -> queue.toggleShuffle()
        is PlayerUiEvent.CycleRepeat   -> queue.cycleRepeat()
        is PlayerUiEvent.PlayTrack     -> loadAndPlay(e.track)
        is PlayerUiEvent.ToggleFavourite -> Unit
        else -> Unit
    }

    private fun loadAndPlay(t: TrackModel) = viewModelScope.launch {
        val url = repo.getStream(t.videoId)?.streamUrl ?: return@launch
        sh.loadTrack(t, url)
    }
    private fun playCurrentTrack() { queue.state.value.currentTrack?.let { loadAndPlay(it) } }
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/SearchViewModel.kt': '''\
package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantasia.music.network.MusicRepository
import com.phantasia.music.storage.SearchHistoryDao
import com.phantasia.music.storage.SearchHistoryEntity
import com.phantasia.music.storage.SearchHistoryType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repo: MusicRepository,
    private val dao: SearchHistoryDao
) : ViewModel() {
    private val _state = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _state.asStateFlow()
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    val history = dao.getRecent(10)

    init {
        _query.debounce(400).distinctUntilChanged().filter { it.length > 1 }
            .onEach { q ->
                _state.value = SearchUiState.Loading
                _state.value = runCatching { SearchUiState.Results(repo.search(q)) }
                    .getOrElse { SearchUiState.Error(it.message ?: "Error") }
            }.launchIn(viewModelScope)
    }

    fun onEvent(e: SearchUiEvent) = when (e) {
        is SearchUiEvent.QueryChanged  -> _query.value = e.query
        is SearchUiEvent.ClearHistory  -> viewModelScope.launch { dao.clearAll() }.let {}
        is SearchUiEvent.TrackSelected -> viewModelScope.launch {
            dao.insert(SearchHistoryEntity(query = e.videoId, type = SearchHistoryType.TRACK)) }.let {}
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/LibraryViewModel.kt': '''\
package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantasia.music.storage.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val songDao: SongDao,
    private val playlistDao: PlaylistDao
) : ViewModel() {
    val favourites = songDao.getFavourites()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val playlists: Flow<List<PlaylistWithSongs>> = playlistDao.getAll()

    fun toggleFavourite(s: SongEntity) = viewModelScope.launch {
        songDao.setFavourite(s.videoId, if (s.isFavourite == 1) 0 else 1) }
    fun createPlaylist(name: String) = viewModelScope.launch { playlistDao.create(PlaylistEntity(name = name)) }
    fun addToPlaylist(playlistId: Long, videoId: String) = viewModelScope.launch {
        playlistDao.addSong(PlaylistSongCrossRef(playlistId, videoId)) }
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/HomeScreen.kt': '''\
package com.phantasia.music.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.phantasia.music.Route

@Composable
fun HomeScreen(nav: NavController, padding: PaddingValues) {
    LazyColumn(modifier=Modifier.fillMaxSize().padding(padding), contentPadding=PaddingValues(16.dp)) {
        item { Text("Phantasia Music", style=MaterialTheme.typography.titleLarge, modifier=Modifier.padding(bottom=24.dp)) }
        item { OutlinedCard(modifier=Modifier.fillMaxWidth().padding(bottom=8.dp),
            onClick={ nav.navigate(Route.Search.path) }) { Text("Search for music", modifier=Modifier.padding(16.dp)) } }
        item { OutlinedCard(modifier=Modifier.fillMaxWidth(),
            onClick={ nav.navigate(Route.Library.path) }) { Text("Your Library", modifier=Modifier.padding(16.dp)) } }
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/SearchScreen.kt': '''\
package com.phantasia.music.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.phantasia.music.Route
import com.phantasia.music.network.SearchResultModel

@Composable
fun SearchScreen(nav: NavController, padding: PaddingValues) {
    val vm: SearchViewModel = hiltViewModel()
    val query by vm.query.collectAsState()
    val state by vm.uiState.collectAsState()
    LazyColumn(modifier=Modifier.fillMaxSize().padding(padding), contentPadding=PaddingValues(16.dp)) {
        item {
            OutlinedTextField(value=query, onValueChange={ vm.onEvent(SearchUiEvent.QueryChanged(it)) },
                placeholder={ Text("Search songs, artists…") }, modifier=Modifier.fillMaxWidth().padding(bottom=12.dp))
        }
        when (val s = state) {
            is SearchUiState.Loading -> item { CircularProgressIndicator(Modifier.padding(16.dp)) }
            is SearchUiState.Error   -> item { Text("Error: ${s.message}", color=MaterialTheme.colorScheme.error) }
            is SearchUiState.Results -> items(s.items.size) { i ->
                (s.items[i] as? SearchResultModel.TrackResult)?.let { r ->
                    TrackCard(r.track) { nav.navigate(Route.Player.build(r.track.videoId)) }
                }
            }
            else -> {}
        }
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/PlayerScreen.kt': '''\
package com.phantasia.music.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.phantasia.music.network.TrackModel

@Composable
fun PlayerScreen(videoId: String, nav: NavController, padding: PaddingValues) {
    val vm: PlayerViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    LaunchedEffect(videoId) { vm.onEvent(PlayerUiEvent.PlayTrack(TrackModel(videoId,"Loading…","","","",0))) }
    Box(modifier=Modifier.fillMaxSize().padding(padding), contentAlignment=Alignment.Center) {
        when (val s = state) {
            is PlayerUiState.Idle    -> Text("Ready")
            is PlayerUiState.Loading -> CircularProgressIndicator()
            is PlayerUiState.Playing -> { DynamicBackground(s.track.artworkUrl); PlayerComponents(s, vm::onEvent) }
        }
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/LibraryScreen.kt': '''\
package com.phantasia.music.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun LibraryScreen(nav: NavController, padding: PaddingValues) {
    val vm: LibraryViewModel = hiltViewModel()
    val favs by vm.favourites.collectAsState()
    LazyColumn(modifier=Modifier.fillMaxSize().padding(padding), contentPadding=PaddingValues(16.dp)) {
        item { Text("Library", style=MaterialTheme.typography.titleLarge, modifier=Modifier.padding(bottom=16.dp)) }
        items(favs.size) { i -> Text(favs[i].title, modifier=Modifier.padding(vertical=6.dp)); Divider() }
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/DynamicBackground.kt': '''\
package com.phantasia.music.ui

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.imageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult

@Composable
fun DynamicBackground(artworkUrl: String, modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    var dominant by remember { mutableStateOf(Color(0xFF1C1B1F)) }
    var vibrant  by remember { mutableStateOf(Color(0xFF4F378A)) }
    LaunchedEffect(artworkUrl) {
        if (artworkUrl.isBlank()) return@LaunchedEffect
        runCatching {
            val r = ctx.imageLoader.execute(ImageRequest.Builder(ctx).data(artworkUrl).allowHardware(false).build())
            if (r is SuccessResult) {
                val bmp: Bitmap = (r.drawable as? BitmapDrawable)?.bitmap ?: return@runCatching
                Palette.from(bmp).generate { p ->
                    p?.dominantSwatch?.rgb?.let { dominant = Color(it) }
                    p?.vibrantSwatch?.rgb?.let  { vibrant  = Color(it) }
                }
            }
        }
    }
    val animDom by animateColorAsState(dominant, tween(800), label="dom")
    val animVib by animateColorAsState(vibrant,  tween(800), label="vib")
    Box(modifier=modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(animVib.copy(alpha=0.65f), animDom.copy(alpha=0.95f), Color.Black))))
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/LyricsDisplay.kt': '''\
package com.phantasia.music.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class LrcLine(val timeMs: Long, val text: String)

fun parseLrc(raw: String): List<LrcLine> {
    val rx = Regex("\\[(\\d+):(\\d+)\\.(\\d+)](.*)")
    return raw.lines().mapNotNull { l ->
        val m = rx.find(l.trim()) ?: return@mapNotNull null
        val (min,sec,cs,text) = m.destructured
        LrcLine((min.toLong()*60_000)+(sec.toLong()*1_000)+(cs.toLong()*10), text.trim())
    }.sortedBy { it.timeMs }
}

@Composable
fun LyricsDisplay(lines: List<LrcLine>, positionMs: Long, modifier: Modifier = Modifier) {
    val ls = rememberLazyListState()
    val ai = remember(positionMs) { lines.indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0) }
    LaunchedEffect(ai) { if (lines.isNotEmpty()) ls.animateScrollToItem(ai, -200) }
    LazyColumn(state=ls, modifier=modifier) {
        items(lines.size) { i ->
            val a = i == ai
            Text(lines[i].text, style=MaterialTheme.typography.bodyLarge.copy(
                fontWeight=if(a) FontWeight.Bold else FontWeight.Normal,
                color=if(a) MaterialTheme.colorScheme.primary else Color.White.copy(alpha=0.5f)),
                textAlign=TextAlign.Center,
                modifier=Modifier.fillMaxWidth().padding(vertical=6.dp, horizontal=24.dp))
        }
    }
}
''',
    'app/src/main/kotlin/com/phantasia/music/ui/PlayerComponents.kt': '''\
package com.phantasia.music.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.phantasia.music.network.TrackModel

@Composable
fun TrackCard(track: TrackModel, onClick: () -> Unit) {
    Row(modifier=Modifier.fillMaxWidth().clickable{onClick()}.padding(horizontal=16.dp, vertical=8.dp),
        verticalAlignment=Alignment.CenterVertically) {
        AsyncImage(model=track.artworkUrl, contentDescription=track.title,
            contentScale=ContentScale.Crop, modifier=Modifier.size(48.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(track.title,      style=MaterialTheme.typography.titleMedium, maxLines=1)
            Text(track.artistName, style=MaterialTheme.typography.bodySmall,   maxLines=1)
        }
    }
}

@Composable
fun PlaybackControls(state: PlayerUiState.Playing, onEvent: (PlayerUiEvent) -> Unit) {
    Column(modifier=Modifier.fillMaxWidth().padding(horizontal=24.dp),
        horizontalAlignment=Alignment.CenterHorizontally) {
        Text(state.track.title, style=MaterialTheme.typography.titleLarge)
        Text(state.track.artistName, style=MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Slider(value=state.positionMs.toFloat(),
            onValueChange={ onEvent(PlayerUiEvent.Seek(it.toLong())) },
            valueRange=0f..state.durationMs.toFloat().coerceAtLeast(1f))
        Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly,
            verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick={onEvent(PlayerUiEvent.ToggleShuffle)}) { Text("⇀") }
            IconButton(onClick={onEvent(PlayerUiEvent.SkipPrev)})      { Text("⏮") }
            FilledIconButton(onClick={
                if(state.isPlaying) onEvent(PlayerUiEvent.Pause) else onEvent(PlayerUiEvent.Play)
            }) { Text(if(state.isPlaying) "⏸" else "▶") }
            IconButton(onClick={onEvent(PlayerUiEvent.SkipNext)})    { Text("⏭") }
            IconButton(onClick={onEvent(PlayerUiEvent.CycleRepeat)}) { Text("↻") }
        }
    }
}

@Composable
fun MiniPlayerBar(state: PlayerUiState.Playing, onEvent: (PlayerUiEvent)->Unit, onClick: ()->Unit) {
    Surface(tonalElevation=4.dp) {
        Row(modifier=Modifier.fillMaxWidth().clickable{onClick()}.padding(horizontal=16.dp, vertical=8.dp),
            verticalAlignment=Alignment.CenterVertically) {
            AsyncImage(model=state.track.artworkUrl, contentDescription=null,
                contentScale=ContentScale.Crop, modifier=Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(state.track.title,      style=MaterialTheme.typography.bodyMedium, maxLines=1)
                Text(state.track.artistName, style=MaterialTheme.typography.bodySmall,  maxLines=1)
            }
            IconButton(onClick={
                if(state.isPlaying) onEvent(PlayerUiEvent.Pause) else onEvent(PlayerUiEvent.Play)
            }) { Text(if(state.isPlaying) "⏸" else "▶") }
        }
    }
}

@Composable
fun PlayerComponents(state: PlayerUiState.Playing, onEvent: (PlayerUiEvent)->Unit) {
    Column(modifier=Modifier.fillMaxWidth().padding(top=320.dp),
        horizontalAlignment=Alignment.CenterHorizontally) {
        PlaybackControls(state, onEvent)
    }
}
''',
    '.github/workflows/build_release.yml': '''\
name: Build & Release
on:
  push:
    tags: ['v*.*.*']
  workflow_dispatch:
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: 'temurin' }
      - uses: actions/cache@v4
        with:
          path: |
            ~/.gradle/caches
            ~/.gradle/wrapper
          key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*','**/libs.versions.toml') }}
      - name: Lint
        run: ./gradlew lint
      - name: Build release APK
        run: ./gradlew assembleRelease
        env:
          KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
          KEY_ALIAS:         ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD:      ${{ secrets.KEY_PASSWORD }}
      - name: Sign APK
        uses: r0adkll/sign-android-release@v1
        with:
          releaseDirectory: app/build/outputs/apk/release
          signingKeyBase64: ${{ secrets.KEYSTORE_BASE64 }}
          alias:            ${{ secrets.KEY_ALIAS }}
          keyStorePassword: ${{ secrets.KEYSTORE_PASSWORD }}
          keyPassword:      ${{ secrets.KEY_PASSWORD }}
      - name: Upload
        uses: actions/upload-artifact@v4
        with:
          name: phantasia-release
          path: app/build/outputs/apk/release/*.apk
          retention-days: 14
''',
}

# Create all files
for path, content in FILES.items():
    if not path:
        continue
    output = html.unescape(content)
    dirpath = os.path.dirname(path)
    if dirpath:
        os.makedirs(dirpath, exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        f.write(output)
    print('Wrote', path)
print('Done', len([p for p in FILES.keys() if p]), 'files')
