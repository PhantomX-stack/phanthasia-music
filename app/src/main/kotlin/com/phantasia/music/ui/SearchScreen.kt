package com.phantasia.music.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phantasia.music.Route
import com.phantasia.music.network.AlbumModel
import com.phantasia.music.network.ArtistModel
import com.phantasia.music.network.SearchResultModel
import com.phantasia.music.network.TrackModel
import com.phantasia.music.storage.SearchHistoryEntity

@Composable
fun SearchScreen(nav: NavController) {
    val vm: SearchViewModel = hiltViewModel()
    // removed downloadVm to fix compilation

    val settingsVm: SettingsViewModel = hiltViewModel()
    val settings   by settingsVm.state.collectAsState()
    val context    = LocalContext.current

    val query       by vm.query.collectAsState()
    val state       by vm.uiState.collectAsState()
    val history     by vm.history.collectAsState()
    val suggestions by vm.suggestions.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted */ }

    val musicDir = remember {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(
                PhantasiaColors.Midnight, PhantasiaColors.GradMid, PhantasiaColors.GradBot
            ))
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(52.dp))

            // ── Glass search bar ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Search icon animates to purple when active
                val iconColor by animateColorAsState(
                    targetValue = if (query.isNotEmpty()) PhantasiaColors.Primary
                                  else PhantasiaColors.OnDim,
                    animationSpec = tween(300), label = "search_icon"
                )
                Icon(Icons.Default.Search, null,
                    tint = iconColor, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))

                BasicTextField(
                    value          = query,
                    onValueChange  = { vm.onEvent(SearchUiEvent.QueryChanged(it)) },
                    textStyle      = MaterialTheme.typography.bodyLarge
                        .copy(color = PhantasiaColors.OnSurface),
                    singleLine     = true,
                    cursorBrush   = SolidColor(PhantasiaColors.Primary),
                    modifier       = Modifier.weight(1f).focusRequester(focusRequester),
                    decorationBox  = { inner ->
                        if (query.isEmpty()) {
                            Text("Search songs, artists, albums…",
                                color = PhantasiaColors.OnHint,
                                style = MaterialTheme.typography.bodyLarge)
                        }
                        inner()
                    }
                )

                // Animated clear/history buttons
                AnimatedVisibility(query.isNotEmpty(),
                    enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                    exit  = fadeOut(tween(200)) + scaleOut(tween(200))
                ) {
                    IconButton(onClick = { vm.onEvent(SearchUiEvent.QueryChanged("")) },
                        modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, "Clear",
                            tint = PhantasiaColors.OnDim, modifier = Modifier.size(16.dp))
                    }
                }
            }

            // ── Content area with animated transitions ─────────────────────────
            AnimatedContent(
                targetState   = when {
                    state is SearchUiState.Loading          -> "loading"
                    state is SearchUiState.Results          -> "results"
                    state is SearchUiState.Error            -> "error"
                    query.isNotEmpty() && suggestions.isNotEmpty() -> "suggestions"
                    history.isNotEmpty()                    -> "history"
                    else                                    -> "empty"
                },
                transitionSpec = {
                    fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                },
                label = "search_content"
            ) { target ->
                when (target) {

                    "loading" -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            CircularProgressIndicator(color = PhantasiaColors.Primary)
                        }
                    }

                    "error" -> {
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(32.dp)) {
                                Text("Search failed", color = PhantasiaColors.OnSurface,
                                    style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(8.dp))
                                Text((state as? SearchUiState.Error)?.message ?: "",
                                    color = PhantasiaColors.Error,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    "results" -> {
                        val results = (state as SearchUiState.Results).items
                        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                            if (results.isEmpty()) {
                                item {
                                    Box(Modifier.fillMaxWidth().padding(top = 64.dp),
                                        Alignment.Center) {
                                        Text("No results for \"$query\"",
                                            color = PhantasiaColors.OnDim,
                                            style = MaterialTheme.typography.bodyLarge)
                                    }
                                }
                            } else {
                                item {
                                    Text("${results.size} results",
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = PhantasiaColors.OnDim,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                                }
                                items(results, key = { item ->
                                    when (item) {
                                        is SearchResultModel.TrackResult  -> "track_${item.track.videoId}"
                                        is SearchResultModel.AlbumResult  -> "album_${item.album.browseId}"
                                        is SearchResultModel.ArtistResult -> "artist_${item.artist.browseId}"
                                    }
                                }) { item ->
                                    when (item) {
                                        is SearchResultModel.TrackResult -> GlassTrackRow(
                                            track      = item.track,
                                            onDownload = {
                                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                                    if (ContextCompat.checkSelfPermission(context,
                                                            Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                                                        permLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                        return@GlassTrackRow
                                                    }
                                                }

                                            },
                                            onClick = {
                                                vm.onEvent(SearchUiEvent.TrackSelected(item.track.videoId))
                                                nav.navigate(Route.Player.build(item.track.videoId))
                                            }
                                        )
                                        is SearchResultModel.AlbumResult  -> GlassAlbumRow(item.album)
                                        is SearchResultModel.ArtistResult -> GlassArtistRow(item.artist)
                                    }
                                }
                            }
                        }
                    }

                    "suggestions" -> {
                        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                            items(suggestions) { suggestion ->
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { vm.searchFromHistory(suggestion) }
                                        .padding(horizontal = 20.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.TrendingUp, null,
                                        tint = PhantasiaColors.Primary,
                                        modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(14.dp))
                                    Text(suggestion, color = PhantasiaColors.OnSurface,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    "history" -> {
                        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text("Recent searches",
                                        style      = MaterialTheme.typography.titleSmall,
                                        color      = PhantasiaColors.OnSurface,
                                        fontWeight = FontWeight.SemiBold)
                                    TextButton(
                                        onClick = { vm.onEvent(SearchUiEvent.ClearHistory) }
                                    ) {
                                        Text("Clear all",
                                            color = PhantasiaColors.Primary,
                                            style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                            if (history.isNotEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 6.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(PhantasiaColors.Primary.copy(alpha = 0.15f))
                                            .clickable {
                                                val random = vm.playRandomFromHistory()
                                                if (random != null) {
                                                    vm.searchFromHistory(random.query)
                                                }
                                            }
                                            .padding(horizontal = 20.dp, vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center) {
                                            Icon(Icons.Default.Shuffle, null,
                                                tint = PhantasiaColors.Primary, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(8.dp))
                                            Text("Play from history",
                                                color = PhantasiaColors.Primary,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                            items(history, key = { it.id }) { item ->
                                HistoryRow(
                                    item     = item,
                                    onTap    = { vm.searchFromHistory(item.query) },
                                    onDelete = { vm.deleteHistoryItem(item) }
                                )
                            }
                        }
                    }

                    else -> {
                        // Empty — no history, no query
                        Box(Modifier.fillMaxSize(), Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔍", style = MaterialTheme.typography.displaySmall)
                                Spacer(Modifier.height(12.dp))
                                Text("Search for anything",
                                    color = PhantasiaColors.OnDim,
                                    style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── History row with individual delete ────────────────────────────────────────
@Composable
private fun HistoryRow(
    item:     SearchHistoryEntity,
    onTap:    () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable { onTap() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(36.dp).clip(CircleShape)
            .background(Color.White.copy(alpha = 0.07f)),
            contentAlignment = Alignment.Center) {
            Icon(Icons.Default.History, null,
                tint = PhantasiaColors.OnDim, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(12.dp))
        Text(item.query, color = PhantasiaColors.OnSurface,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f), maxLines = 1,
            overflow = TextOverflow.Ellipsis)
        // Individual delete X button
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, "Remove",
                tint = PhantasiaColors.OnHint, modifier = Modifier.size(15.dp))
        }
    }
}

// ── Glass result rows ─────────────────────────────────────────────────────────
@Composable
fun GlassTrackRow(track: TrackModel, onDownload: () -> Unit = {}, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.07f))) {
            AsyncImage(model = track.artworkUrl, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(track.title, color = PhantasiaColors.OnSurface,
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text("${track.artistName} · Song", color = PhantasiaColors.OnDim,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onDownload, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.FileDownload, "Download",
                tint = PhantasiaColors.Primary.copy(alpha = 0.7f),
                modifier = Modifier.size(18.dp))
        }
        Icon(Icons.Default.PlayArrow, null, tint = PhantasiaColors.OnDim,
            modifier = Modifier.size(20.dp))
    }
}

@Composable
fun GlassAlbumRow(album: AlbumModel) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.07f))) {
            AsyncImage(model = album.thumbnailUrl, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Album", style = MaterialTheme.typography.labelSmall,
                    color = PhantasiaColors.Primary)
                Text("·", color = PhantasiaColors.OnHint)
                Text(album.year, style = MaterialTheme.typography.labelSmall,
                    color = PhantasiaColors.OnHint)
            }
            Text(album.title, color = PhantasiaColors.OnSurface,
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(album.artistName, color = PhantasiaColors.OnDim,
                style = MaterialTheme.typography.bodySmall, maxLines = 1)
        }
    }
}

@Composable
fun GlassArtistRow(artist: ArtistModel) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(52.dp).clip(CircleShape)
            .background(Brush.linearGradient(listOf(
                PhantasiaColors.PrimaryDim, PhantasiaColors.Secondary
            ))), contentAlignment = Alignment.Center) {
            if (artist.thumbnailUrl.isNotEmpty()) {
                AsyncImage(model = artist.thumbnailUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Text(artist.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge, color = Color.White)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("Artist", style = MaterialTheme.typography.labelSmall,
                color = PhantasiaColors.Secondary)
            Text(artist.name, color = PhantasiaColors.OnSurface,
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                maxLines = 1)
        }
    }
}
