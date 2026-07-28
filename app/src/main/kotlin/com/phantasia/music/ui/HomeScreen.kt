package com.phantasia.music.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phantasia.music.Route
import com.phantasia.music.network.HomeSection
import com.phantasia.music.network.TrackModel

// ── Genre chips (Velune-style) ────────────────────────────────────────────────
private data class Genre(val label: String, val emoji: String, val searchQuery: String)
private val GENRES = listOf(
    Genre("Pop",       "🎵", "pop music 2024"),
    Genre("Rock",      "🎸", "rock music"),
    Genre("Hip Hop",   "🎤", "hip hop rap"),
    Genre("Electronic","🎧", "electronic dance music"),
    Genre("Romance",   "💜", "romantic songs"),
    Genre("Workout",   "💪", "workout gym music"),
    Genre("Chill",     "🌙", "chill lo-fi music"),
    Genre("Jazz",      "🎷", "jazz music"),
    Genre("Classical", "🎻", "classical music"),
    Genre("Indie",     "🎹", "indie music"),
    Genre("K-Pop",     "✨", "kpop"),
    Genre("Bollywood", "🌟", "bollywood hits"),
)

@Composable
fun HomeScreen(nav: NavController) {
    val vm: HomeViewModel = hiltViewModel()
    val state by vm.state.collectAsState()

    var showSearch    by remember { mutableStateOf(false) }
    var searchQuery   by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf<String?>(null) }
    val searchVm: SearchViewModel = hiltViewModel()

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(
            PhantasiaColors.GradTop, PhantasiaColors.GradMid, PhantasiaColors.GradBot
        ))
    )) {

        // ── Pull-to-refresh gesture ────────────────────────────────────────────
        when {
            state.isLoading -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PhantasiaColors.Primary)
                        Spacer(Modifier.height(12.dp))
                        Text("Loading your feed…", color = PhantasiaColors.OnDim,
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            state.error != null && state.homeSections.isEmpty() -> {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)) {
                        Text("Could not load", color = PhantasiaColors.OnSurface,
                            style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(state.error ?: "", color = PhantasiaColors.Error,
                            style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(20.dp))
                        Button(onClick = { vm.loadFeed() },
                            colors = ButtonDefaults.buttonColors(containerColor = PhantasiaColors.Primary)) {
                            Icon(Icons.Default.Refresh, null)
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
                    // ── Header + inline search ─────────────────────────────────
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .background(Brush.verticalGradient(
                                    listOf(PhantasiaColors.GradTop, Color.Transparent)))
                                .padding(horizontal = 20.dp).padding(top = 60.dp, bottom = 8.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Phantasia",
                                            style      = MaterialTheme.typography.headlineLarge,
                                            color      = PhantasiaColors.Primary,
                                            fontWeight = FontWeight.ExtraBold)
                                        Text("What do you want to hear?",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = PhantasiaColors.OnDim)
                                    }
                                    // Search button
                                    Box(modifier = Modifier.size(40.dp).clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f))
                                        .clickable { showSearch = !showSearch },
                                        contentAlignment = Alignment.Center) {
                                        Icon(
                                            if (showSearch) Icons.Default.Close else Icons.Default.Search,
                                            null, tint = PhantasiaColors.OnSurface,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                // Inline search bar — animates in
                                AnimatedVisibility(showSearch,
                                    enter = expandVertically() + fadeIn(tween(250)),
                                    exit  = shrinkVertically() + fadeOut(tween(200))
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                                            .clip(RoundedCornerShape(50))
                                            .background(Color.White.copy(alpha = 0.1f))
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Search, null,
                                            tint = PhantasiaColors.OnDim, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(10.dp))
                                        BasicTextField(
                                            value          = searchQuery,
                                            onValueChange  = { searchQuery = it },
                                            textStyle      = MaterialTheme.typography.bodyMedium
                                                .copy(color = PhantasiaColors.OnSurface),
                                            cursorBrush   = SolidColor(PhantasiaColors.Primary),
                                            singleLine     = true,
                                            modifier       = Modifier.weight(1f),
                                            decorationBox  = { inner ->
                                                if (searchQuery.isEmpty()) Text("Search songs…",
                                                    color = PhantasiaColors.OnHint,
                                                    style = MaterialTheme.typography.bodyMedium)
                                                inner()
                                            }
                                        )
                                        if (searchQuery.isNotEmpty()) {
                                            TextButton(onClick = {
                                                showSearch = false
                                                searchVm.onEvent(SearchUiEvent.QueryChanged(searchQuery))
                                                nav.navigate(Route.Search.path) {
                                                    launchSingleTop = true
                                                }
                                            }) { Text("Go", color = PhantasiaColors.Primary) }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Genre chips ────────────────────────────────────────────
                    item {
                        Text("Genres", style = MaterialTheme.typography.titleSmall,
                            color = PhantasiaColors.OnSurface, fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp))
                        LazyRow(
                            contentPadding        = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier              = Modifier.padding(bottom = 8.dp)
                        ) {
                            items(GENRES) { genre ->
                                val isSelected = selectedGenre == genre.label
                                val bgColor by animateColorAsState(
                                    targetValue = if (isSelected) PhantasiaColors.Primary
                                                  else Color.White.copy(alpha = 0.08f),
                                    animationSpec = tween(250), label = "genre_chip"
                                )
                                Row(
                                    modifier = Modifier.clip(RoundedCornerShape(50))
                                        .background(bgColor)
                                        .clickable {
                                            selectedGenre = if (isSelected) null else genre.label
                                            if (!isSelected) {
                                                searchVm.onEvent(SearchUiEvent.QueryChanged(genre.searchQuery))
                                                nav.navigate(Route.Search.path) { launchSingleTop = true }
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(genre.emoji, style = MaterialTheme.typography.bodySmall)
                                    Text(genre.label,
                                        style    = MaterialTheme.typography.labelMedium,
                                        color    = if (isSelected) Color.White else PhantasiaColors.OnDim,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal)
                                }
                            }
                        }
                    }

                    // ── Quick picks ────────────────────────────────────────────
                    if (state.quickPicks.isNotEmpty()) {
                        item {
                            HomeSectionTitle("Quick picks") { vm.refresh() }
                        }
                        item {
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 8.dp)) {
                                items(state.quickPicks) { track ->
                                    HomePickCard(track) {
                                        nav.navigate(Route.Player.build(track.videoId))
                                    }
                                }
                            }
                        }
                    }

                    // ── Recently played ────────────────────────────────────────
                    if (state.recentlyPlayed.isNotEmpty()) {
                        item {
                            HomeSectionTitle("Recently played") {}
                        }
                        items(state.recentlyPlayed.take(5)) { track ->
                            HomeTrackRow(track) { nav.navigate(Route.Player.build(track.videoId)) }
                        }
                    }

                    // ── YTM home sections (carousels) ──────────────────────────
                    state.homeSections.forEach { section ->
                        item { HomeSectionTitle(section.title) {} }
                        item {
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 8.dp)) {
                                items(section.items) { track ->
                                    HomePickCard(track) {
                                        nav.navigate(Route.Player.build(track.videoId))
                                    }
                                }
                            }
                        }
                    }

                    // ── Empty state ────────────────────────────────────────────
                    if (state.homeSections.isEmpty() && state.recentlyPlayed.isEmpty() && !state.isLoading) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(top = 48.dp), Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)) {
                                    Text("🎵", style = MaterialTheme.typography.displaySmall)
                                    Spacer(Modifier.height(12.dp))
                                    Text("Search for music to get started",
                                        color = PhantasiaColors.OnDim,
                                        style = MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.height(16.dp))
                                    Button(onClick = { showSearch = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = PhantasiaColors.Primary)) {
                                        Icon(Icons.Default.Search, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Search music")
                                    }
                                }
                            }
                        }
                    }
                }

                // Refresh progress bar at top
                if (state.isRefreshing) {
                    LinearProgressIndicator(
                        modifier   = Modifier.fillMaxWidth(),
                        color      = PhantasiaColors.Primary,
                        trackColor = Color.Transparent
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeSectionTitle(title: String, onRefresh: () -> Unit = {}) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleMedium,
            color = PhantasiaColors.OnSurface, fontWeight = FontWeight.SemiBold)
        if (title == "Quick picks") {
            Text("Refresh", style = MaterialTheme.typography.labelMedium,
                color = PhantasiaColors.Primary,
                modifier = Modifier.clickable { onRefresh() })
        }
    }
}

