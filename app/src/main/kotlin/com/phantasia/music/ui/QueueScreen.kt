package com.phantasia.music.ui

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.*
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
import com.phantasia.music.network.TrackModel
import com.phantasia.music.player.RepeatMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QueueScreen(nav: NavController) {
    val vm: PlayerViewModel = hiltViewModel()
    val uiState by vm.uiState.collectAsState()
    val queueState by vm.queueState.collectAsState()

    val currentPlayingTrack = (uiState as? PlayerUiState.Playing)?.track ?: queueState.currentTrack
    val isPlaying = (uiState as? PlayerUiState.Playing)?.isPlaying == true

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Play Queue",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "${queueState.tracks.size} tracks • ${queueState.remainingCount} up next",
                            style = MaterialTheme.typography.labelMedium,
                            color = PhantasiaColors.OnDim
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { nav.navigateUp() },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { vm.onEvent(PlayerUiEvent.ToggleShuffle) }) {
                        Icon(
                            Icons.Default.Shuffle,
                            contentDescription = "Shuffle",
                            tint = if (queueState.shuffleEnabled) PhantasiaColors.Primary else PhantasiaColors.OnDim
                        )
                    }
                    IconButton(onClick = { vm.onEvent(PlayerUiEvent.CycleRepeat) }) {
                        Icon(
                            imageVector = when (queueState.repeatMode) {
                                RepeatMode.ONE -> Icons.Default.RepeatOne
                                else -> Icons.Default.Repeat
                            },
                            contentDescription = "Repeat",
                            tint = if (queueState.repeatMode != RepeatMode.NONE) PhantasiaColors.Primary else PhantasiaColors.OnDim
                        )
                    }
                    if (queueState.tracks.size > 1) {
                        IconButton(onClick = { vm.onEvent(PlayerUiEvent.ClearQueue) }) {
                            Icon(
                                Icons.Default.ClearAll,
                                contentDescription = "Clear Queue",
                                tint = PhantasiaColors.OnDim
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PhantasiaColors.Midnight
                )
            )
        },
        containerColor = PhantasiaColors.Midnight
    ) { padding ->
        if (queueState.tracks.isEmpty() && currentPlayingTrack == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.QueueMusic,
                        contentDescription = null,
                        tint = PhantasiaColors.OnDim,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Your queue is empty",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Play any song from Home or Search to begin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PhantasiaColors.OnDim
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // ── NOW PLAYING SECTION ──────────────────────────────────
                if (currentPlayingTrack != null) {
                    item {
                        Text(
                            "NOW PLAYING",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = PhantasiaColors.Primary,
                            modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                        )
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = PhantasiaColors.SurfaceHigh,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                ) {
                                    AsyncImage(
                                        model = currentPlayingTrack.artworkUrl,
                                        contentDescription = currentPlayingTrack.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Animated Playing indicator badge
                                    if (isPlaying) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color.Black.copy(alpha = 0.35f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                                verticalAlignment = Alignment.Bottom,
                                                modifier = Modifier.height(20.dp)
                                            ) {
                                                EqualizerBar(minHeight = 4.dp, maxHeight = 18.dp, durationMs = 450)
                                                EqualizerBar(minHeight = 8.dp, maxHeight = 20.dp, durationMs = 320)
                                                EqualizerBar(minHeight = 6.dp, maxHeight = 16.dp, durationMs = 520)
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentPlayingTrack.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(Modifier.height(3.dp))
                                    Text(
                                        text = currentPlayingTrack.artistName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PhantasiaColors.OnDim,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (isPlaying) vm.onEvent(PlayerUiEvent.Pause)
                                        else vm.onEvent(PlayerUiEvent.Play)
                                    },
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(PhantasiaColors.Primary)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Play",
                                        tint = Color.Black,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "UP NEXT",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = PhantasiaColors.OnDim,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                            if (queueState.tracks.size > 1) {
                                Text(
                                    "${queueState.tracks.size} tracks in queue",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PhantasiaColors.OnDim
                                )
                            }
                        }
                    }
                }

                // ── UP NEXT TRACKS ───────────────────────────────────────
                itemsIndexed(queueState.tracks) { index, track ->
                    val isCurrent = index == queueState.currentIndex
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isCurrent) PhantasiaColors.Primary.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.04f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                vm.onEvent(PlayerUiEvent.PlayQueueIndex(index))
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Queue index number or equalizer
                            Box(
                                modifier = Modifier.width(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCurrent) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "Playing",
                                        tint = PhantasiaColors.Primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else {
                                    Text(
                                        "${index + 1}",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = PhantasiaColors.OnDim
                                    )
                                }
                            }

                            Spacer(Modifier.width(8.dp))

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
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

                            Spacer(Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = track.title,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCurrent) PhantasiaColors.Primary else Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = track.artistName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PhantasiaColors.OnDim,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Remove item from queue button
                            IconButton(
                                onClick = {
                                    vm.onEvent(PlayerUiEvent.RemoveQueueIndex(index))
                                },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove from Queue",
                                    tint = PhantasiaColors.OnDim.copy(alpha = 0.6f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Endless Radio Station indicator footer
                item {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = PhantasiaColors.Primary.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PhantasiaColors.Primary.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = PhantasiaColors.Primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Endless Radio Mode Active",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = PhantasiaColors.PrimaryGlow
                                )
                                Text(
                                    "New matching tracks are automatically queued in real-time as you listen",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PhantasiaColors.OnDim
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun EqualizerBar(minHeight: androidx.compose.ui.unit.Dp, maxHeight: androidx.compose.ui.unit.Dp, durationMs: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "eq")
    val height by infiniteTransition.animateValue(
        initialValue = minHeight,
        targetValue = maxHeight,
        typeConverter = androidx.compose.ui.unit.Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "eq_bar"
    )
    Box(
        modifier = Modifier
            .width(3.5.dp)
            .height(height)
            .clip(RoundedCornerShape(2.dp))
            .background(PhantasiaColors.Primary)
    )
}

