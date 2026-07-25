# PHANTASIA MUSIC — Complete Rebuild Prompt for Jules

## RULES — follow without exception
- Read every file before editing it
- Complete ALL steps in one response, no stopping
- After all files written, run `./gradlew assembleDebug` and fix every error
- One final commit: `git add -A && git commit -m "feat: complete rebuild — working search, home, player, accounts, dark purple theme"`
- Show `git log --oneline -5` at end
- Never truncate file content, write every line
- Never leave unresolved imports or TODO comments
- Fix ALL build errors before committing

---

## READ THESE FILES FIRST

```
app/src/main/kotlin/com/phantasia/music/network/InnerTubeClient.kt
app/src/main/kotlin/com/phantasia/music/network/MusicRepository.kt
app/src/main/kotlin/com/phantasia/music/network/SearchParser.kt
app/src/main/kotlin/com/phantasia/music/network/PlayerParser.kt
app/src/main/kotlin/com/phantasia/music/network/CipherEngine.kt
app/src/main/kotlin/com/phantasia/music/ui/Theme.kt
app/src/main/kotlin/com/phantasia/music/ui/HomeScreen.kt
app/src/main/kotlin/com/phantasia/music/ui/SearchScreen.kt
app/src/main/kotlin/com/phantasia/music/ui/PlayerScreen.kt
app/src/main/kotlin/com/phantasia/music/ui/SearchViewModel.kt
app/src/main/kotlin/com/phantasia/music/ui/PlayerViewModel.kt
app/src/main/kotlin/com/phantasia/music/AppNavigation.kt
app/src/main/kotlin/com/phantasia/music/storage/SongStore.kt
app/src/main/kotlin/com/phantasia/music/storage/PhantasiaDatabase.kt
app/build.gradle.kts
```

---

## STEP 1 — Fix Theme.kt (dark purple, no white, Velune gradient style)

Replace the ENTIRE content of `app/src/main/kotlin/com/phantasia/music/ui/Theme.kt` with:

```kotlin
package com.phantasia.music.ui

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ── Phantasia deep purple palette ─────────────────────────────────────────────
object PhantasiaColors {
    val Bg           = Color(0xFF0A0A12)   // near-black with purple tint
    val Surface      = Color(0xFF111120)   // card background
    val SurfaceHigh  = Color(0xFF1A1A2E)   // elevated surface
    val SurfaceCard  = Color(0xFF16213E)   // list item background
    val Primary      = Color(0xFF9B59FF)   // main purple
    val PrimaryDim   = Color(0xFF7B3FEF)   // darker purple
    val PrimaryGlow  = Color(0xFFBB86FC)   // light purple glow
    val Secondary    = Color(0xFF6C63FF)   // indigo accent
    val Tertiary     = Color(0xFFE040FB)   // magenta accent
    val OnBg         = Color(0xFFF0EEFF)   // near-white with purple tint
    val OnSurface    = Color(0xFFE8E0FF)   // text on surface
    val OnSurfaceDim = Color(0xFF9090B0)   // secondary text
    val Outline      = Color(0xFF2A2A4A)   // dividers
    val Error        = Color(0xFFFF5252)
    val GradientTop  = Color(0xFF1A0533)   // gradient start
    val GradientMid  = Color(0xFF0D0D2B)   // gradient middle
    val GradientBot  = Color(0xFF080818)   // gradient end
}

private val PhantasiaDark = darkColorScheme(
    primary            = PhantasiaColors.Primary,
    onPrimary          = Color.White,
    primaryContainer   = PhantasiaColors.PrimaryDim,
    onPrimaryContainer = PhantasiaColors.PrimaryGlow,
    secondary          = PhantasiaColors.Secondary,
    onSecondary        = Color.White,
    secondaryContainer = Color(0xFF1E1B4B),
    tertiary           = PhantasiaColors.Tertiary,
    onTertiary         = Color.White,
    background         = PhantasiaColors.Bg,
    onBackground       = PhantasiaColors.OnBg,
    surface            = PhantasiaColors.Surface,
    onSurface          = PhantasiaColors.OnSurface,
    surfaceVariant     = PhantasiaColors.SurfaceCard,
    onSurfaceVariant   = PhantasiaColors.OnSurfaceDim,
    error              = PhantasiaColors.Error,
    outline            = PhantasiaColors.Outline,
    surfaceTint        = PhantasiaColors.Primary,
    inverseSurface     = PhantasiaColors.OnBg,
    inverseOnSurface   = PhantasiaColors.Bg,
    inversePrimary     = PhantasiaColors.PrimaryDim,
    scrim              = Color(0xFF000000),
)

// Light mode still dark-purple (Phantasia is always dark)
private val PhantasiaLight = PhantasiaDark

val PhantasiaTypography = Typography(
    displayLarge  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 57.sp),
    displayMedium = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 45.sp),
    displaySmall  = TextStyle(fontWeight = FontWeight.Bold,     fontSize = 36.sp),
    headlineLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 32.sp),
    headlineMedium= TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 28.sp),
    headlineSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
    titleLarge    = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
    titleMedium   = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 16.sp, lineHeight = 24.sp),
    titleSmall    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium    = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall     = TextStyle(fontWeight = FontWeight.Normal,   fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 14.sp),
    labelMedium   = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 12.sp),
    labelSmall    = TextStyle(fontWeight = FontWeight.Medium,   fontSize = 11.sp, lineHeight = 16.sp)
)

@Composable
fun PhantasiaTheme(
    dark: Boolean = true,  // Always dark — Phantasia is a dark-only app
    content: @Composable () -> Unit
) {
    val colorScheme = PhantasiaDark
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor     = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars     = false
                isAppearanceLightNavigationBars = false
            }
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = PhantasiaTypography,
        content     = content
    )
}
```

---

## STEP 2 — Fix InnerTubeClient.kt (remove duplicate import, fix API key)

Replace the ENTIRE content of `app/src/main/kotlin/com/phantasia/music/network/InnerTubeClient.kt` with:

