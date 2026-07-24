package com.phantasia.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phantasia.music.Route
import com.phantasia.music.network.TrackModel
import com.phantasia.music.player.RepeatMode

@Composable
fun PlayerScreen(videoId: String, nav: NavController) {
    val vm: PlayerViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()

    LaunchedEffect(videoId) {
        vm.onEvent(PlayerUiEvent.PlayTrack(TrackModel(videoId, "Loading…", "", "", "", 0)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (val s = state) {
            is PlayerUiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Ready", style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            is PlayerUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is PlayerUiState.Playing -> PlayerUI(state = s, onEvent = vm::onEvent, nav = nav)
        }
    }
}

@Composable
private fun PlayerUI(
    state:   PlayerUiState.Playing,
    onEvent: (PlayerUiEvent) -> Unit,
    nav:     NavController
) {
    Box(Modifier.fillMaxSize()) {

        // ── Blurred gradient background from artwork ───────────────────────────
        DynamicBackground(artworkUrl = state.track.artworkUrl)

        // ── Dark scrim so controls are always readable ─────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.3f),
                        0.5f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.85f)
                    )
                )
        )

        // ── Content ────────────────────────────────────────────────────────────
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // Top bar — back + options
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                IconButton(onClick = { nav.navigateUp() }) {
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Back",
                        tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Text(
                    "NOW PLAYING",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { nav.navigate(Route.Queue.path) }) {
                        Icon(
                            Icons.Default.QueueMusic,
                            contentDescription = "Queue",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    IconButton(onClick = { /* options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = Color.White)
                    }
                }
            }

            // Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model              = state.track.artworkUrl,
                    contentDescription = state.track.title,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            }

            // Track info + like
            Column {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text      = state.track.title,
                            style     = MaterialTheme.typography.headlineSmall,
                            color     = Color.White,
                            maxLines  = 1,
                            overflow  = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text  = state.track.artistName,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                    IconButton(onClick = { onEvent(PlayerUiEvent.ToggleFavourite) }) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint     = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Progress bar
                Slider(
                    value         = state.positionMs.toFloat(),
                    onValueChange = { onEvent(PlayerUiEvent.Seek(it.toLong())) },
                    valueRange    = 0f..state.durationMs.toFloat().coerceAtLeast(1f),
                    colors        = SliderDefaults.colors(
                        thumbColor        = Color.White,
                        activeTrackColor  = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                    )
                )
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(formatMs(state.positionMs), style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f))
                    Text(formatMs(state.durationMs), style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f))
                }

                Spacer(Modifier.height(16.dp))

                // Controls
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    // Shuffle
                    IconButton(onClick = { onEvent(PlayerUiEvent.ToggleShuffle) }) {
                        Icon(
                            Icons.Filled.Shuffle,
                            contentDescription = "Shuffle",
                            tint     = if (state.shuffleEnabled) MaterialTheme.colorScheme.primary
                                       else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Previous
                    IconButton(
                        onClick  = { onEvent(PlayerUiEvent.SkipPrev) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous",
                            tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    // Play / Pause
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick  = {
                                if (state.isPlaying) onEvent(PlayerUiEvent.Pause)
                                else onEvent(PlayerUiEvent.Play)
                            }
                        ) {
                            Icon(
                                imageVector  = if (state.isPlaying) Icons.Filled.Pause
                                               else Icons.Filled.PlayArrow,
                                contentDescription = if (state.isPlaying) "Pause" else "Play",
                                tint     = Color.Black,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    // Next
                    IconButton(
                        onClick  = { onEvent(PlayerUiEvent.SkipNext) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(Icons.Filled.SkipNext, contentDescription = "Next",
                            tint = Color.White, modifier = Modifier.size(36.dp))
                    }

                    // Repeat
                    IconButton(onClick = { onEvent(PlayerUiEvent.CycleRepeat) }) {
                        Icon(
                            imageVector  = when (state.repeatMode) {
                                RepeatMode.ONE -> Icons.Filled.RepeatOne
                                else           -> Icons.Filled.Repeat
                            },
                            contentDescription = "Repeat",
                            tint     = if (state.repeatMode != RepeatMode.NONE)
                                           MaterialTheme.colorScheme.primary
                                       else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val totalSec = ms / 1000
    val min      = totalSec / 60
    val sec      = totalSec % 60
    return "%d:%02d".format(min, sec)
}