@Composable
private fun HomePickCard(track: TrackModel, onClick: () -> Unit) {
    Column(modifier = Modifier.width(130.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(130.dp).clip(RoundedCornerShape(14.dp))
            .background(PhantasiaColors.SurfaceCard)) {
            AsyncImage(model = track.artworkUrl, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            // Gradient overlay
            Box(Modifier.fillMaxWidth().height(48.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(
                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)))))
            // Play button
            Box(Modifier.align(Alignment.BottomEnd).padding(6.dp).size(30.dp).clip(CircleShape)
                .background(PhantasiaColors.Primary), Alignment.Center) {
                Icon(Icons.Default.PlayArrow, null, tint = Color.White,
                    modifier = Modifier.size(18.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(track.title, style = MaterialTheme.typography.labelMedium,
            color = PhantasiaColors.OnSurface, maxLines = 2,
            overflow = TextOverflow.Ellipsis)
        if (track.artistName.isNotEmpty()) {
            Text(track.artistName, style = MaterialTheme.typography.labelSmall,
                color = PhantasiaColors.OnDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun HomeTrackRow(track: TrackModel, onClick: () -> Unit) {
    ListItem(
        headlineContent   = { Text(track.title, color = PhantasiaColors.OnSurface,
            maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text(track.artistName, color = PhantasiaColors.OnDim, maxLines = 1) },
        leadingContent    = {
            Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp))
                .background(PhantasiaColors.SurfaceCard)) {
                AsyncImage(model = track.artworkUrl, contentDescription = null,
                    contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            }
        },
        colors   = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}