```kotlin
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
    const val KEY    = "AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
    const val NAME   = "WEB_REMIX"
    const val VER    = "1.20240101.01.00"
    const val UA     = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36"
    const val BASE   = "https://music.youtube.com/youtubei/v1"
    const val ORIGIN = "https://music.youtube.com"
    const val PLAYER = "https://www.youtube.com/s/player/4248d3c7/player_ias.vflset/en_US/base.js"
}

data class InnerTubeLocale(val gl: String = "US", val hl: String = "en")

fun buildContext(l: InnerTubeLocale, cookie: String? = null): MutableMap<String, Any> = mutableMapOf(
    "context" to mapOf(
        "client" to mapOf(
            "clientName"       to IT.NAME,
            "clientVersion"    to IT.VER,
            "hl"               to l.hl,
            "gl"               to l.gl,
            "userAgent"        to IT.UA,
            "timeZone"         to "UTC",
            "utcOffsetMinutes" to 0
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
    fun provideOkHttp(guard: EnvironmentGuard): OkHttpClient =
        OkHttpClient.Builder()
            .protocols(listOf(Protocol.HTTP_2, Protocol.HTTP_1_1))
            .connectionPool(ConnectionPool(5, 5, TimeUnit.MINUTES))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30,    TimeUnit.SECONDS)
            .writeTimeout(15,   TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(guard.sslPinningInterceptor())
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent",      IT.UA)
                        .header("Origin",          IT.ORIGIN)
                        .header("Referer",         "${IT.ORIGIN}/")
                        .header("X-Goog-Api-Key",  IT.KEY)
                        .header("Content-Type",    "application/json")
                        .build()
                )
            }.build()

    @Provides @Singleton
    fun provideKtor(@InnerTubeHttp ok: OkHttpClient): HttpClient =
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
}
```

---

## STEP 3 — Fix InnerTubeRequests.kt (use WEB_REMIX client like Velune)

Replace the ENTIRE content of `app/src/main/kotlin/com/phantasia/music/network/InnerTubeRequests.kt` with:

```kotlin
package com.phantasia.music.network

import io.ktor.client.HttpClient
import io.ktor.client.request.header
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
    // Cookie for YTM login — set by AccountViewModel when user logs in
    var ytmCookie: String? = null

    private fun buildHeaders(block: io.ktor.client.request.HttpRequestBuilder.() -> Unit = {}):
        io.ktor.client.request.HttpRequestBuilder.() -> Unit = {
        if (!ytmCookie.isNullOrBlank()) {
            header("Cookie", ytmCookie!!)
            // SAPISIDHASH auth for logged-in requests
            val sapisid = ytmCookie!!.split(";")
                .map { it.trim() }
                .find { it.startsWith("SAPISID=") }
                ?.removePrefix("SAPISID=")
            if (sapisid != null) {
                val time = System.currentTimeMillis() / 1000
                val hash = java.security.MessageDigest.getInstance("SHA-1")
                    .digest("$time $sapisid ${IT.ORIGIN}".toByteArray())
                    .joinToString("") { "%02x".format(it) }
                header("Authorization", "SAPISIDHASH ${time}_$hash")
                header("X-Origin", IT.ORIGIN)
            }
        }
        block()
    }

    suspend fun search(q: String, params: String? = null, continuation: String? = null): HttpResponse =
        client.post("${IT.BASE}/search?key=${IT.KEY}&prettyPrint=false") {
            contentType(ContentType.Application.Json)
            buildHeaders()()
            setBody(buildContext(locale).apply {
                if (q.isNotBlank()) put("query", q)
                if (params != null) put("params", params)
                if (continuation != null) {
                    put("continuation", continuation)
                    put("ctoken", continuation)
                }
            })
        }

    suspend fun player(videoId: String, playlistId: String? = null): HttpResponse =
        client.post("${IT.BASE}/player?key=${IT.KEY}&prettyPrint=false") {
            contentType(ContentType.Application.Json)
            buildHeaders()()
            setBody(buildContext(locale).apply {
                put("videoId", videoId)
                if (playlistId != null) put("playlistId", playlistId)
                put("playbackContext", mapOf(
                    "contentPlaybackContext" to mapOf(
                        "signatureTimestamp" to 19950,
                        "html5Preference"    to "HTML5_PREF_WANTS"
                    )
                ))
                put("racyCheckOk",    true)
                put("contentCheckOk", true)
            })
        }

    suspend fun browse(browseId: String, params: String? = null, continuation: String? = null): HttpResponse =
        client.post("${IT.BASE}/browse?key=${IT.KEY}&prettyPrint=false") {
            contentType(ContentType.Application.Json)
            buildHeaders()()
            setBody(buildContext(locale).apply {
                if (continuation == null) put("browseId", browseId)
                if (params != null) put("params", params)
                if (continuation != null) {
                    put("continuation", continuation)
                    put("ctoken", continuation)
                }
            })
        }

    suspend fun next(videoId: String, playlistId: String? = null, continuation: String? = null): HttpResponse =
        client.post("${IT.BASE}/next?key=${IT.KEY}&prettyPrint=false") {
            contentType(ContentType.Application.Json)
            buildHeaders()()
            setBody(buildContext(locale).apply {
                put("videoId", videoId)
                if (playlistId != null) put("playlistId", playlistId)
                put("isAudioOnly", true)
                if (continuation != null) {
                    put("continuation", continuation)
                    put("ctoken", continuation)
                }
            })
        }

    suspend fun suggestions(q: String): HttpResponse =
        client.post("${IT.BASE}/music/get_search_suggestions?key=${IT.KEY}&prettyPrint=false") {
            contentType(ContentType.Application.Json)
            buildHeaders()()
            setBody(buildContext(locale).apply { put("input", q) })
        }

    suspend fun home(continuation: String? = null, params: String? = null): HttpResponse =
        browse("FEmusic_home", params = params, continuation = continuation)

    suspend fun library(browseId: String): HttpResponse = browse(browseId)
}
```

---

## STEP 4 — Fix MusicRepository.kt (add home(), proper search, error handling)

Replace the ENTIRE content of `app/src/main/kotlin/com/phantasia/music/network/MusicRepository.kt` with:

