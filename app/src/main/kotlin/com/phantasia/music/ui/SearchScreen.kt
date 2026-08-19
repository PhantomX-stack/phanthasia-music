package com.phantasia.music.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.ImeAction
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
import kotlinx.coroutines.launch

private val SEARCH_FILTERS = listOf("All", "Songs", "Videos", "Artists", "Albums")

private sealed interface SearchScreenContent {
    object Loading : SearchScreenContent
    data class Results(val items: List<SearchResultModel>) : SearchScreenContent
    data class Error(val message: String) : SearchScreenContent
    data class Suggestions(val list: List<String>) : SearchScreenContent
    data class History(val list: List<SearchHistoryEntity>) : SearchScreenContent
    object Empty : SearchScreenContent
}

@Composable
fun SearchScreen(nav: NavController) {
    val vm: SearchViewModel = hiltViewModel()
    val playerVm: PlayerViewModel = hiltViewModel()
    val downloadVm: DownloadViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val query       by vm.query.collectAsState()
    val filter      by vm.filter.collectAsState()
    val state       by vm.uiState.collectAsState()
    val history     by vm.history.collectAsState()
    val suggestions by vm.suggestions.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val context = LocalContext.current
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted */ }

    val contentState: SearchScreenContent = remember(state, query, suggestions, history) {
        when {
            state is SearchUiState.Loading -> SearchScreenContent.Loading
            state is SearchUiState.Results -> SearchScreenContent.Results((state as SearchUiState.Results).items)
            state is SearchUiState.Error -> SearchScreenContent.Error((state as SearchUiState.Error).message)
            query.isNotEmpty() && suggestions.isNotEmpty() -> SearchScreenContent.Suggestions(suggestions)
            history.isNotEmpty() -> SearchScreenContent.History(history)
            else -> SearchScreenContent.Empty
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = PhantasiaColors.Midnight
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(PhantasiaColors.Midnight, PhantasiaColors.GradMid, PhantasiaColors.GradBot)
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(Modifier.height(16.dp))

                // ── YouTube Music Search Bar ───────────────────────────────────────────────
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.White.copy(alpha = 0.08f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconColor by animateColorAsState(
                        targetValue = if (query.isNotEmpty()) PhantasiaColors.Primary
                        else PhantasiaColors.OnDim,
                        animationSpec = tween(300), label = "search_icon"
                    )
                    Icon(
                        Icons.Default.Search, null,
                        tint = iconColor, modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))

                    BasicTextField(
                        value = query,
                        onValueChange = { vm.onEvent(SearchUiEvent.QueryChanged(it)) },
                        textStyle = MaterialTheme.typography.bodyLarge
                            .copy(color = PhantasiaColors.OnSurface),
                        singleLine = true,
                        cursorBrush = SolidColor(PhantasiaColors.Primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = {
                            vm.onEvent(SearchUiEvent.SearchSubmitted)
                        }),
                        modifier = Modifier.weight(1f).focusRequester(focusRequester),
                        decorationBox = { inner ->
                            if (query.isEmpty()) {
                                Text(
                                    "Search songs, artists, albums…",
                                    color = PhantasiaColors.OnHint,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            inner()
                        }
                    )

                    AnimatedVisibility(
                        query.isNotEmpty(),
                        enter = fadeIn(tween(200)) + scaleIn(tween(200)),
                        exit = fadeOut(tween(200)) + scaleOut(tween(200))
                    ) {
                        IconButton(
                            onClick = { vm.onEvent(SearchUiEvent.QueryChanged("")) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close, "Clear",
                                tint = PhantasiaColors.OnDim, modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // ── YouTube Music Search Filter Chips ─────────────────────────────
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    items(SEARCH_FILTERS) { item ->
                        val isSelected = filter.equals(item, ignoreCase = true)
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) PhantasiaColors.Primary
                            else Color.White.copy(alpha = 0.08f),
                            animationSpec = tween(250), label = "chip_bg"
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(chipBg)
                                .clickable { vm.onEvent(SearchUiEvent.SetFilter(item)) }
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                item,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) Color.White else PhantasiaColors.OnDim,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }

                // ── Content Area ──────────────────────────────────────────────────
                AnimatedContent(
                    targetState = contentState,
                    transitionSpec = {
                        fadeIn(tween(200)) togetherWith fadeOut(tween(150))
                    },
                    label = "search_content"
                ) { target ->
                    when (target) {
                        is SearchScreenContent.Loading -> {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                CircularProgressIndicator(color = PhantasiaColors.Primary)
                            }
                        }

                        is SearchScreenContent.Error -> {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Text(
                                        "Search failed", color = PhantasiaColors.OnSurface,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        target.message,
                                        color = PhantasiaColors.Error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        is SearchScreenContent.Results -> {
                            val results = target.items
                            val tracksInResults = results.filterIsInstance<SearchResultModel.TrackResult>().map { it.track }

                            LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                                if (results.isEmpty()) {
                                    item {
                                        Box(
                                            Modifier.fillMaxWidth().padding(top = 64.dp),
                                            Alignment.Center
                                        ) {
                                            Text(
                                                "No results for \"$query\"",
                                                color = PhantasiaColors.OnDim,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        }
                                    }
                                } else {
                                    item {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "${results.size} results",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = PhantasiaColors.OnDim
                                            )
                                            if (tracksInResults.isNotEmpty()) {
                                                Row(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(50))
                                                        .background(PhantasiaColors.PrimaryDim.copy(alpha = 0.2f))
                                                        .clickable {
                                                            val firstTrack = tracksInResults.first()
                                                            playerVm.onEvent(PlayerUiEvent.PlayTrack(firstTrack, tracksInResults))
                                                            nav.navigate(Route.Player.build(firstTrack.videoId))
                                                        }
                                                        .padding(horizontal = 10.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(Icons.Default.PlayArrow, null, tint = PhantasiaColors.Primary, modifier = Modifier.size(14.dp))
                                                    Text("Play all", style = MaterialTheme.typography.labelSmall, color = PhantasiaColors.Primary, fontWeight = FontWeight.SemiBold)
                                                }
                                            }
                                        }
                                    }
                                    items(results, key = { item ->
                                        when (item) {
                                            is SearchResultModel.TrackResult  -> "track_${item.track.videoId}"
                                            is SearchResultModel.AlbumResult  -> "album_${item.album.browseId}"
                                            is SearchResultModel.ArtistResult -> "artist_${item.artist.browseId}"
                                        }
                                    }) { item ->
                                        when (item) {
                                            is SearchResultModel.TrackResult -> YtmTrackRow(
                                                track = item.track,
                                                onPlayNow = {
                                                    vm.onEvent(SearchUiEvent.TrackSelected(item.track.videoId))
                                                    playerVm.onEvent(PlayerUiEvent.PlayTrack(item.track, tracksInResults))
                                                    nav.navigate(Route.Player.build(item.track.videoId))
                                                },
                                                onDownload = {
                                                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                                                        if (ContextCompat.checkSelfPermission(
                                                                context,
                                                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                                            ) != PackageManager.PERMISSION_GRANTED
                                                        ) {
                                                            permLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                                            return@YtmTrackRow
                                                        }
                                                    }
                                                    downloadVm.queueDownload(item.track)
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar("Added \"${item.track.title}\" to Downloads")
                                                    }
                                                },
                                                onClick = {
                                                    vm.onEvent(SearchUiEvent.TrackSelected(item.track.videoId))
                                                    playerVm.onEvent(PlayerUiEvent.PlayTrack(item.track, tracksInResults))
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

                        is SearchScreenContent.Suggestions -> {
                            val suggestionsList = target.list
                            LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                                items(suggestionsList) { suggestion ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .clickable { vm.searchFromHistory(suggestion) }
                                            .padding(horizontal = 20.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.TrendingUp, null,
                                            tint = PhantasiaColors.Primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(14.dp))
                                        Text(
                                            suggestion, color = PhantasiaColors.OnSurface,
                                            style = MaterialTheme.typography.bodyMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForwardIos, null,
                                            tint = PhantasiaColors.OnHint,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        is SearchScreenContent.History -> {
                            val historyList = target.list
                            LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                                item {
                                    Row(
                                        modifier = Modifier.fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Recent searches",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = PhantasiaColors.OnSurface,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        TextButton(
                                            onClick = { vm.onEvent(SearchUiEvent.ClearHistory) }
                                        ) {
                                            Text(
                                                "Clear all",
                                                color = PhantasiaColors.Primary,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                                items(historyList, key = { it.id }) { item ->
                                    HistoryRow(
                                        item = item,
                                        onTap = { vm.searchFromHistory(item.query) },
                                        onDelete = { vm.deleteHistoryItem(item) }
                                    )
                                }
                                item {
                                    TrendingExploreSection { catQuery ->
                                        vm.executeSearch(catQuery)
                                    }
                                }
                            }
                        }

                        is SearchScreenContent.Empty -> {
                            LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                                item {
                                    TrendingExploreSection { catQuery ->
                                        vm.executeSearch(catQuery)
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
private fun TrendingExploreSection(onSelect: (String) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
        Text(
            "Explore Categories",
            style = MaterialTheme.typography.titleSmall,
            color = PhantasiaColors.OnSurface,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(12.dp))
        val categories = listOf(
            "🔥 Top 50 Hits" to "Top 50 songs",
            "✨ Pop Radio" to "Pop music hits",
            "🎤 Hip-Hop Essentials" to "Hip hop songs",
            "🎸 Rock Legends" to "Rock music hits",
            "💪 Workout Bangers" to "Workout high energy music",
            "🌙 Chill & Lo-Fi" to "Chill lofi beats",
            "🌟 Bollywood Hits" to "Bollywood top songs",
            "🎧 Electronic Dance" to "EDM electronic dance music"
        )
        categories.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pair.forEach { (label, query) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .clickable { onSelect(query) }
                            .padding(horizontal = 14.dp, vertical = 14.dp)
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = PhantasiaColors.OnSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

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
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape)
                .background(Color.White.copy(alpha = 0.07f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.History, null,
                tint = PhantasiaColors.OnDim, modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            item.query, color = PhantasiaColors.OnSurface,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f), maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                Icons.Default.Close, "Remove",
                tint = PhantasiaColors.OnHint, modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
fun YtmTrackRow(
    track: TrackModel,
    onPlayNow: () -> Unit = {},
    onDownload: () -> Unit = {},
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.07f))
        ) {
            AsyncImage(
                model = track.artworkUrl, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )
            // Play overlay on image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.PlayArrow, null,
                    tint = Color.White, modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                track.title, color = PhantasiaColors.OnSurface,
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Song", color = PhantasiaColors.Primary,
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium
                )
                Text(" • ", color = PhantasiaColors.OnHint, style = MaterialTheme.typography.labelSmall)
                Text(
                    track.artistName, color = PhantasiaColors.OnDim,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis
                )
                if (track.durationSeconds > 0) {
                    Text(" • ", color = PhantasiaColors.OnHint, style = MaterialTheme.typography.labelSmall)
                    val m = track.durationSeconds / 60
                    val s = track.durationSeconds % 60
                    Text(
                        String.format("%d:%02d", m, s),
                        color = PhantasiaColors.OnHint,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
        IconButton(onClick = onDownload, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.FileDownload, "Download",
                tint = PhantasiaColors.OnDim,
                modifier = Modifier.size(20.dp)
            )
        }
        IconButton(onClick = onPlayNow, modifier = Modifier.size(36.dp)) {
            Icon(
                Icons.Default.PlayCircleFilled, "Play",
                tint = PhantasiaColors.Primary,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
fun GlassAlbumRow(album: AlbumModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.07f))
        ) {
            AsyncImage(
                model = album.thumbnailUrl, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    "Album", style = MaterialTheme.typography.labelSmall,
                    color = PhantasiaColors.Primary
                )
                if (album.year.isNotEmpty()) {
                    Text("·", color = PhantasiaColors.OnHint)
                    Text(
                        album.year, style = MaterialTheme.typography.labelSmall,
                        color = PhantasiaColors.OnHint
                    )
                }
            }
            Text(
                album.title, color = PhantasiaColors.OnSurface,
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis
            )
            if (album.artistName.isNotEmpty()) {
                Text(
                    album.artistName, color = PhantasiaColors.OnDim,
                    style = MaterialTheme.typography.bodySmall, maxLines = 1
                )
            }
        }
    }
}

@Composable
fun GlassArtistRow(artist: ArtistModel) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(54.dp).clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            PhantasiaColors.PrimaryDim, PhantasiaColors.Secondary
                        )
                    )
                ), contentAlignment = Alignment.Center
            ) {
            if (artist.thumbnailUrl.isNotEmpty()) {
                AsyncImage(
                    model = artist.thumbnailUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()
                )
            } else {
                Text(
                    artist.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleLarge, color = Color.White
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Artist", style = MaterialTheme.typography.labelSmall,
                color = PhantasiaColors.Secondary
            )
            Text(
                artist.name, color = PhantasiaColors.OnSurface,
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}
