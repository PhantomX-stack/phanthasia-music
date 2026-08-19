package com.phantasia.music.ui

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
import com.phantasia.music.Route
import com.phantasia.music.network.TrackModel
import com.phantasia.music.player.QueueManager
import com.phantasia.music.storage.PlaylistDao
import com.phantasia.music.storage.PlaylistSongCrossRef
import com.phantasia.music.storage.SongDao
import com.phantasia.music.storage.SongEntity
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportedPlaylistScreen(id: String, name: String, nav: NavController) {
    val libraryVm: LibraryViewModel = hiltViewModel()
    val playerVm: PlayerViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    val playlistIdLong = id.toLongOrNull()
    val allPlaylists by libraryVm.playlists.collectAsState(initial = emptyList())
    val playlist = allPlaylists.find { it.playlist.playlistId == playlistIdLong || it.playlist.name == name }

    val songs = playlist?.songs ?: emptyList()
    var showDeleteConfirm by remember { mutableStateOf(false) }

    fun playTracks(startIndex: Int = 0, shuffle: Boolean = false) {
        if (songs.isEmpty()) return
        val trackModels = songs.map {
            TrackModel(
                videoId = it.videoId,
                title = it.title,
                artistName = it.artistName,
                albumTitle = it.albumTitle,
                artworkUrl = it.artworkUrl,
                durationSeconds = it.durationSeconds
            )
        }
        val targetTracks = if (shuffle) trackModels.shuffled() else trackModels
        val targetIndex = if (shuffle) 0 else startIndex

        playerVm.onEvent(PlayerUiEvent.PlayTrack(targetTracks[targetIndex]))
        nav.navigate(Route.Player.build(targetTracks[targetIndex].videoId))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(name, color = PhantasiaColors.OnSurface, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PhantasiaColors.OnSurface)
                    }
                },
                actions = {
                    if (playlist != null) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete playlist", tint = PhantasiaColors.OnDim)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PhantasiaColors.Midnight)
            )
        },
        containerColor = PhantasiaColors.Midnight
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(PhantasiaColors.Midnight, PhantasiaColors.GradMid, PhantasiaColors.GradBot)
                    )
                ),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(PhantasiaColors.Primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (songs.isNotEmpty() && songs.first().artworkUrl.isNotBlank()) {
                            AsyncImage(
                                model = songs.first().artworkUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.QueueMusic,
                                contentDescription = null,
                                tint = PhantasiaColors.Primary,
                                modifier = Modifier.size(64.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(
                        name,
                        style = MaterialTheme.typography.headlineSmall,
                        color = PhantasiaColors.OnSurface,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${songs.size} songs",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PhantasiaColors.OnDim
                    )

                    Spacer(Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { playTracks(startIndex = 0, shuffle = false) },
                            enabled = songs.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.buttonColors(containerColor = PhantasiaColors.Primary)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Play all")
                        }

                        OutlinedButton(
                            onClick = { playTracks(startIndex = 0, shuffle = true) },
                            enabled = songs.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(50),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PhantasiaColors.Primary)
                        ) {
                            Icon(Icons.Default.Shuffle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Shuffle")
                        }
                    }
                }
            }

            if (songs.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No songs in this playlist", color = PhantasiaColors.OnDim, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(8.dp))
                            Text("Use search or import to add songs", color = PhantasiaColors.OnHint, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                itemsIndexed(songs) { index, song ->
                    ListItem(
                        headlineContent = {
                            Text(
                                song.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = PhantasiaColors.OnSurface,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supportingContent = {
                            Text(
                                song.artistName,
                                maxLines = 1,
                                color = PhantasiaColors.OnDim,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PhantasiaColors.SurfaceCard)
                            ) {
                                AsyncImage(
                                    model = song.artworkUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        },
                        trailingContent = {
                            IconButton(
                                onClick = {
                                    if (playlistIdLong != null) {
                                        libraryVm.removeFromPlaylist(playlistIdLong, song.videoId)
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove song", tint = PhantasiaColors.OnHint, modifier = Modifier.size(16.dp))
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        modifier = Modifier.clickable { playTracks(startIndex = index, shuffle = false) }
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = PhantasiaColors.SurfaceHigh,
            title = { Text("Delete playlist?", color = PhantasiaColors.OnSurface) },
            text = { Text("Are you sure you want to remove \"$name\"?", color = PhantasiaColors.OnDim) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        if (playlist != null) {
                            scope.launch {
                                libraryVm.removeFromPlaylist(playlist.playlist.playlistId, "")
                            }
                        }
                        nav.navigateUp()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = PhantasiaColors.Error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = PhantasiaColors.Primary) }
            }
        )
    }
}
