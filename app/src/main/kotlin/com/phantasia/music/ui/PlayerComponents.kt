package com.phantasia.music.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.phantasia.music.network.TrackModel
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Samsung "Now Bar" / One UI Floating Media Bar
 * Features:
 * - Floating pill capsule elevated with frosted glassmorphic gradient
 * - Previous, Play/Pause, and Next controls
 * - Swipe-down to dismiss the bar and stop playback
 * - Micro equalizer bars & glowing artwork ring
 * - Ultra-thin progress tracker
 */
@Composable
fun MiniPlayerBar(
    state: PlayerUiState.Playing,
    onEvent: (PlayerUiEvent) -> Unit,
    onClick: () -> Unit
) {
    var offsetY by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "mini_bar_drag"
    )

    val draggableState = rememberDraggableState { delta ->
        // Only allow dragging downwards (positive Y)
        offsetY = (offsetY + delta).coerceAtLeast(0f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .offset { IntOffset(0, animatedOffsetY.roundToInt()) }
            .draggable(
                state = draggableState,
                orientation = Orientation.Vertical,
                onDragStopped = { velocity ->
                    if (offsetY > 120f || velocity > 800f) {
                        // Dismiss and stop song
                        onEvent(PlayerUiEvent.Stop)
                    } else {
                        // Snap back
                        offsetY = 0f
                    }
                }
            )
    ) {
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = Color(0xEB121726),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        PhantasiaColors.Primary.copy(alpha = 0.5f),
                        PhantasiaColors.Secondary.copy(alpha = 0.4f),
                        Color.White.copy(alpha = 0.12f)
                    )
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(36.dp),
                    ambientColor = PhantasiaColors.Primary.copy(alpha = 0.3f),
                    spotColor = PhantasiaColors.Primary.copy(alpha = 0.45f)
                )
                .clickable { onClick() }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top drag hint pill
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(3.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Artwork with glowing border
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PhantasiaColors.SurfaceHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = state.track.artworkUrl,
                            contentDescription = state.track.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Dynamic micro-equalizer overlay when playing
                        if (state.isPlaying) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.Bottom,
                                    modifier = Modifier.height(14.dp)
                                ) {
                                    MiniEqBar(4.dp, 12.dp, 350)
                                    MiniEqBar(8.dp, 14.dp, 280)
                                    MiniEqBar(5.dp, 11.dp, 420)
                                }
                            }
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    // Track info
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 4.dp)
                    ) {
                        Text(
                            text = state.track.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            ),
                            color = PhantasiaColors.OnBg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(1.dp))
                        Text(
                            text = state.track.artistName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp
                            ),
                            color = PhantasiaColors.OnDim,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Samsung Media Controls: Previous, Play/Pause, Next, and Dismiss
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Previous Track Button
                        IconButton(
                            onClick = { onEvent(PlayerUiEvent.SkipPrev) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "Previous Track",
                                tint = PhantasiaColors.OnSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Play/Pause button with glowing circle
                        val playScale by animateFloatAsState(
                            targetValue = if (state.isPlaying) 1.05f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "mini_play_scale"
                        )
                        IconButton(
                            onClick = {
                                if (state.isPlaying) onEvent(PlayerUiEvent.Pause)
                                else onEvent(PlayerUiEvent.Play)
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(PhantasiaColors.Primary)
                                .graphicsLayer {
                                    scaleX = playScale
                                    scaleY = playScale
                                }
                        ) {
                            Crossfade(targetState = state.isPlaying, label = "mini_play_crossfade") { isPlaying ->
                                Icon(
                                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Play",
                                    tint = Color.Black,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        // Next Track Button
                        IconButton(
                            onClick = { onEvent(PlayerUiEvent.SkipNext) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SkipNext,
                                contentDescription = "Next Track",
                                tint = PhantasiaColors.OnSurface,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Quick Dismiss / Stop
                        IconButton(
                            onClick = { onEvent(PlayerUiEvent.Stop) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Stop & Dismiss",
                                tint = PhantasiaColors.OnHint,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Ultra-thin neon progress indicator line at the bottom
                val progressFraction = remember(state.positionMs, state.durationMs) {
                    if (state.durationMs > 0) (state.positionMs.toFloat() / state.durationMs.toFloat()).coerceIn(0f, 1f)
                    else 0f
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .background(Color.White.copy(alpha = 0.08f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction = progressFraction)
                            .fillMaxHeight()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(PhantasiaColors.Primary, PhantasiaColors.Secondary)
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniEqBar(minHeight: androidx.compose.ui.unit.Dp, maxHeight: androidx.compose.ui.unit.Dp, durationMs: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_eq")
    val height by infiniteTransition.animateValue(
        initialValue = minHeight,
        targetValue = maxHeight,
        typeConverter = androidx.compose.ui.unit.Dp.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMs, easing = FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "mini_eq_bar"
    )
    Box(
        modifier = Modifier
            .width(2.5.dp)
            .height(height)
            .clip(RoundedCornerShape(1.5.dp))
            .background(PhantasiaColors.Primary)
    )
}

/**
 * Modern Glassmorphic Track card used across search results and home screens.
 */
@Composable
fun TrackCard(
    track: TrackModel,
    isPlaying: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isPlaying) PhantasiaColors.Primary.copy(alpha = 0.12f) else PhantasiaColors.SurfaceCard,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPlaying) PhantasiaColors.Primary.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
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
                    contentDescription = track.title,
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
                        contentDescription = "Play",
                        tint = if (isPlaying) PhantasiaColors.Primary else Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isPlaying) PhantasiaColors.Primary else PhantasiaColors.OnBg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = track.artistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = PhantasiaColors.OnDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (track.durationSeconds > 0) {
                        Text(
                            text = " • ${track.durationSeconds / 60}:${String.format("%02d", track.durationSeconds % 60)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PhantasiaColors.OnHint
                        )
                    }
                }
            }
        }
    }
}