```kotlin
package com.phantasia.music.network

import com.phantasia.music.storage.SongEntity
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.*
import javax.inject.Inject
import javax.inject.Singleton

// ── Home page section ─────────────────────────────────────────────────────────
data class HomeSection(
    val title:    String,
    val items:    List<TrackModel>
)

interface MusicRepository {
    suspend fun search(query: String): List<SearchResultModel>
    suspend fun getStream(videoId: String): StreamDataModel?
    suspend fun getAlbum(browseId: String): AlbumModel?
    suspend fun getSuggestions(videoId: String): List<TrackModel>
    suspend fun getSearchSuggestions(query: String): List<String>
    suspend fun getHomeSections(): List<HomeSection>
    suspend fun getRelatedSongs(videoId: String): List<TrackModel>
}

@Singleton
class MusicRepositoryImpl @Inject constructor(
    private val req:    InnerTubeRequests,
    private val sParse: SearchParser,
    private val pParse: PlayerParser,
    private val aParse: AlbumParser,
    private val cipher: CipherEngine
) : MusicRepository {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true; coerceInputValues = true }

    override suspend fun search(query: String): List<SearchResultModel> = runCatching {
        val raw = req.search(query).bodyAsText()
        val root = json.parseToJsonElement(raw)
        // Try tabs-based search response (WEB_REMIX format)
        val contents = root.jsonObject["contents"]?.jsonObject
        val tabs = contents?.get("tabbedSearchResultsRenderer")?.jsonObject
            ?.get("tabs")?.jsonArray?.firstOrNull()?.jsonObject
            ?.get("tabRenderer")?.jsonObject?.get("content")?.jsonObject
            ?.get("sectionListRenderer")?.jsonObject?.get("contents")?.jsonArray

        val results = mutableListOf<SearchResultModel>()
        tabs?.forEach { section ->
            val shelf = section.jsonObject["musicShelfRenderer"]?.jsonObject
            val items = shelf?.get("contents")?.jsonArray ?: return@forEach
            items.forEach { item ->
                val renderer = item.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject
                    ?: return@forEach
                parseSearchItem(renderer)?.let { results.add(it) }
            }
        }
        // Fallback: if tabs empty, try legacy parser
        if (results.isEmpty()) sParse.parse(json.parseToJsonElement(raw)) else results
    }.onFailure { it.printStackTrace() }.getOrDefault(emptyList())

    private fun parseSearchItem(r: JsonObject): SearchResultModel? = runCatching {
        val cols   = r["flexColumns"]?.jsonArray ?: return null
        val col0   = cols.getOrNull(0)?.jsonObject?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
        val col1   = cols.getOrNull(1)?.jsonObject?.get("musicResponsiveListItemFlexColumnRenderer")?.jsonObject
        val title  = col0?.get("text")?.jsonObject?.get("runs")?.jsonArray
            ?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: return null
        val thumb  = r["thumbnail"]?.jsonObject?.get("musicThumbnailRenderer")?.jsonObject
            ?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray
            ?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull ?: ""

        val navEp  = col0.get("text")?.jsonObject?.get("runs")?.jsonArray
            ?.firstOrNull()?.jsonObject?.get("navigationEndpoint")?.jsonObject
        val watchId  = navEp?.get("watchEndpoint")?.jsonObject?.get("videoId")?.jsonPrimitive?.contentOrNull
        val browseId = navEp?.get("browseEndpoint")?.jsonObject?.get("browseId")?.jsonPrimitive?.contentOrNull
        val pageType = navEp?.get("browseEndpoint")?.jsonObject
            ?.get("browseEndpointContextSupportedConfigs")?.jsonObject
            ?.get("browseEndpointContextMusicConfig")?.jsonObject
            ?.get("pageType")?.jsonPrimitive?.contentOrNull

        val playlistData = r["playlistItemData"]?.jsonObject
        val videoIdFallback = playlistData?.get("videoId")?.jsonPrimitive?.contentOrNull

        val finalVideoId = watchId ?: videoIdFallback

        when {
            finalVideoId != null -> {
                val artistText = col1?.get("text")?.jsonObject?.get("runs")?.jsonArray
                    ?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: ""
                val durText = col1?.get("text")?.jsonObject?.get("runs")?.jsonArray
                    ?.lastOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: "0:00"
                SearchResultModel.TrackResult(TrackModel(
                    videoId = finalVideoId, title = title,
                    artistName = artistText, albumTitle = "", artworkUrl = thumb,
                    durationSeconds = parseDuration(durText)
                ))
            }
            browseId != null && pageType?.contains("ALBUM") == true -> {
                val artist = col1?.get("text")?.jsonObject?.get("runs")?.jsonArray
                    ?.getOrNull(0)?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: ""
                val year   = col1?.get("text")?.jsonObject?.get("runs")?.jsonArray
                    ?.lastOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: ""
                SearchResultModel.AlbumResult(AlbumModel(browseId, title, year, thumb, artist))
            }
            browseId != null -> {
                SearchResultModel.ArtistResult(ArtistModel(browseId, title, thumb))
            }
            else -> null
        }
    }.getOrNull()

    private fun parseDuration(raw: String): Long = runCatching {
        val p = raw.trim().split(":").map { it.toLong() }
        when (p.size) { 2 -> p[0]*60+p[1]; 3 -> p[0]*3600+p[1]*60+p[2]; else -> 0L }
    }.getOrDefault(0L)

    override suspend fun getStream(videoId: String): StreamDataModel? = runCatching {
        val raw  = req.player(videoId).bodyAsText()
        val root = json.parseToJsonElement(raw)
        val raw2 = pParse.parse(videoId, root) ?: return@runCatching null
        raw2.copy(streamUrl = cipher.resolveStreamUrl(raw2.streamUrl, IT.PLAYER))
    }.getOrNull()

    override suspend fun getAlbum(browseId: String): AlbumModel? = runCatching {
        aParse.parse(browseId, json.parseToJsonElement(req.browse(browseId).bodyAsText()))
    }.getOrNull()

    override suspend fun getSuggestions(videoId: String): List<TrackModel> = runCatching {
        val raw  = req.next(videoId).bodyAsText()
        val root = json.parseToJsonElement(raw)
        // Parse related songs from next response
        val songs = mutableListOf<TrackModel>()
        val tabs  = root.jsonObject["contents"]?.jsonObject
            ?.get("singleColumnMusicWatchNextResultsRenderer")?.jsonObject
            ?.get("tabbedRenderer")?.jsonObject
            ?.get("watchNextTabbedResultsRenderer")?.jsonObject
            ?.get("tabs")?.jsonArray
        tabs?.forEach { tab ->
            val contents = tab.jsonObject["tabRenderer"]?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("musicQueueRenderer")?.jsonObject
                ?.get("content")?.jsonObject
                ?.get("playlistPanelRenderer")?.jsonObject
                ?.get("contents")?.jsonArray ?: return@forEach
            contents.forEach { item ->
                val r = item.jsonObject["playlistPanelVideoRenderer"]?.jsonObject ?: return@forEach
                val vid   = r["videoId"]?.jsonPrimitive?.contentOrNull ?: return@forEach
                val title = r["title"]?.jsonObject?.get("runs")?.jsonArray
                    ?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: return@forEach
                val artist = r["longBylineText"]?.jsonObject?.get("runs")?.jsonArray
                    ?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: ""
                val thumb  = r["thumbnail"]?.jsonObject?.get("thumbnails")?.jsonArray
                    ?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull ?: ""
                songs.add(TrackModel(vid, title, artist, "", thumb, 0L))
            }
        }
        songs.take(20)
    }.getOrDefault(emptyList())

    override suspend fun getRelatedSongs(videoId: String): List<TrackModel> = getSuggestions(videoId)

    override suspend fun getSearchSuggestions(query: String): List<String> = runCatching {
        val raw  = req.suggestions(query).bodyAsText()
        val root = json.parseToJsonElement(raw).jsonObject
        root["contents"]?.jsonArray?.flatMap { s ->
            s.jsonObject["searchSuggestionsSectionRenderer"]?.jsonObject?.get("contents")?.jsonArray
                ?.mapNotNull { it.jsonObject["searchSuggestionRenderer"]?.jsonObject
                    ?.get("suggestion")?.jsonObject?.get("runs")?.jsonArray
                    ?.joinToString("") { run -> run.jsonObject["text"]?.jsonPrimitive?.contentOrNull ?: "" }
                } ?: emptyList()
        } ?: emptyList()
    }.getOrDefault(emptyList())

    override suspend fun getHomeSections(): List<HomeSection> = runCatching {
        val raw  = req.home().bodyAsText()
        val root = json.parseToJsonElement(raw).jsonObject
        val sections = mutableListOf<HomeSection>()

        // Parse WEB_REMIX home page carousels
        val contents = root["contents"]?.jsonObject
            ?.get("singleColumnBrowseResultsRenderer")?.jsonObject
            ?.get("tabs")?.jsonArray?.firstOrNull()?.jsonObject
            ?.get("tabRenderer")?.jsonObject?.get("content")?.jsonObject
            ?.get("sectionListRenderer")?.jsonObject?.get("contents")?.jsonArray

        contents?.forEach { section ->
            val carousel = section.jsonObject["musicCarouselShelfRenderer"]?.jsonObject ?: return@forEach
            val title    = carousel["header"]?.jsonObject
                ?.get("musicCarouselShelfBasicHeaderRenderer")?.jsonObject
                ?.get("title")?.jsonObject?.get("runs")?.jsonArray
                ?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: return@forEach

            val items = mutableListOf<TrackModel>()
            carousel["contents"]?.jsonArray?.forEach { content ->
                // Two-row item (album/playlist tiles)
                val twoRow = content.jsonObject["musicTwoRowItemRenderer"]?.jsonObject
                if (twoRow != null) {
                    val vid   = twoRow["navigationEndpoint"]?.jsonObject
                        ?.get("watchEndpoint")?.jsonObject
                        ?.get("videoId")?.jsonPrimitive?.contentOrNull
                    val bid   = twoRow["navigationEndpoint"]?.jsonObject
                        ?.get("browseEndpoint")?.jsonObject
                        ?.get("browseId")?.jsonPrimitive?.contentOrNull
                    val t     = twoRow["title"]?.jsonObject?.get("runs")?.jsonArray
                        ?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: return@forEach
                    val thumb = twoRow["thumbnailRenderer"]?.jsonObject
                        ?.get("musicThumbnailRenderer")?.jsonObject
                        ?.get("thumbnail")?.jsonObject?.get("thumbnails")?.jsonArray
                        ?.lastOrNull()?.jsonObject?.get("url")?.jsonPrimitive?.contentOrNull ?: ""
                    val sub   = twoRow["subtitle"]?.jsonObject?.get("runs")?.jsonArray
                        ?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.contentOrNull ?: ""
                    if (vid != null) {
                        items.add(TrackModel(vid, t, sub, "", thumb, 0L))
                    } else if (bid != null) {
                        items.add(TrackModel(bid, t, sub, "", thumb, 0L))
                    }
                    return@forEach
                }
                // Responsive list item (songs)
                val resp = content.jsonObject["musicResponsiveListItemRenderer"]?.jsonObject
                if (resp != null) {
                    parseSearchItem(resp)?.let {
                        if (it is SearchResultModel.TrackResult) items.add(it.track)
                    }
                }
            }
            if (items.isNotEmpty()) sections.add(HomeSection(title, items))
        }
        sections.take(8)
    }.onFailure { it.printStackTrace() }.getOrDefault(emptyList())
}

@Module @InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton
    abstract fun bindMusicRepository(impl: MusicRepositoryImpl): MusicRepository
}
```

