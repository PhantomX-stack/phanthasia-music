package com.phantasia.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phantasia.music.Route

enum class ImportMode {
    YTM_PLAYLIST, YTM_LIKED, SPOTIFY_PLAYLIST, SPOTIFY_LIKED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(nav: NavController) {
    val vm: LibraryViewModel = hiltViewModel()
    val favs      by vm.favourites.collectAsState(initial = emptyList())
    val playlists by vm.playlists.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(0) }
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName       by remember { mutableStateOf("") }

    var showImportSheet  by remember { mutableStateOf(false) }
    var importUrl        by remember { mutableStateOf("") }
    var importMode       by remember { mutableStateOf<ImportMode?>(null) }
    val accountVm: AccountViewModel = hiltViewModel()
    val accountState by accountVm.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(PhantasiaColors.Midnight)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(top = 60.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Your Library",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PhantasiaColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { nav.navigate(Route.Settings.path) }) {
                    Icon(Icons.Default.Settings, null, tint = PhantasiaColors.OnSurface)
                }
            }

            // Tabs
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                containerColor   = Color.Transparent,
                contentColor     = PhantasiaColors.Primary,
                edgePadding      = 16.dp,
                divider          = {}
            ) {
                listOf("Liked songs", "Playlists", "Offline").forEachIndexed { index, title ->
                    val selected = selectedTab == index
                    Tab(
                        selected = selected,
                        onClick  = { selectedTab = index },
                        text     = {
                            Text(title,
                                color = if (selected) PhantasiaColors.OnSurface else PhantasiaColors.OnDim,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // Liked songs
                    if (favs.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Favorite, null,
                                    tint = PhantasiaColors.OnHint, modifier = Modifier.size(48.dp))
                                Spacer(Modifier.height(12.dp))
                                Text("Songs you like will appear here",
                                    color = PhantasiaColors.OnDim,
                                    style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                            items(favs.size) { i ->
                                val song = favs[i]
                                ListItem(
                                    headlineContent = { Text(song.title, maxLines = 1, overflow = TextOverflow.Ellipsis, color = PhantasiaColors.OnSurface) },
                                    supportingContent = { Text(song.artistName, maxLines = 1, color = PhantasiaColors.OnDim) },
                                    leadingContent = {
                                        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(PhantasiaColors.SurfaceCard)) {
                                            AsyncImage(model = song.artworkUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                        }
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { vm.toggleFavourite(song) }) {
                                            Icon(Icons.Default.Favorite, null, tint = PhantasiaColors.Primary)
                                        }
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                                    modifier = Modifier.clickable { nav.navigate(Route.Player.build(song.videoId)) }
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Playlists
                    if (playlists.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎧", style = MaterialTheme.typography.displaySmall)
                                Spacer(Modifier.height(12.dp))
                                Text("Create your first playlist",
                                    color = PhantasiaColors.OnDim,
                                    style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.height(16.dp))
                                Button(onClick = { showNewPlaylistDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PhantasiaColors.Primary)) {
                                    Text("New playlist")
                                }
                            }
                        }
                    } else {
                        LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                            items(playlists.size) { i ->
                                val pl = playlists[i]
                                ListItem(
                                    headlineContent = { Text(pl.playlist.name, color = PhantasiaColors.OnSurface) },
                                    supportingContent = { Text("${pl.songs.size} songs", color = PhantasiaColors.OnDim) },
                                    leadingContent = {
                                        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(PhantasiaColors.Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                            Icon(Icons.AutoMirrored.Filled.QueueMusic, null, tint = PhantasiaColors.Primary)
                                        }
                                    },
                                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                                )
                            }
                        }
                    }
                }
                2 -> {
                    // Offline
                    LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                        item {
                            ListItem(
                                modifier = Modifier.clickable { nav.navigate(Route.Downloads.path) },
                                headlineContent = { Text("Downloaded Songs", color = PhantasiaColors.OnSurface) },
                                supportingContent = { Text("Music available offline", color = PhantasiaColors.OnDim) },
                                leadingContent = {
                                    Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(PhantasiaColors.Primary.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Download, null, tint = PhantasiaColors.Primary)
                                    }
                                },
                                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(end = 16.dp, bottom = 100.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            ExtendedFloatingActionButton(
                onClick          = { showImportSheet = true },
                containerColor   = PhantasiaColors.Primary,
                contentColor     = Color.White,
                icon             = { Icon(Icons.Default.CloudSync, contentDescription = "Import/CloudSync") },
                text             = { Text("Import") },
                shape            = RoundedCornerShape(16.dp)
            )
        }

        if (showNewPlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showNewPlaylistDialog = false; newPlaylistName = "" },
                containerColor   = PhantasiaColors.SurfaceHigh,
                title            = { Text("New playlist", color = PhantasiaColors.OnSurface) },
                text             = {
                    OutlinedTextField(
                        value         = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label         = { Text("Playlist name") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = TextFieldDefaults.colors(
                            focusedIndicatorColor   = PhantasiaColors.Primary,
                            unfocusedIndicatorColor = PhantasiaColors.Outline,
                            focusedTextColor     = PhantasiaColors.OnSurface,
                            unfocusedTextColor   = PhantasiaColors.OnSurface
                        )
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (newPlaylistName.isNotBlank()) {
                            vm.createPlaylist(newPlaylistName)
                            showNewPlaylistDialog = false
                            newPlaylistName = ""
                        }
                    }) { Text("Create", color = PhantasiaColors.Primary) }
                },
                dismissButton = {
                    TextButton(onClick = { showNewPlaylistDialog = false; newPlaylistName = "" }) {
                        Text("Cancel", color = PhantasiaColors.OnDim)
                    }
                }
            )
        }

        if (showImportSheet) {
            ModalBottomSheet(
                onDismissRequest = { showImportSheet = false; importUrl = ""; importMode = null },
                containerColor   = PhantasiaColors.SurfaceHigh
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp, 8.dp, 16.dp, 32.dp)) {
                    Text("Import music", style = MaterialTheme.typography.titleMedium,
                        color = PhantasiaColors.OnSurface, fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp))

                    Text("YouTube Music", style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFFF4444), modifier = Modifier.padding(bottom = 8.dp))

                    ImportOptionRow(
                        icon    = Icons.Default.Link,
                        title   = "Import YTM playlist",
                        subtitle= "Paste a YouTube Music playlist link",
                        colour  = Color(0xFFFF4444),
                        onClick = { importMode = ImportMode.YTM_PLAYLIST }
                    )
                    ImportOptionRow(
                        icon    = Icons.Default.Favorite,
                        title   = "Import liked songs",
                        subtitle= "Import all your YTM liked songs",
                        colour  = Color(0xFFFF4444),
                        onClick = {
                            if (!accountState.isYtmConnected) {
                                showImportSheet = false
                                nav.navigate(Route.YtmLogin.path)
                            } else {
                                accountVm.syncYtm()
                                showImportSheet = false
                            }
                        }
                    )

                    Spacer(Modifier.height(12.dp))

                    Text("Spotify", style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF1DB954), modifier = Modifier.padding(bottom = 8.dp))

                    ImportOptionRow(
                        icon    = Icons.Default.Link,
                        title   = "Import Spotify playlist",
                        subtitle= "Paste a Spotify playlist link",
                        colour  = Color(0xFF1DB954),
                        onClick = { importMode = ImportMode.SPOTIFY_PLAYLIST }
                    )
                    ImportOptionRow(
                        icon    = Icons.Default.Favorite,
                        title   = "Import liked songs",
                        subtitle= "Import all your Spotify saved songs",
                        colour  = Color(0xFF1DB954),
                        onClick = {
                            if (!accountState.isSpotifyConnected) {
                                showImportSheet = false
                                nav.navigate(Route.SpotifyLogin.path)
                            } else {
                                accountVm.syncSpotify()
                                showImportSheet = false
                            }
                        }
                    )

                    if (importMode == ImportMode.YTM_PLAYLIST || importMode == ImportMode.SPOTIFY_PLAYLIST) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            if (importMode == ImportMode.YTM_PLAYLIST)
                                "Paste YouTube Music playlist URL"
                            else "Paste Spotify playlist URL",
                            style = MaterialTheme.typography.labelMedium,
                            color = PhantasiaColors.OnDim,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value         = importUrl,
                            onValueChange = { importUrl = it },
                            placeholder   = {
                                Text(
                                    if (importMode == ImportMode.YTM_PLAYLIST)
                                        "https://music.youtube.com/playlist?list=..."
                                    else "https://open.spotify.com/playlist/...",
                                    color = PhantasiaColors.OnHint,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier      = Modifier.fillMaxWidth(),
                            colors        = TextFieldDefaults.colors(
                                focusedIndicatorColor   = PhantasiaColors.Primary,
                                unfocusedIndicatorColor = PhantasiaColors.Outline,
                                focusedTextColor     = PhantasiaColors.OnSurface,
                                unfocusedTextColor   = PhantasiaColors.OnSurface
                            ),
                            singleLine = false,
                            maxLines   = 3
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick  = {
                                showImportSheet = false
                                importUrl = ""
                                importMode = null
                            },
                            enabled  = importUrl.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors   = ButtonDefaults.buttonColors(containerColor = PhantasiaColors.Primary)
                        ) { Text("Import") }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportOptionRow(
    icon:     androidx.compose.ui.graphics.vector.ImageVector,
    title:    String,
    subtitle: String,
    colour:   Color,
    onClick:  () -> Unit
) {
    ListItem(
        headlineContent   = { Text(title, color = PhantasiaColors.OnSurface) },
        supportingContent = { Text(subtitle, color = PhantasiaColors.OnDim,
            style = MaterialTheme.typography.bodySmall) },
        leadingContent    = {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(colour.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = colour, modifier = Modifier.size(20.dp))
            }
        },
        colors   = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}
