package com.phantasia.music.ui

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phantasia.music.Route
import com.phantasia.music.network.TrackModel

// ── Genre & Mood categories matching YouTube Music ────────────
private data class Genre(val label: String, val emoji: String, val searchQuery: String)
private val GENRES = listOf(
    Genre("All",        "✨", ""),
    Genre("Podcasts",   "🎙️", "popular podcasts"),
    Genre("Romance",    "💖", "romantic love songs"),
    Genre("Relax",      "🧘", "relaxing acoustic peaceful music"),
    Genre("Feel good",  "😊", "feel good upbeat hits"),
    Genre("Party",      "🎉", "party dance club hits"),
    Genre("Commute",    "🚗", "commute road trip songs"),
    Genre("Energize",   "⚡", "energy boost gym songs"),
    Genre("Sad",        "🌧️", "sad emotional acoustic songs"),
    Genre("Workout",    "💪", "workout motivational music"),
    Genre("Sleep",      "🌙", "sleep calm ambient lofi"),
    Genre("Focus",      "☕", "focus study instrumental lofi"),
    Genre("Bollywood",  "🌟", "bollywood top hits"),
    Genre("Pop",        "🎵", "pop hits"),
    Genre("Hip Hop",    "🎤", "hip hop rap")
)

@Composable
fun HomeScreen(nav: NavController) {
    val vm: HomeViewModel = hiltViewModel()
    val playerVm: PlayerViewModel = hiltViewModel()
    val state by vm.state.collectAsState()

    var showSearch  by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        PhantasiaColors.GradTop, PhantasiaColors.GradMid, PhantasiaColors.GradBot
                    )
                )
            )
    ) {
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PhantasiaColors.Primary)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Loading music feed…", color = PhantasiaColors.OnDim,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            state.error != null && state.homeSections.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Text(
                            "Could not load feed", color = PhantasiaColors.OnSurface,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.error ?: "", color = PhantasiaColors.Error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { vm.loadFeed() },
                            colors = ButtonDefaults.buttonColors(containerColor = PhantasiaColors.Primary)
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Retry")
                        }
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    // ── Header + inline search ─────────────────────────────────
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(PhantasiaColors.GradTop, Color.Transparent)
                                    )
                                )
                                .padding(horizontal = 20.dp)
                                .padding(top = 52.dp, bottom = 8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Phantasia",
                                            style = MaterialTheme.typography.headlineLarge,
                                            color = PhantasiaColors.Primary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Text(
                                            "Music for every moment",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = PhantasiaColors.OnDim
                                        )
                                    }
                                    // Search button
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.1f))
                                            .clickable { showSearch = !showSearch },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = PhantasiaColors.OnSurface,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Inline search bar
                                AnimatedVisibility(
                                    showSearch,
                                    enter = expandVertically() + fadeIn(tween(250)),
                                    exit = shrinkVertically() + fadeOut(tween(200))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.White.copy(alpha = 0.1f))
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Search, null,
                                            tint = PhantasiaColors.OnDim, modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(Modifier.width(10.dp))
                                        BasicTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            textStyle = MaterialTheme.typography.bodyMedium
                                                .copy(color = PhantasiaColors.OnSurface),
                                            cursorBrush = SolidColor(PhantasiaColors.Primary),
                                            singleLine = true,
                                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                            keyboardActions = KeyboardActions(onSearch = {
                                                if (searchQuery.isNotBlank()) {
                                                    val queryToSearch = searchQuery
                                                    showSearch = false
                                                    nav.navigate(Route.Search.build(queryToSearch)) {
                                                        launchSingleTop = true
                                                    }
                                                }
                                            }),
                                            modifier = Modifier.weight(1f),
                                            decorationBox = { inner ->
                                                if (searchQuery.isEmpty()) {
                                                    Text(
                                                        "Search songs, artists…",
                                                        color = PhantasiaColors.OnHint,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                                inner()
                                            }
                                        )
                                        if (searchQuery.isNotEmpty()) {
                                            TextButton(onClick = {
                                                val queryToSearch = searchQuery
                                                showSearch = false
                                                nav.navigate(Route.Search.build(queryToSearch)) {
                                                    launchSingleTop = true
                                                }
                                            }) {
                                                Text("Go", color = PhantasiaColors.Primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Genre & Mood chips ─────────────────────────────────────
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            items(GENRES) { genre ->
                                val isSelected = state.selectedGenre.equals(genre.label, ignoreCase = true)
                                val bgColor by animateColorAsState(
                                    targetValue = if (isSelected) PhantasiaColors.Primary
                                    else Color.White.copy(alpha = 0.08f),
                                    animationSpec = tween(250), label = "genre_chip"
                                )
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(bgColor)
                                        .clickable { vm.selectGenre(genre.label) }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(genre.emoji, style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        genre.label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) Color.White else PhantasiaColors.OnDim,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // ── Dynamic Genre Content (When Genre Filter is Active) ────
                    if (state.selectedGenre != "All") {
                        if (state.isGenreLoading) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = PhantasiaColors.Primary)
                                }
                            }
                        } else if (state.genreTracks.isNotEmpty()) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            "${state.selectedGenre} Radio",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = PhantasiaColors.OnSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Curated for you",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = PhantasiaColors.OnDim
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(PhantasiaColors.Primary)
                                            .clickable {
                                                val firstTrack = state.genreTracks.first()
                                                playerVm.onEvent(PlayerUiEvent.PlayTrack(firstTrack, state.genreTracks))
                                                nav.navigate(Route.Player.build(firstTrack.videoId))
                                            }
                                            .padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                        Text("Play mix", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            item {
                                LazyRow(
                                    contentPadding = PaddingValues(horizontal = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    items(state.genreTracks) { track ->
                                        HomePickCard(track) {
                                            playerVm.onEvent(PlayerUiEvent.PlayTrack(track, state.genreTracks))
                                            nav.navigate(Route.Player.build(track.videoId))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Quick picks ────────────────────────────────────────────
                    if (state.quickPicks.isNotEmpty()) {
                        item {
                            HomeSectionTitle("Quick picks", "Start radio based on these") { vm.refresh() }
                        }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                items(state.quickPicks) { track ->
                                    HomePickCard(track) {
                                        playerVm.onEvent(PlayerUiEvent.PlayTrack(track, state.quickPicks))
                                        nav.navigate(Route.Player.build(track.videoId))
                                    }
                                }
                            }
                        }
                    }

                    // ── Recently played ────────────────────────────────────────
                    if (state.recentlyPlayed.isNotEmpty()) {
                        item {
                            HomeSectionTitle("Listen again", "Songs from your history") {}
                        }
                        items(state.recentlyPlayed.take(4)) { track ->
                            HomeTrackRow(track) {
                                playerVm.onEvent(PlayerUiEvent.PlayTrack(track, state.recentlyPlayed))
                                nav.navigate(Route.Player.build(track.videoId))
                            }
                        }
                    }

                    // ── YTM home sections (carousels) ──────────────────────────
                    state.homeSections.forEach { section ->
                        item { HomeSectionTitle(title = section.title, subtitle = section.subtitle) }
                        item {
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.padding(bottom = 18.dp)
                            ) {
                                items(section.items) { track ->
                                    HomePickCard(track) {
                                        playerVm.onEvent(PlayerUiEvent.PlayTrack(track, section.items))
                                        nav.navigate(Route.Player.build(track.videoId))
                                    }
                                }
                            }
                        }
                    }
                }

                // Refresh progress bar at top
                if (state.isRefreshing) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = PhantasiaColors.Primary,
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSectionTitle(title: String, subtitle: String? = null, onRefresh: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column(modifier = Modifier.weight(1f)) {
            if (subtitle != null) {
                Text(
                    subtitle.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.4.sp),
                    color = PhantasiaColors.PrimaryGlow,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(3.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                color = PhantasiaColors.OnBg,
                fontWeight = FontWeight.ExtraBold
            )
        }
        if (title == "Quick picks") {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PhantasiaColors.Primary.copy(alpha = 0.15f),
                modifier = Modifier.clickable { onRefresh() }
            ) {
                Text(
                    "Refresh",
                    style = MaterialTheme.typography.labelMedium,
                    color = PhantasiaColors.Primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun HomePickCard(track: TrackModel, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = PhantasiaColors.Surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PhantasiaColors.SurfaceCard)
            ) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                // Gradient overlay
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                )
                // Play button overlay with neon aura
                Box(
                    Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(PhantasiaColors.Primary),
                    Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                track.title,
                style = MaterialTheme.typography.bodyMedium,
                color = PhantasiaColors.OnBg,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (track.artistName.isNotEmpty()) {
                Spacer(Modifier.height(3.dp))
                Text(
                    track.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = PhantasiaColors.OnDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HomeTrackRow(track: TrackModel, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PhantasiaColors.SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(PhantasiaColors.SurfaceHigh)
            ) {
                AsyncImage(
                    model = track.artworkUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    track.title,
                    color = PhantasiaColors.OnBg,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    track.artistName,
                    color = PhantasiaColors.OnDim,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Default.MoreVert,
                contentDescription = "More",
                tint = PhantasiaColors.OnHint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