---

## STEP 5 — Create HomeViewModel.kt

Create `app/src/main/kotlin/com/phantasia/music/ui/HomeViewModel.kt` with:

```kotlin
package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantasia.music.network.HomeSection
import com.phantasia.music.network.MusicRepository
import com.phantasia.music.network.TrackModel
import com.phantasia.music.storage.SongDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading:       Boolean          = true,
    val isRefreshing:    Boolean          = false,
    val homeSections:    List<HomeSection> = emptyList(),
    val quickPicks:      List<TrackModel>  = emptyList(),
    val recentlyPlayed:  List<TrackModel>  = emptyList(),
    val error:           String?           = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo:    MusicRepository,
    private val songDao: SongDao
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        load()
        // Observe recent plays from Room
        viewModelScope.launch {
            songDao.getAll().collect { songs ->
                _state.update { it.copy(
                    recentlyPlayed = songs.take(20).map { s ->
                        TrackModel(s.videoId, s.title, s.artistName, s.albumTitle, s.artworkUrl, s.durationSeconds)
                    }
                )}
            }
        }
    }

    fun load() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isLoading = true, error = null) }
            runCatching {
                val sections = repo.getHomeSections()
                // Quick picks = first song from each section, shuffled
                val picks = sections.flatMap { it.items }.shuffled().take(12)
                _state.update { it.copy(
                    isLoading    = false,
                    homeSections = sections,
                    quickPicks   = picks
                )}
            }.onFailure { e ->
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isRefreshing = true) }
            runCatching { repo.getHomeSections() }.onSuccess { sections ->
                _state.update { it.copy(
                    isRefreshing = false,
                    homeSections = sections,
                    quickPicks   = sections.flatMap { it.items }.shuffled().take(12)
                )}
            }.onFailure {
                _state.update { it.copy(isRefreshing = false) }
            }
        }
    }
}
```

---

## STEP 6 — Replace HomeScreen.kt (Velune-style with real data)

Replace the ENTIRE content of `app/src/main/kotlin/com/phantasia/music/ui/HomeScreen.kt` with:

