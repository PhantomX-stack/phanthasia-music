package com.phantasia.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phantasia.music.Route
import com.phantasia.music.network.SearchResultModel

@Composable
fun SearchScreen(nav: NavController) {
    val vm: SearchViewModel = hiltViewModel()
    val query by vm.query.collectAsState()
    val state by vm.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 56.dp)
    ) {
        // ── Search bar ────────────────────────────────────────────────────────
        TextField(
            value         = query,
            onValueChange = { vm.onEvent(SearchUiEvent.QueryChanged(it)) },
            placeholder   = { Text("Songs, artists, albums…") },
            leadingIcon   = {
                Icon(Icons.Default.Search, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            trailingIcon  = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { vm.onEvent(SearchUiEvent.QueryChanged("")) }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            singleLine    = true,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(50)),
            colors        = TextFieldDefaults.colors(
                focusedContainerColor   = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor   = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor  = Color.Transparent
            )
        )

        Spacer(Modifier.height(16.dp))

        // ── Results ───────────────────────────────────────────────────────────
        when (val s = state) {
            is SearchUiState.Idle -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🔍", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Search for anything",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is SearchUiState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is SearchUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text  = s.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            is SearchUiState.Results -> {
                if (s.items.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No results for \"$query\"",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text     = "${s.items.size} results",
                        style    = MaterialTheme.typography.labelMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    LazyColumn {
                        items(s.items.size) { i ->
                            when (val item = s.items[i]) {
                                is SearchResultModel.TrackResult -> SearchTrackRow(
                                    track    = item.track,
                                    onClick  = { nav.navigate(Route.Player.build(item.track.videoId)) }
                                )
                                is SearchResultModel.AlbumResult -> SearchAlbumRow(
                                    album = item.album
                                )
                                is SearchResultModel.ArtistResult -> SearchArtistRow(
                                    artist = item.artist
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchTrackRow(
    track: com.phantasia.music.network.TrackModel,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent   = { Text(track.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(
                "${track.artistName} · Song",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
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

@Composable
private fun SearchAlbumRow(album: com.phantasia.music.network.AlbumModel) {
    ListItem(
        headlineContent   = { Text(album.title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = {
            Text(
                "${album.artistName} · Album · ${album.year}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                AsyncImage(
                    model              = album.thumbnailUrl,
                    contentDescription = null,
                    contentScale       = ContentScale.Crop,
                    modifier           = Modifier.fillMaxSize()
                )
            }
        },
        overlineContent  = { Text("Album", color = MaterialTheme.colorScheme.primary) },
        colors           = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
private fun SearchArtistRow(artist: com.phantasia.music.network.ArtistModel) {
    ListItem(
        headlineContent  = { Text(artist.name, maxLines = 1) },
        leadingContent   = {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                if (artist.thumbnailUrl.isNotEmpty()) {
                    AsyncImage(
                        model              = artist.thumbnailUrl,
                        contentDescription = null,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize()
                    )
                } else {
                    Text(
                        text     = artist.name.take(1).uppercase(),
                        modifier = Modifier.align(Alignment.Center),
                        style    = MaterialTheme.typography.titleLarge,
                        color    = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        },
        overlineContent  = { Text("Artist", color = MaterialTheme.colorScheme.primary) },
        colors           = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}
