package com.phantasia.music.ui

import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phantasia.music.Route
import com.phantasia.music.network.TrackModel
import com.phantasia.music.player.QueueState
import com.phantasia.music.player.RepeatMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(videoId: String, nav: NavController) {
    val vm: PlayerViewModel = hiltViewModel()
    val settingsVm: SettingsViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    val queueState by vm.queueState.collectAsState()
    val settings by settingsVm.state.collectAsState()

    LaunchedEffect(videoId) {
        val currState = state
        if (currState !is PlayerUiState.Playing || currState.track.videoId != videoId) {
            val existingInQueue = queueState.tracks.firstOrNull { it.videoId == videoId }
            if (existingInQueue != null) {
                vm.onEvent(PlayerUiEvent.PlayTrack(existingInQueue, queueState.tracks))
            } else {
                vm.onEvent(PlayerUiEvent.PlayTrack(TrackModel(videoId, "Loading…", "", "", "", 0)))
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PhantasiaColors.Midnight)
    ) {
        when (val s = state) {
            is PlayerUiState.Idle, is PlayerUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PhantasiaColors.Primary, modifier = Modifier.size(54.dp), strokeWidth = 4.dp)
                        Spacer(Modifier.height(20.dp))
                        Text(
                            "Connecting to audio stream…",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = PhantasiaColors.OnDim
                        )
                    }
                }
            }
            is PlayerUiState.Playing -> PlayerUI(
                state = s,
                queueState = queueState,
                settings = settings,
                onEvent = vm::onEvent,
                nav = nav
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun PlayerUI(
    state:      PlayerUiState.Playing,
    queueState: QueueState,
    settings:   SettingsState,
    onEvent:    (PlayerUiEvent) -> Unit,
    nav:        NavController
) {
    var showQueueSheet by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {

        // ── Dynamic Background ──────────────────────────────────────────
        DynamicBackground(artworkUrl = state.track.artworkUrl)

        // ── Gradient Scrim ──────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.40f),
                        0.40f to Color.Black.copy(alpha = 0.25f),
                        1.0f to Color.Black.copy(alpha = 0.92f)
                    )
                )
        )

        // ── Main Content ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 44.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Top bar: Back + Larger Song/Video Switcher + Queue/Lyrics ──
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { nav.navigateUp() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                ) {
                    Icon(
                        Icons.Default.KeyboardArrowDown,
                        contentDescription = "Collapse",
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Song / Video Toggle (Bigger & Rounded 50% Capsule)
                Surface(
                    color = Color.White.copy(alpha = 0.18f),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.height(42.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxHeight().padding(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isVideo = state.viewMode == PlayerViewMode.VIDEO
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (!isVideo) PhantasiaColors.Primary else Color.Transparent)
                                .clickable { onEvent(PlayerUiEvent.SetViewThumbnail) }
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Song",
                                color = if (!isVideo) Color.Black else Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isVideo) PhantasiaColors.Primary else Color.Transparent)
                                .clickable { onEvent(PlayerUiEvent.SetViewVideo) }
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                        ) {
                            Text(
                                "Video",
                                color = if (isVideo) Color.Black else Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Lyrics button
                    IconButton(
                        onClick = { onEvent(PlayerUiEvent.ToggleLyrics) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (state.showLyrics) PhantasiaColors.Primary.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            Icons.Default.Lyrics,
                            contentDescription = "Lyrics",
                            tint = if (state.showLyrics) PhantasiaColors.Primary else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Queue button (Opens in-player Queue Sheet)
                    IconButton(
                        onClick = { showQueueSheet = true },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // ── Center Media Area (Artwork vs Video vs Lyrics) ───────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.showLyrics -> {
                        LyricsDisplay(
                            lines = state.lyricsLines,
                            positionMs = state.positionMs,
                            isSearching = state.isSearchingLyrics,
                            statusMessage = state.lyricsStatusMessage,
                            songTitle = state.track.title,
                            onSeekTo = { pos -> onEvent(PlayerUiEvent.Seek(pos)) },
                            onSearchOnline = { onEvent(PlayerUiEvent.SearchLyricsOnline(null)) },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    state.viewMode == PlayerViewMode.VIDEO -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.Black)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        val webSettings = this.settings
                                        webSettings.javaScriptEnabled = true
                                        webSettings.domStorageEnabled = true
                                        webSettings.databaseEnabled = true
                                        webSettings.mediaPlaybackRequiresUserGesture = false
                                        webSettings.loadWithOverviewMode = true
                                        webSettings.useWideViewPort = true
                                        webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                        webSettings.userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36"
                                        webViewClient = object : WebViewClient() {
                                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean = false
                                        }
                                        webChromeClient = WebChromeClient()
                                        tag = state.track.videoId
                                        loadDataWithBaseURL(
                                            "https://www.youtube.com",
                                            buildYouTubeEmbedHtml(state.track.videoId),
                                            "text/html",
                                            "UTF-8",
                                            "https://www.youtube.com"
                                        )
                                    }
                                },
                                update = { webView ->
                                    if (webView.tag != state.track.videoId) {
                                        webView.tag = state.track.videoId
                                        webView.loadDataWithBaseURL(
                                            "https://www.youtube.com",
                                            buildYouTubeEmbedHtml(state.track.videoId),
                                            "text/html",
                                            "UTF-8",
                                            "https://www.youtube.com"
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color.White.copy(alpha = 0.08f))
                        ) {
                            AsyncImage(
                                model = state.track.artworkUrl,
                                contentDescription = state.track.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }

            // ── Track Title, Artist, & Large Rounded Controls ─────────────
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = state.track.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = state.track.artistName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.8f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (state.track.albumTitle.isNotBlank() || state.durationMs > 0L) {
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (state.track.albumTitle.isNotBlank()) {
                                    PlayerInfoChip(Icons.Default.Album, state.track.albumTitle)
                                }
                                if (state.durationMs > 0L) {
                                    PlayerInfoChip(Icons.Default.Schedule, formatMs(state.durationMs))
                                }
                            }
                        }
                    }
                    IconButton(
                        onClick = { onEvent(PlayerUiEvent.ToggleFavourite) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.12f))
                    ) {
                        Icon(
                            if (state.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (state.isFavourite) PhantasiaColors.Primary else Color.White,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Progress Bar (PhantasiaProgressBar with transparent touch Slider) ──
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PhantasiaProgressBar(
                        style = settings.progressBarStyle,
                        positionMs = state.positionMs,
                        durationMs = state.durationMs,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Slider(
                        value = state.positionMs.toFloat(),
                        onValueChange = { onEvent(PlayerUiEvent.Seek(it.toLong())) },
                        valueRange = 0f..state.durationMs.toFloat().coerceAtLeast(1f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Transparent,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        formatMs(state.positionMs),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                    Text(
                        formatMs(state.durationMs),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                Spacer(Modifier.height(14.dp))

                // ── Big Tactile Player Action Buttons ─────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Shuffle (Bigger touch target)
                    IconButton(
                        onClick = { onEvent(PlayerUiEvent.ToggleShuffle) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (state.shuffleEnabled) PhantasiaColors.Primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (state.shuffleEnabled) PhantasiaColors.Primary else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Previous (Bigger 64.dp button)
                    IconButton(
                        onClick = { onEvent(PlayerUiEvent.SkipPrev) },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            Icons.Filled.SkipPrevious,
                            contentDescription = "Previous",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Play / Pause Circle (Enlarged 82.dp with vibrant gradient & spring pulse)
                    val playButtonScale by animateFloatAsState(
                        targetValue = if (state.isPlaying) 1.05f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "play_scale"
                    )
                    Box(
                        modifier = Modifier
                            .size(82.dp)
                            .graphicsLayer {
                                scaleX = playButtonScale
                                scaleY = playButtonScale
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(PhantasiaColors.Primary, PhantasiaColors.Secondary)
                                )
                            )
                            .clickable {
                                if (state.isPlaying) onEvent(PlayerUiEvent.Pause)
                                else onEvent(PlayerUiEvent.Play)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Crossfade(targetState = state.isPlaying, label = "play_pause_crossfade") { isPlaying ->
                            Icon(
                                imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.Black,
                                modifier = Modifier.size(46.dp)
                            )
                        }
                    }

                    // Next (Bigger 64.dp button)
                    IconButton(
                        onClick = { onEvent(PlayerUiEvent.SkipNext) },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.14f))
                    ) {
                        Icon(
                            Icons.Filled.SkipNext,
                            contentDescription = "Next",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    // Repeat (Bigger touch target)
                    IconButton(
                        onClick = { onEvent(PlayerUiEvent.CycleRepeat) },
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (state.repeatMode != RepeatMode.NONE) PhantasiaColors.Primary.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = when (state.repeatMode) {
                                RepeatMode.ONE -> Icons.Filled.RepeatOne
                                else -> Icons.Filled.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (state.repeatMode != RepeatMode.NONE) PhantasiaColors.Primary else Color.White.copy(alpha = 0.85f),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ── Up Next Quick Pill at bottom (Endless Radio Info) ──────
                val nextTrack = queueState.tracks.getOrNull(queueState.currentIndex + 1)
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showQueueSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(PhantasiaColors.Primary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.GraphicEq,
                                    contentDescription = null,
                                    tint = PhantasiaColors.Primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (nextTrack != null) "UP NEXT: ${nextTrack.title}" else "Endless Radio Station",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (nextTrack != null) "${nextTrack.artistName} • Endless auto-play active" else "${queueState.tracks.size} tracks queued forever",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PhantasiaColors.PrimaryGlow,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "View Queue",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // ── IN-PLAYER QUEUE BOTTOM SHEET ─────────────────────────────────
        if (showQueueSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showQueueSheet = false },
                sheetState = sheetState,
                containerColor = PhantasiaColors.Midnight,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.78f)
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Playing Queue",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "${queueState.tracks.size} songs in queue",
                                style = MaterialTheme.typography.bodySmall,
                                color = PhantasiaColors.OnDim
                            )
                        }
                        Row {
                            IconButton(onClick = { nav.navigate(Route.Queue.path); showQueueSheet = false }) {
                                Icon(Icons.Default.OpenInFull, contentDescription = "Full Screen", tint = Color.White)
                            }
                            IconButton(onClick = { onEvent(PlayerUiEvent.ClearQueue) }) {
                                Icon(Icons.Default.ClearAll, contentDescription = "Clear", tint = PhantasiaColors.OnDim)
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(queueState.tracks) { index, track ->
                            val isCurrent = index == queueState.currentIndex
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isCurrent) PhantasiaColors.Primary.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onEvent(PlayerUiEvent.PlayQueueIndex(index))
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White.copy(alpha = 0.08f))
                                    ) {
                                        AsyncImage(
                                            model = track.artworkUrl,
                                            contentDescription = track.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isCurrent) PhantasiaColors.Primary else Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = track.artistName,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PhantasiaColors.OnDim,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    IconButton(
                                        onClick = { onEvent(PlayerUiEvent.RemoveQueueIndex(index)) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = PhantasiaColors.OnDim,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
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
private fun PlayerInfoChip(icon: ImageVector, label: String) {
    Surface(
        color = Color.White.copy(alpha = 0.16f),
        contentColor = Color.White,
        shape = RoundedCornerShape(50),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
            Text(label, style = MaterialTheme.typography.labelMedium, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun formatMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}

private fun buildYouTubeEmbedHtml(videoId: String): String {
    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { box-sizing: border-box; margin: 0; padding: 0; }
                html, body { width: 100%; height: 100%; background: #000000; overflow: hidden; display: flex; align-items: center; justify-content: center; }
                iframe { width: 100%; height: 100%; border: none; }
            </style>
        </head>
        <body>
            <iframe
                id="ytplayer"
                type="text/html"
                src="https://www.youtube.com/embed/$videoId?autoplay=1&playsinline=1&controls=1&fs=1&rel=0&enablejsapi=1&origin=https://www.youtube.com&widget_referrer=https://www.youtube.com"
                frameborder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                allowfullscreen>
            </iframe>
        </body>
        </html>
    """.trimIndent()
}