```kotlin
package com.phantasia.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phantasia.music.Route
import com.phantasia.music.network.TrackModel

@Composable
fun HomeScreen(nav: NavController) {
    val vm: HomeViewModel = hiltViewModel()
    val state by vm.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        PhantasiaColors.GradientTop,
                        PhantasiaColors.GradientMid,
                        PhantasiaColors.GradientBot
                    )
                )
            )
    ) {
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PhantasiaColors.Primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Loading…", color = PhantasiaColors.OnSurfaceDim,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            state.error != null && state.homeSections.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                        Text("Could not load home feed", color = PhantasiaColors.OnSurface,
                            style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(state.error ?: "", color = PhantasiaColors.OnSurfaceDim,
                            style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = { vm.load() }) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier       = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    // ── Header ────────────────────────────────────────────────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(PhantasiaColors.GradientTop, Color.Transparent)
                                    )
                                )
                                .padding(horizontal = 24.dp)
                                .padding(top = 60.dp)
                        ) {
                            Column {
                                Text(
                                    "Phantasia",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = PhantasiaColors.Primary
                                )
                                Text(
                                    "What do you want to hear?",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PhantasiaColors.OnSurfaceDim
                                )
                            }
                        }
                    }

                    // ── Quick picks carousel ──────────────────────────────────
                    if (state.quickPicks.isNotEmpty()) {
                        item {
                            SectionHeader("Quick picks")
                        }
                        item {
                            LazyRow(
                                contentPadding       = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier             = Modifier.padding(bottom = 8.dp)
                            ) {
                                items(state.quickPicks) { track ->
                                    QuickPickCard(track) {
                                        nav.navigate(Route.Player.build(track.videoId))
                                    }
                                }
                            }
                        }
                    }

                    // ── Recently played ───────────────────────────────────────
                    if (state.recentlyPlayed.isNotEmpty()) {
                        item { SectionHeader("Recently played") }
                        items(state.recentlyPlayed.take(5)) { track ->
                            HomeTrackRow(track) {
                                nav.navigate(Route.Player.build(track.videoId))
                            }
                        }
                    }

                    // ── Home sections from YouTube Music ──────────────────────
                    state.homeSections.forEach { section ->
                        item {
                            SectionHeader(section.title)
                        }
                        item {
                            LazyRow(
                                contentPadding        = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement  = Arrangement.spacedBy(12.dp),
                                modifier              = Modifier.padding(bottom = 8.dp)
                            ) {
                                items(section.items) { track ->
                                    QuickPickCard(track) {
                                        nav.navigate(Route.Player.build(track.videoId))
                                    }
                                }
                            }
                        }
                    }

                    // ── Empty state ───────────────────────────────────────────
                    if (state.homeSections.isEmpty() && !state.isLoading) {
                        item {
                            Box(
                                modifier            = Modifier.fillMaxWidth().padding(top = 32.dp),
                                contentAlignment    = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎵", style = MaterialTheme.typography.displayMedium)
                                    Spacer(Modifier.height(16.dp))
                                    Text("Search for music to get started",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = PhantasiaColors.OnSurfaceDim)
                                }
                            }
                        }
                    }
                }

                // Refresh indicator
                if (state.isRefreshing) {
                    Box(
                        modifier         = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            color    = PhantasiaColors.Primary,
                            trackColor = Color.Transparent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Row(
        modifier              = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = PhantasiaColors.OnSurface)
    }
}

@Composable
private fun QuickPickCard(track: TrackModel, onClick: () -> Unit) {
    Column(
        modifier            = Modifier.width(130.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PhantasiaColors.SurfaceCard)
        ) {
            AsyncImage(
                model              = track.artworkUrl,
                contentDescription = track.title,
                contentScale       = ContentScale.Crop,
                modifier           = Modifier.fillMaxSize()
            )
            // Purple gradient overlay at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000)))
                    )
            )
            // Play button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(PhantasiaColors.Primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play",
                    tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(track.title, style = MaterialTheme.typography.labelMedium,
            color = PhantasiaColors.OnSurface, maxLines = 2,
            overflow = TextOverflow.Ellipsis)
        if (track.artistName.isNotEmpty()) {
            Text(track.artistName, style = MaterialTheme.typography.labelSmall,
                color = PhantasiaColors.OnSurfaceDim, maxLines = 1,
                overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun HomeTrackRow(track: TrackModel, onClick: () -> Unit) {
    ListItem(
        headlineContent   = { Text(track.title, color = PhantasiaColors.OnSurface,
            maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text(track.artistName, color = PhantasiaColors.OnSurfaceDim, maxLines = 1) },
        leadingContent    = {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(6.dp))
                .background(PhantasiaColors.SurfaceCard)) {
                AsyncImage(model = track.artworkUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        },
        colors   = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}
```

---

## STEP 7 — Replace SearchScreen.kt (actually works, shows results)

Replace the ENTIRE content of `app/src/main/kotlin/com/phantasia/music/ui/SearchScreen.kt` with:

