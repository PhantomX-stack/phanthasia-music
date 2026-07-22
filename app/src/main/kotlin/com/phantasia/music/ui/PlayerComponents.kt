package com.phantasia.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.phantasia.music.network.TrackModel

/**
 * Mini player bar — shown at bottom of Home/Search/Library
 * when something is playing. Pass into Scaffold bottomBar if needed.
 */
@Composable
fun MiniPlayerBar(
    state:   PlayerUiState.Playing,
    onEvent: (PlayerUiEvent) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier      = Modifier.fillMaxWidth(),
        color         = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Artwork
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                AsyncImage(
                    model              = state.track.artworkUrl,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            }

            Spacer(Modifier.width(12.dp))

            // Track info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text     = state.track.title,
                    style    = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text  = state.track.artistName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Play/Pause
            IconButton(onClick = {
                if (state.isPlaying) onEvent(PlayerUiEvent.Pause)
                else onEvent(PlayerUiEvent.Play)
            }) {
                Icon(
                    imageVector  = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint         = MaterialTheme.colorScheme.onSurface
                )
            }

            // Next
            IconButton(onClick = { onEvent(PlayerUiEvent.SkipNext) }) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onSurface)
            }
        }

        // Progress line at bottom
        LinearProgressIndicator(
            progress  = { (state.positionMs.toFloat() / state.durationMs.toFloat().coerceAtLeast(1f)) },
            modifier  = Modifier.fillMaxWidth().height(2.dp),
            color     = MaterialTheme.colorScheme.primary,
            trackColor = Color.Transparent
        )
    }
}

/**
 * Track card used in search results and home screen.
 */
@Composable
fun TrackCard(track: TrackModel, onClick: () -> Unit) {
    ListItem(
        headlineContent   = { Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(track.artistName, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model              = track.artworkUrl,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            }
        },
        colors   = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}
