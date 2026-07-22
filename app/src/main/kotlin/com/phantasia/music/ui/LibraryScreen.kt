package com.phantasia.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
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

@Composable
fun LibraryScreen(nav: NavController) {
    val vm: LibraryViewModel = hiltViewModel()
    val favs      by vm.favourites.collectAsState()
    val playlists by vm.playlists.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) }
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(top = 56.dp)
    ) {
        // ── Title ─────────────────────────────────────────────────────────────
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment   = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Your Library", style = MaterialTheme.typography.headlineSmall)
            if (selectedTab == 1) {
                IconButton(onClick = { showNewPlaylistDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "New playlist",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }
        }

        // ── Tabs ──────────────────────────────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor   = Color.Transparent,
            contentColor     = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick  = { selectedTab = 0 },
                text     = { Text("Liked songs") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick  = { selectedTab = 1 },
                text     = { Text("Playlists") }
            )
        }

        Spacer(Modifier.height(8.dp))

        when (selectedTab) {
            // ── Liked songs ───────────────────────────────────────────────────
            0 -> {
                if (favs.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Songs you like will appear here",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Text(
                        "${favs.size} songs",
                        style    = MaterialTheme.typography.labelMedium,
                        color    = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp)
                    )
                    LazyColumn {
                        items(favs.size) { i ->
                            val song = favs[i]
                            ListItem(
                                headlineContent   = {
                                    Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                },
                                supportingContent = {
                                    Text(song.artistName, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                },
                                leadingContent = {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        AsyncImage(
                                            model        = song.artworkUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier     = Modifier.fillMaxSize()
                                        )
                                    }
                                },
                                trailingContent = {
                                    IconButton(onClick = { vm.toggleFavourite(song) }) {
                                        Icon(
                                            Icons.Default.Favorite,
                                            contentDescription = "Unlike",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                colors   = ListItemDefaults.colors(containerColor = Color.Transparent),
                                modifier = Modifier.clickable {
                                    nav.navigate(Route.Player.build(song.videoId))
                                }
                            )
                            if (i < favs.size - 1) {
                                Divider(
                                    modifier = Modifier.padding(start = 84.dp, end = 16.dp),
                                    color    = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }

            // ── Playlists ─────────────────────────────────────────────────────
            1 -> {
                if (playlists.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎧", style = MaterialTheme.typography.displaySmall)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Create your first playlist",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { showNewPlaylistDialog = true }) {
                                Text("New playlist")
                            }
                        }
                    }
                } else {
                    LazyColumn {
                        items(playlists.size) { i ->
                            val pl = playlists[i]
                            ListItem(
                                headlineContent   = { Text(pl.playlist.name) },
                                supportingContent = { Text("${pl.songs.size} songs") },
                                leadingContent    = {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🎵", style = MaterialTheme.typography.titleMedium)
                                    }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }
            }
        }
    }

    // ── New playlist dialog ────────────────────────────────────────────────────
    if (showNewPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showNewPlaylistDialog = false; newPlaylistName = "" },
            title            = { Text("New playlist") },
            text             = {
                OutlinedTextField(
                    value         = newPlaylistName,
                    onValueChange = { newPlaylistName = it },
                    label         = { Text("Playlist name") },
                    singleLine    = true,
                    modifier      = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick  = {
                        if (newPlaylistName.isNotBlank()) {
                            vm.createPlaylist(newPlaylistName)
                            showNewPlaylistDialog = false
                            newPlaylistName = ""
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showNewPlaylistDialog = false; newPlaylistName = "" }) {
                    Text("Cancel")
                }
            }
        )
    }
}