```kotlin
package com.phantasia.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phantasia.music.Route
import com.phantasia.music.network.AlbumModel
import com.phantasia.music.network.ArtistModel
import com.phantasia.music.network.SearchResultModel
import com.phantasia.music.network.TrackModel

@Composable
fun SearchScreen(nav: NavController) {
    val vm: SearchViewModel = hiltViewModel()
    val query  by vm.query.collectAsState()
    val state  by vm.uiState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(PhantasiaColors.Bg, PhantasiaColors.GradientBot))
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Search bar ────────────────────────────────────────────────────
            Spacer(Modifier.height(52.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(PhantasiaColors.SurfaceCard)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, contentDescription = null,
                        tint = PhantasiaColors.OnSurfaceDim, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = { vm.onEvent(SearchUiEvent.QueryChanged(it)) },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = PhantasiaColors.OnSurface),
                        singleLine = true,
                        modifier   = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (query.isEmpty()) {
                                Text("Search songs, artists, albums…",
                                    color = PhantasiaColors.OnSurfaceDim,
                                    style = MaterialTheme.typography.bodyLarge)
                            }
                            inner()
                        }
                    )
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { vm.onEvent(SearchUiEvent.QueryChanged("")) },
                            modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, contentDescription = "Clear",
                                tint = PhantasiaColors.OnSurfaceDim, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            // ── Results ───────────────────────────────────────────────────────
            when (val s = state) {
                is SearchUiState.Idle -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", style = MaterialTheme.typography.displaySmall)
                            Spacer(Modifier.height(12.dp))
                            Text("Search for anything",
                                color = PhantasiaColors.OnSurfaceDim,
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                is SearchUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PhantasiaColors.Primary)
                    }
                }
                is SearchUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)) {
                            Text("Search failed", color = PhantasiaColors.OnSurface,
                                style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(s.message, color = PhantasiaColors.Error,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                is SearchUiState.Results -> {
                    if (s.items.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No results for \"$query\"",
                                color = PhantasiaColors.OnSurfaceDim,
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    } else {
                        Text("${s.items.size} results", style = MaterialTheme.typography.labelMedium,
                            color = PhantasiaColors.OnSurfaceDim,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp))
                        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                            items(s.items) { item ->
                                when (item) {
                                    is SearchResultModel.TrackResult ->
                                        SearchTrackRow(item.track) {
                                            vm.onEvent(SearchUiEvent.TrackSelected(item.track.videoId))
                                            nav.navigate(Route.Player.build(item.track.videoId))
                                        }
                                    is SearchResultModel.AlbumResult -> SearchAlbumRow(item.album)
                                    is SearchResultModel.ArtistResult -> SearchArtistRow(item.artist)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    textStyle: androidx.compose.ui.text.TextStyle,
    singleLine: Boolean,
    modifier: Modifier,
    decorationBox: @Composable (@Composable () -> Unit) -> Unit
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        textStyle = textStyle,
        singleLine = singleLine,
        modifier = modifier,
        decorationBox = decorationBox
    )
}

@Composable
private fun SearchTrackRow(track: TrackModel, onClick: () -> Unit) {
    ListItem(
        headlineContent   = { Text(track.title, color = PhantasiaColors.OnSurface,
            maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text("${track.artistName} · Song",
            color = PhantasiaColors.OnSurfaceDim, maxLines = 1) },
        leadingContent    = {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(6.dp))
                .background(PhantasiaColors.SurfaceCard)) {
                AsyncImage(model = track.artworkUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        },
        colors   = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
private fun SearchAlbumRow(album: AlbumModel) {
    ListItem(
        headlineContent   = { Text(album.title, color = PhantasiaColors.OnSurface,
            maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text("${album.artistName} · Album · ${album.year}",
            color = PhantasiaColors.OnSurfaceDim, maxLines = 1) },
        leadingContent    = {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(6.dp))
                .background(PhantasiaColors.SurfaceCard)) {
                AsyncImage(model = album.thumbnailUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        },
        overlineContent = { Text("Album", color = PhantasiaColors.Primary) },
        colors          = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun SearchArtistRow(artist: ArtistModel) {
    ListItem(
        headlineContent = { Text(artist.name, color = PhantasiaColors.OnSurface) },
        leadingContent  = {
            Box(modifier = Modifier.size(52.dp).clip(CircleShape)
                .background(PhantasiaColors.SurfaceCard), contentAlignment = Alignment.Center) {
                if (artist.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(model = artist.thumbnailUrl, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                } else {
                    Text(artist.name.take(1).uppercase(),
                        style = MaterialTheme.typography.titleLarge,
                        color = PhantasiaColors.Primary)
                }
            }
        },
        overlineContent = { Text("Artist", color = PhantasiaColors.Secondary) },
        colors          = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
```

---

## STEP 8 — Create YtmLoginScreen.kt (WebView-based, exactly like Velune)

Create `app/src/main/kotlin/com/phantasia/music/ui/YtmLoginScreen.kt`:

```kotlin
package com.phantasia.music.ui

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

/**
 * YouTube Music login via WebView.
 * Exactly how Velune does it — loads Google sign-in page inside a WebView,
 * captures the cookie when redirected to music.youtube.com, stores it securely.
 * The cookie gives us full YouTube Music API access (playlists, liked songs, history).
 */
@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YtmLoginScreen(nav: NavController) {
    val vm: AccountViewModel = hiltViewModel()
    var webViewRef: WebView? = null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign in to YouTube Music") },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                            tint = PhantasiaColors.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PhantasiaColors.Surface,
                    titleContentColor = PhantasiaColors.OnSurface
                )
            )
        },
        containerColor = PhantasiaColors.Bg
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory  = { context ->
                WebView(context).apply {
                    webViewRef = this
                    settings.apply {
                        javaScriptEnabled = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        domStorageEnabled   = true
                        databaseEnabled     = true
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String?) {
                            if (url?.startsWith("https://music.youtube.com") == true) {
                                val cookie = CookieManager.getInstance().getCookie(url)
                                if (!cookie.isNullOrBlank() && "SAPISID" in cookie) {
                                    vm.onYtmCookieReceived(cookie)
                                    nav.navigateUp()
                                }
                            }
                        }
                    }
                    // Load Google sign-in page targeted at YouTube Music
                    loadUrl("https://accounts.google.com/ServiceLogin?continue=https%3A%2F%2Fmusic.youtube.com")
                }
            }
        )
    }
}
```

---

## STEP 9 — Update AccountViewModel.kt (add YTM cookie method + sync)

Replace the ENTIRE content of `app/src/main/kotlin/com/phantasia/music/ui/AccountViewModel.kt` with:

```kotlin
package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantasia.music.accounts.SpotifyImporter
import com.phantasia.music.accounts.SyncResult
import com.phantasia.music.accounts.YouTubeMusicImporter
import com.phantasia.music.network.InnerTubeRequests
import com.phantasia.music.network.MusicRepository
import com.phantasia.music.network.TrackModel
import com.phantasia.music.security.SecurePreferenceManager
import com.phantasia.music.storage.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_YTM_COOKIE = "ytm_cookie"
private const val KEY_YTM_NAME   = "ytm_account_name"
private const val KEY_YTM_EMAIL  = "ytm_account_email"

data class AccountUiState(
    val accounts:         List<AccountEntity>          = emptyList(),
    val ytmPlaylists:     List<ImportedPlaylistEntity> = emptyList(),
    val spotifyPlaylists: List<ImportedPlaylistEntity> = emptyList(),
    val isSyncing:        Boolean                      = false,
    val syncMessage:      String?                      = null,
    val error:            String?                      = null,
    val ytmAccountName:   String?                      = null,
    val ytmAccountEmail:  String?                      = null
) {
    val isYtmConnected     get() = accounts.any { it.service == AccountService.YOUTUBE_MUSIC }
    val isSpotifyConnected get() = accounts.any { it.service == AccountService.SPOTIFY }
}

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val accountDao:      AccountDao,
    private val playlistDao:     ImportedPlaylistDao,
    private val ytmImporter:     YouTubeMusicImporter,
    private val spotifyImporter: SpotifyImporter,
    private val securePrefs:     SecurePreferenceManager,
    private val requests:        InnerTubeRequests,
    private val songDao:         SongDao
) : ViewModel() {

    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    // Expose the stored YTM cookie for the request interceptor
    val ytmCookie: String? get() = securePrefs.getString(KEY_YTM_COOKIE)

    init {
        accountDao.getAllAccounts()
            .onEach { _state.update { s -> s.copy(accounts = it) } }
            .launchIn(viewModelScope)
        playlistDao.getPlaylists(AccountService.YOUTUBE_MUSIC)
            .onEach { _state.update { s -> s.copy(ytmPlaylists = it) } }
            .launchIn(viewModelScope)
        playlistDao.getPlaylists(AccountService.SPOTIFY)
            .onEach { _state.update { s -> s.copy(spotifyPlaylists = it) } }
            .launchIn(viewModelScope)

        // Restore saved YTM cookie and inject into requests
        val saved = securePrefs.getString(KEY_YTM_COOKIE)
        if (!saved.isNullOrBlank()) {
            requests.ytmCookie = saved
            _state.update { it.copy(
                ytmAccountName  = securePrefs.getString(KEY_YTM_NAME),
                ytmAccountEmail = securePrefs.getString(KEY_YTM_EMAIL)
            )}
        }
    }

    // ── YouTube Music — WebView cookie login ──────────────────────────────────
    /**
     * Called by YtmLoginScreen when the WebView lands on music.youtube.com
     * and we've extracted the session cookie. Exactly how Velune works.
     */
    fun onYtmCookieReceived(cookie: String) {
        viewModelScope.launch(Dispatchers.IO) {
            securePrefs.putString(KEY_YTM_COOKIE, cookie)
            // Inject cookie into all future InnerTube requests
            requests.ytmCookie = cookie

            // Save account record
            accountDao.upsert(AccountEntity(
                service      = AccountService.YOUTUBE_MUSIC,
                displayName  = "YouTube Music",
                email        = "",
                accessToken  = "cookie_auth",
                refreshToken = ""
            ))

            // Trigger sync
            syncYtm()
        }
    }

    fun syncYtm() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isSyncing = true, syncMessage = "Syncing YouTube Music…") }
            val result = ytmImporter.syncAll()
            _state.update {
                when (result) {
                    is SyncResult.Success -> it.copy(
                        isSyncing   = false,
                        syncMessage = "Synced ${result.playlistCount} playlists, ${result.trackCount} tracks"
                    )
                    is SyncResult.Failure -> it.copy(isSyncing = false, error = result.message)
                }
            }
        }
    }

    fun logoutYtm() {
        viewModelScope.launch(Dispatchers.IO) {
            securePrefs.remove(KEY_YTM_COOKIE)
            securePrefs.remove(KEY_YTM_NAME)
            securePrefs.remove(KEY_YTM_EMAIL)
            requests.ytmCookie = null
            accountDao.logout(AccountService.YOUTUBE_MUSIC)
            playlistDao.clearService(AccountService.YOUTUBE_MUSIC)
            _state.update { it.copy(
                syncMessage     = "YouTube Music disconnected",
                ytmAccountName  = null,
                ytmAccountEmail = null
            )}
        }
    }

    // ── Spotify ───────────────────────────────────────────────────────────────
    fun onYtmLoginSuccess(
        displayName: String, email: String, avatarUrl: String,
        accessToken: String, refreshToken: String, expiresIn: Long = 3600L
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            ytmImporter.saveTokens(accessToken, refreshToken, expiresIn)
            accountDao.upsert(AccountEntity(
                service     = AccountService.YOUTUBE_MUSIC,
                displayName = displayName, email = email, avatarUrl = avatarUrl,
                accessToken = "encrypted", refreshToken = "encrypted",
                expiresAt   = System.currentTimeMillis() + (expiresIn * 1000)
            ))
            syncYtm()
        }
    }

    fun onSpotifyLoginSuccess(
        displayName: String, email: String, avatarUrl: String,
        accessToken: String, refreshToken: String, expiresIn: Long = 3600L
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            spotifyImporter.saveTokens(accessToken, refreshToken, expiresIn)
            accountDao.upsert(AccountEntity(
                service     = AccountService.SPOTIFY,
                displayName = displayName, email = email, avatarUrl = avatarUrl,
                accessToken = "encrypted", refreshToken = "encrypted",
                expiresAt   = System.currentTimeMillis() + (expiresIn * 1000)
            ))
            syncSpotify()
        }
    }

    fun syncSpotify() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.update { it.copy(isSyncing = true, syncMessage = "Syncing Spotify…") }
            val result = spotifyImporter.syncAll()
            _state.update {
                when (result) {
                    is SyncResult.Success -> it.copy(
                        isSyncing   = false,
                        syncMessage = "Synced ${result.playlistCount} playlists, ${result.trackCount} tracks"
                    )
                    is SyncResult.Failure -> it.copy(isSyncing = false, error = result.message)
                }
            }
        }
    }

    fun logoutSpotify() {
        viewModelScope.launch(Dispatchers.IO) {
            spotifyImporter.clearTokens()
            accountDao.logout(AccountService.SPOTIFY)
            playlistDao.clearService(AccountService.SPOTIFY)
            _state.update { it.copy(syncMessage = "Spotify disconnected") }
        }
    }

    fun clearMessage() { _state.update { it.copy(syncMessage = null, error = null) } }
}
```

---

## STEP 10 — Update AccountsScreen.kt (wire YTM login to WebView, Spotify to browser)

Replace the ENTIRE content of `app/src/main/kotlin/com/phantasia/music/ui/AccountsScreen.kt` with:

