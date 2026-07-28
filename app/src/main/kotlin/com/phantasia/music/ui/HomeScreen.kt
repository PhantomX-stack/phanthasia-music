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
import androidx.compose.ui.text.font.FontWeight
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

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(
            PhantasiaColors.GradTop, PhantasiaColors.GradMid, PhantasiaColors.GradBot
        ))
    )) {
        when {
            state.isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PhantasiaColors.Primary)
                    Spacer(Modifier.height(12.dp))
                    Text("Loading your feed…", color = PhantasiaColors.OnDim,
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            state.error != null && state.homeSections.isEmpty() -> Box(
                Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)) {
                    Text("Could not load", color = PhantasiaColors.OnSurface,
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Text(state.error ?: "", color = PhantasiaColors.OnDim,
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
            else -> {
                LazyColumn(Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 120.dp)) {
                    // Header
                    item {
                        Box(Modifier.fillMaxWidth().height(200.dp).background(
                            Brush.verticalGradient(listOf(
                                PhantasiaColors.GradTop, Color.Transparent
                            ))
                        ).padding(horizontal = 24.dp).padding(top = 60.dp)) {
                            Column {
                                Text("Phantasia",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = PhantasiaColors.Primary,
                                    fontWeight = FontWeight.Bold)
                                Text("What do you want to hear?",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PhantasiaColors.OnDim)
                            }
                        }
                    }
                    // Quick picks
                    if (state.quickPicks.isNotEmpty()) {
                        item { SectionTitle("Quick picks") }
                        item {
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 8.dp)) {
                                items(state.quickPicks) { track ->
                                    QuickPickCard(track) {
                                        nav.navigate(Route.Player.build(track.videoId))
                                    }
                                }
                            }
                        }
                    }
                    // Recently played
                    if (state.recentlyPlayed.isNotEmpty()) {
                        item { SectionTitle("Recently played") }
                        items(state.recentlyPlayed.take(5)) { track ->
                            HomeTrackRow(track) { nav.navigate(Route.Player.build(track.videoId)) }
                        }
                    }
                    // YTM home carousels
                    state.homeSections.forEach { section ->
                        item { SectionTitle(section.title) }
                        item {
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 8.dp)) {
                                items(section.items) { track ->
                                    QuickPickCard(track) {
                                        nav.navigate(Route.Player.build(track.videoId))
                                    }
                                }
                            }
                        }
                    }
                    // Empty state
                    if (state.homeSections.isEmpty() && !state.isLoading) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(top = 32.dp), Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("🎵", style = MaterialTheme.typography.displayMedium)
                                    Spacer(Modifier.height(12.dp))
                                    Text("Search for music to get started",
                                        color = PhantasiaColors.OnDim,
                                        style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                }
                // Refresh bar at top
                if (state.isRefreshing) {
                    LinearProgressIndicator(Modifier.fillMaxWidth(),
                        color = PhantasiaColors.Primary,
                        trackColor = Color.Transparent)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium,
        color = PhantasiaColors.OnSurface, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp))
}

@Composable
private fun QuickPickCard(track: TrackModel, onClick: () -> Unit) {
    Column(modifier = Modifier.width(130.dp).clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(130.dp).clip(RoundedCornerShape(14.dp))
            .background(PhantasiaColors.SurfaceCard)) {
            AsyncImage(model = track.artworkUrl, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            Box(Modifier.fillMaxWidth().height(50.dp).align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xCC000000)))))
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
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}