```kotlin
package com.phantasia.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.phantasia.music.Route
import com.phantasia.music.storage.AccountEntity
import com.phantasia.music.storage.AccountService
import com.phantasia.music.storage.ImportedPlaylistEntity

@Composable
fun AccountsScreen(nav: NavController) {
    val vm: AccountViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val snackbarHost = remember { SnackbarHostState() }

    LaunchedEffect(state.syncMessage) {
        state.syncMessage?.let { snackbarHost.showSnackbar(it); vm.clearMessage() }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbarHost.showSnackbar("Error: $it"); vm.clearMessage() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        containerColor = PhantasiaColors.Bg
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(padding).background(
                Brush.verticalGradient(listOf(PhantasiaColors.GradientTop, PhantasiaColors.Bg))
            )
        ) {
            LazyColumn(
                modifier       = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                item {
                    Spacer(Modifier.height(56.dp))
                    Text("Connected accounts",
                        style    = MaterialTheme.typography.headlineSmall,
                        color    = PhantasiaColors.OnSurface,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
                    Text("Connect your music accounts to import playlists and liked songs. " +
                         "Spotify tracks play via YouTube Music.",
                        style    = MaterialTheme.typography.bodyMedium,
                        color    = PhantasiaColors.OnSurfaceDim,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp))
                    Spacer(Modifier.height(16.dp))
                }

                // ── YouTube Music card ─────────────────────────────────────────
                item {
                    val ytmAccount = state.accounts.find { it.service == AccountService.YOUTUBE_MUSIC }
                    AccountCard(
                        serviceName  = "YouTube Music",
                        serviceColor = Color(0xFFFF0000),
                        icon         = Icons.Default.MusicNote,
                        account      = ytmAccount,
                        playlists    = state.ytmPlaylists,
                        isSyncing    = state.isSyncing,
                        connectLabel = "Sign in with Google",
                        onConnect    = { nav.navigate(Route.YtmLogin.path) },
                        onSync       = { vm.syncYtm() },
                        onLogout     = { vm.logoutYtm() }
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // ── Spotify card ───────────────────────────────────────────────
                item {
                    val spotifyAccount = state.accounts.find { it.service == AccountService.SPOTIFY }
                    AccountCard(
                        serviceName  = "Spotify",
                        serviceColor = Color(0xFF1DB954),
                        icon         = Icons.Default.Headphones,
                        account      = spotifyAccount,
                        playlists    = state.spotifyPlaylists,
                        isSyncing    = state.isSyncing,
                        connectLabel = "Connect with Spotify",
                        onConnect    = { nav.navigate(Route.SpotifyLogin.path) },
                        onSync       = { vm.syncSpotify() },
                        onLogout     = { vm.logoutSpotify() }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountCard(
    serviceName:  String,
    serviceColor: Color,
    icon:         ImageVector,
    account:      AccountEntity?,
    playlists:    List<ImportedPlaylistEntity>,
    isSyncing:    Boolean,
    connectLabel: String,
    onConnect:    () -> Unit,
    onSync:       () -> Unit,
    onLogout:     () -> Unit
) {
    var showLogout by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = PhantasiaColors.SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier         = Modifier.size(44.dp).clip(CircleShape).background(serviceColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(serviceName, style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold, color = PhantasiaColors.OnSurface)
                    if (account != null) {
                        Text(if (account.email.isNotBlank()) account.email else "Connected",
                            style = MaterialTheme.typography.bodySmall,
                            color = PhantasiaColors.OnSurfaceDim)
                    } else {
                        Text("Not connected", style = MaterialTheme.typography.bodySmall,
                            color = PhantasiaColors.OnSurfaceDim)
                    }
                }
                if (account != null) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Connected",
                        tint = serviceColor, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            if (account == null) {
                Button(onClick = onConnect, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = serviceColor)) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(connectLabel, color = Color.White)
                }
            } else {
                if (playlists.isNotEmpty()) {
                    Text("${playlists.size} playlists imported",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PhantasiaColors.OnSurfaceDim)
                    Spacer(Modifier.height(6.dp))
                    playlists.take(3).forEach { pl ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(Icons.Default.QueueMusic, null, Modifier.size(14.dp),
                                tint = PhantasiaColors.OnSurfaceDim)
                            Spacer(Modifier.width(6.dp))
                            Text("${pl.name} · ${pl.trackCount} songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = PhantasiaColors.OnSurfaceDim)
                        }
                    }
                    if (playlists.size > 3) {
                        Text("+${playlists.size - 3} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = PhantasiaColors.OnSurfaceDim.copy(alpha = 0.7f))
                    }
                    Spacer(Modifier.height(12.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onSync, enabled = !isSyncing,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PhantasiaColors.Primary)) {
                        if (isSyncing) CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp,
                            color = PhantasiaColors.Primary)
                        else Icon(Icons.Default.Sync, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isSyncing) "Syncing…" else "Sync")
                    }
                    OutlinedButton(onClick = { showLogout = true },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = PhantasiaColors.Error)) {
                        Icon(Icons.Default.Logout, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Disconnect")
                    }
                }
            }
        }
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            containerColor   = PhantasiaColors.SurfaceHigh,
            title            = { Text("Disconnect $serviceName?", color = PhantasiaColors.OnSurface) },
            text             = { Text("This removes all imported playlists. You can reconnect anytime.",
                color = PhantasiaColors.OnSurfaceDim) },
            confirmButton    = {
                TextButton(onClick = { onLogout(); showLogout = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = PhantasiaColors.Error)) {
                    Text("Disconnect")
                }
            },
            dismissButton    = {
                TextButton(onClick = { showLogout = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = PhantasiaColors.Primary)) {
                    Text("Cancel")
                }
            }
        )
    }
}
```

---

## STEP 11 — Add YtmLogin route to AppNavigation.kt

Read `app/src/main/kotlin/com/phantasia/music/AppNavigation.kt`

In the `sealed class Route`, add if not present:
```kotlin
object YtmLogin : Route("ytm_login")
```

In the `NavHost` composables, add if not present:
```kotlin
composable(Route.YtmLogin.path) { YtmLoginScreen(nav) }
```

Add import at top if not present:
```kotlin
import com.phantasia.music.ui.YtmLoginScreen
```

---

## STEP 12 — Fix PlayerScreen.kt background to dark purple

In `app/src/main/kotlin/com/phantasia/music/ui/PlayerScreen.kt`, find the line:
```kotlin
.background(MaterialTheme.colorScheme.background)
```
Replace with:
```kotlin
.background(PhantasiaColors.Bg)
```

Find any `Color.White` used for text in the player — those are fine to keep as-is on the dark background.

---

## STEP 13 — Add WebView dependency to build.gradle.kts

In `app/build.gradle.kts`, add this inside `dependencies` if not already there:
```kotlin
// WebView is part of Android SDK, no extra dep needed
// But we need this for the AndroidView composable:
implementation("androidx.compose.ui:ui-viewbinding")
```

Also verify `android.permission.INTERNET` is in AndroidManifest.xml — it should already be there.

---

## STEP 14 — Run build and fix all errors

```bash
./gradlew assembleDebug 2>&1 | grep -E "^e:|error:|BUILD|FAILED" | head -40
```

Common fixes:
- If `BasicTextField` conflicts — rename our wrapper to `PhantasiaSearchField` and update SearchScreen
- If `PhantasiaColors` not found in HomeScreen — make sure the import `com.phantasia.music.ui.PhantasiaColors` is present or use `MaterialTheme.colorScheme` equivalents
- If `Route.YtmLogin` not found — check AppNavigation.kt was updated
- Fix every error shown, rebuild until `BUILD SUCCESSFUL`

---

## STEP 15 — Commit

```bash
git add -A
git commit -m "feat: working search, home feed, dark purple theme, YTM WebView login, Spotify login"
git log --oneline -5
git status
```
