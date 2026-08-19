package com.phantasia.music.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import coil.compose.AsyncImage
import com.phantasia.music.Route
import com.phantasia.music.network.TrackModel

enum class ImportMode {
    YTM_PLAYLIST, YTM_LIKED, SPOTIFY_PLAYLIST, SPOTIFY_LIKED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(nav: NavController) {
    val vm: LibraryViewModel = hiltViewModel()
    val playerVm: PlayerViewModel = hiltViewModel()
    val favs by vm.favourites.collectAsState(initial = emptyList())
    val playlists by vm.playlists.collectAsState(initial = emptyList())
    val localSongs by vm.localSongs.collectAsState()
    val isScanning by vm.isScanning.collectAsState()
    val scanMsg by vm.scanMessage.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showNewPlaylistDialog by remember { mutableStateOf(false) }
    var newPlaylistName by remember { mutableStateOf("") }
    var offlineSearchQuery by remember { mutableStateOf("") }

    var showImportSheet by remember { mutableStateOf(false) }
    var importUrl by remember { mutableStateOf("") }
    var importMode by remember { mutableStateOf<ImportMode?>(null) }
    val accountVm: AccountViewModel = hiltViewModel()
    val accountState by accountVm.state.collectAsState()
    val importProgress by accountVm.importProgress.collectAsState()

    val snackbar = remember { SnackbarHostState() }

    // Permission launcher for device scanning
    val permissionToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            vm.scanDeviceMusic()
        }
    }

    LaunchedEffect(scanMsg) {
        scanMsg?.let {
            snackbar.showSnackbar(it)
            vm.clearScanMessage()
        }
    }

    LaunchedEffect(accountState.syncMessage) {
        accountState.syncMessage?.let {
            snackbar.showSnackbar(it)
            accountVm.clearMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = PhantasiaColors.Midnight
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(PhantasiaColors.GradTop, PhantasiaColors.GradMid, PhantasiaColors.GradBot)
                    )
                )
        ) {
            // Header with reliable navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 18.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Your Library",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            fontSize = 28.sp
                        ),
                        color = PhantasiaColors.OnBg
                    )
                    Text(
                        "${favs.size} favourites • ${playlists.size} playlists • ${localSongs.size} offline",
                        style = MaterialTheme.typography.bodySmall,
                        color = PhantasiaColors.OnDim
                    )
                }

                // Import Button in Header
                IconButton(
                    onClick = { showImportSheet = true },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PhantasiaColors.SurfaceCard)
                ) {
                    Icon(
                        Icons.Default.CloudSync,
                        contentDescription = "Import Music",
                        tint = PhantasiaColors.Primary
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Settings icon button with clean top-level tab navigation
                IconButton(
                    onClick = {
                        nav.navigate(Route.Settings.path) {
                            popUpTo(nav.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(PhantasiaColors.SurfaceCard)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = PhantasiaColors.OnSurface
                    )
                }
            }

            // Sync/import progress card
            if (importProgress.isImporting) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PhantasiaColors.SurfaceCard,
                    border = BorderStroke(1.dp, PhantasiaColors.Primary.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        LinearProgressIndicator(
                            progress = {
                                if (importProgress.total > 0) importProgress.current.toFloat() / importProgress.total.toFloat()
                                else 0f
                            },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = PhantasiaColors.Primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            importProgress.statusText,
                            color = PhantasiaColors.OnSurface,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Tab Navigation Pill Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "Liked songs (${favs.size})" to 0,
                    "Playlists (${playlists.size})" to 1,
                    "Offline (${localSongs.size})" to 2
                ).forEach { (title, index) ->
                    val selected = selectedTab == index
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (selected) PhantasiaColors.Primary else PhantasiaColors.SurfaceCard,
                        border = BorderStroke(
                            1.dp,
                            if (selected) PhantasiaColors.Primary else Color.White.copy(alpha = 0.06f)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = index }
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (selected) Color.Black else PhantasiaColors.OnDim,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            when (selectedTab) {
                0 -> {
                    // Liked songs tab
                    if (favs.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(bottom = 120.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(PhantasiaColors.SurfaceCard),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.FavoriteBorder,
                                        contentDescription = null,
                                        tint = PhantasiaColors.Primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "No favourites yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = PhantasiaColors.OnBg,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Heart songs while listening to find them here",
                                    color = PhantasiaColors.OnDim,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 140.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${favs.size} favourite tracks",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PhantasiaColors.OnDim
                                    )
                                    Button(
                                        onClick = {
                                            val first = favs.firstOrNull()
                                            if (first != null) {
                                                val trackModels = favs.map {
                                                    TrackModel(it.videoId, it.title, it.artistName, it.albumTitle, it.artworkUrl, it.durationSeconds)
                                                }
                                                playerVm.onEvent(
                                                    PlayerUiEvent.PlayTrack(
                                                        trackModels.first(),
                                                        trackModels
                                                    )
                                                )
                                                nav.navigate(Route.Player.build(first.videoId))
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PhantasiaColors.Primary),
                                        shape = RoundedCornerShape(16.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Play All", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            items(favs) { song ->
                                val track = TrackModel(
                                    videoId = song.videoId,
                                    title = song.title,
                                    artistName = song.artistName,
                                    albumTitle = song.albumTitle,
                                    artworkUrl = song.artworkUrl,
                                    durationSeconds = song.durationSeconds
                                )
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = PhantasiaColors.SurfaceCard,
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            playerVm.onEvent(PlayerUiEvent.PlayTrack(track, favs.map {
                                                TrackModel(it.videoId, it.title, it.artistName, it.albumTitle, it.artworkUrl, it.durationSeconds)
                                            }))
                                            nav.navigate(Route.Player.build(song.videoId))
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(PhantasiaColors.SurfaceHigh)
                                        ) {
                                            AsyncImage(
                                                model = song.artworkUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                song.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = PhantasiaColors.OnBg,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                song.artistName,
                                                maxLines = 1,
                                                color = PhantasiaColors.OnDim,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        IconButton(onClick = { vm.toggleFavourite(song) }) {
                                            Icon(Icons.Default.Favorite, contentDescription = "Unfavourite", tint = PhantasiaColors.Primary)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // Playlists tab
                    if (playlists.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(bottom = 120.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(PhantasiaColors.SurfaceCard),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.QueueMusic,
                                        contentDescription = null,
                                        tint = PhantasiaColors.Primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "No playlists found",
                                    color = PhantasiaColors.OnBg,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(14.dp))
                                Button(
                                    onClick = { showNewPlaylistDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = PhantasiaColors.Primary),
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Icon(Icons.Default.Add, null, tint = Color.Black)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Create Playlist", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 140.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "${playlists.size} custom playlists",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PhantasiaColors.OnDim
                                    )
                                    TextButton(onClick = { showNewPlaylistDialog = true }) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = PhantasiaColors.Primary)
                                        Spacer(Modifier.width(4.dp))
                                        Text("New Playlist", color = PhantasiaColors.Primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            items(playlists) { pl ->
                                Surface(
                                    shape = RoundedCornerShape(18.dp),
                                    color = PhantasiaColors.SurfaceCard,
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            nav.navigate(
                                                Route.ImportedPlaylist.build(
                                                    pl.playlist.playlistId.toString(),
                                                    pl.playlist.name
                                                )
                                            )
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(54.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(PhantasiaColors.Primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.AutoMirrored.Filled.QueueMusic,
                                                null,
                                                tint = PhantasiaColors.Primary,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                pl.playlist.name,
                                                color = PhantasiaColors.OnBg,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Spacer(Modifier.height(3.dp))
                                            Text(
                                                "${pl.songs.size} tracks",
                                                color = PhantasiaColors.OnDim,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowForwardIos,
                                            null,
                                            tint = PhantasiaColors.OnHint,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    // Offline / Device Scanner Tab
                    val filteredLocal = remember(localSongs, offlineSearchQuery) {
                        if (offlineSearchQuery.isBlank()) localSongs
                        else localSongs.filter {
                            it.title.contains(offlineSearchQuery, ignoreCase = true) ||
                            it.artist.contains(offlineSearchQuery, ignoreCase = true) ||
                            it.album.contains(offlineSearchQuery, ignoreCase = true)
                        }
                    }

                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 140.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Scanner hero banner
                        item {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = PhantasiaColors.SurfaceCard,
                                border = BorderStroke(1.dp, PhantasiaColors.Primary.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(PhantasiaColors.Primary.copy(alpha = 0.15f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.PhoneAndroid,
                                                contentDescription = null,
                                                tint = PhantasiaColors.Primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        Spacer(Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "Device Music Scanner",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = PhantasiaColors.OnBg
                                            )
                                            Text(
                                                if (isScanning) "Scanning storage..." else "${localSongs.size} local audio files found",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = PhantasiaColors.OnDim
                                            )
                                        }
                                    }

                                    Spacer(Modifier.height(14.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = { permissionLauncher.launch(permissionToRequest) },
                                            enabled = !isScanning,
                                            colors = ButtonDefaults.buttonColors(containerColor = PhantasiaColors.Primary),
                                            shape = RoundedCornerShape(14.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            if (isScanning) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(18.dp),
                                                    color = Color.Black,
                                                    strokeWidth = 2.dp
                                                )
                                            } else {
                                                Icon(Icons.Default.Refresh, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(6.dp))
                                                Text("Scan Device", color = Color.Black, fontWeight = FontWeight.Bold)
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = { nav.navigate(Route.Downloads.path) },
                                            shape = RoundedCornerShape(14.dp),
                                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(Icons.Default.Download, null, tint = PhantasiaColors.OnSurface, modifier = Modifier.size(18.dp))
                                            Spacer(Modifier.width(6.dp))
                                            Text("Downloads", color = PhantasiaColors.OnSurface)
                                        }
                                    }
                                }
                            }
                        }

                        // Search local songs
                        if (localSongs.isNotEmpty()) {
                            item {
                                OutlinedTextField(
                                    value = offlineSearchQuery,
                                    onValueChange = { offlineSearchQuery = it },
                                    placeholder = { Text("Filter ${localSongs.size} local songs…", color = PhantasiaColors.OnHint) },
                                    leadingIcon = { Icon(Icons.Default.Search, null, tint = PhantasiaColors.OnDim) },
                                    trailingIcon = {
                                        if (offlineSearchQuery.isNotEmpty()) {
                                            IconButton(onClick = { offlineSearchQuery = "" }) {
                                                Icon(Icons.Default.Close, null, tint = PhantasiaColors.OnDim)
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = PhantasiaColors.SurfaceCard,
                                        unfocusedContainerColor = PhantasiaColors.SurfaceCard,
                                        focusedIndicatorColor = PhantasiaColors.Primary,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        focusedTextColor = PhantasiaColors.OnBg,
                                        unfocusedTextColor = PhantasiaColors.OnBg
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Quick Play & Shuffle all local
                            item {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            val playlist = filteredLocal.map { it.toTrackModel() }
                                            if (playlist.isNotEmpty()) {
                                                playerVm.onEvent(PlayerUiEvent.PlayTrack(playlist.first(), playlist))
                                                nav.navigate(Route.Player.build(playlist.first().videoId))
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PhantasiaColors.Primary),
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Play All", color = Color.Black, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            val playlist = filteredLocal.map { it.toTrackModel() }.shuffled()
                                            if (playlist.isNotEmpty()) {
                                                playerVm.onEvent(PlayerUiEvent.PlayTrack(playlist.first(), playlist))
                                                nav.navigate(Route.Player.build(playlist.first().videoId))
                                            }
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, PhantasiaColors.Primary.copy(alpha = 0.4f)),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Shuffle, null, tint = PhantasiaColors.Primary, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Shuffle", color = PhantasiaColors.Primary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Render local songs
                            items(filteredLocal) { localSong ->
                                val track = localSong.toTrackModel()
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = PhantasiaColors.SurfaceCard,
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val allTracks = filteredLocal.map { it.toTrackModel() }
                                            playerVm.onEvent(PlayerUiEvent.PlayTrack(track, allTracks))
                                            nav.navigate(Route.Player.build(track.videoId))
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(PhantasiaColors.SurfaceHigh),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (localSong.albumArtUri != null) {
                                                AsyncImage(
                                                    model = localSong.albumArtUri,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Icon(
                                                    Icons.Default.Audiotrack,
                                                    null,
                                                    tint = PhantasiaColors.Primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                localSong.title,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = PhantasiaColors.OnBg,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(Modifier.height(2.dp))
                                            Text(
                                                "${localSong.artist} • ${localSong.durationMs / 60000}:${String.format("%02d", (localSong.durationMs % 60000) / 1000)}",
                                                maxLines = 1,
                                                color = PhantasiaColors.OnDim,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Icon(
                                            Icons.Default.PlayCircle,
                                            contentDescription = "Play",
                                            tint = PhantasiaColors.Primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        } else if (!isScanning) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Default.MusicOff,
                                            contentDescription = null,
                                            tint = PhantasiaColors.OnDim,
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(Modifier.height(12.dp))
                                        Text(
                                            "No local audio files detected",
                                            color = PhantasiaColors.OnBg,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(Modifier.height(4.dp))
                                        Text(
                                            "Click 'Scan Device' to scan storage for songs",
                                            color = PhantasiaColors.OnDim,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // New Playlist Dialog
        if (showNewPlaylistDialog) {
            AlertDialog(
                onDismissRequest = { showNewPlaylistDialog = false; newPlaylistName = "" },
                containerColor = PhantasiaColors.SurfaceHigh,
                title = { Text("Create New Playlist", color = PhantasiaColors.OnBg, fontWeight = FontWeight.Bold) },
                text = {
                    OutlinedTextField(
                        value = newPlaylistName,
                        onValueChange = { newPlaylistName = it },
                        label = { Text("Playlist Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = PhantasiaColors.Primary,
                            unfocusedIndicatorColor = PhantasiaColors.Outline,
                            focusedTextColor = PhantasiaColors.OnBg,
                            unfocusedTextColor = PhantasiaColors.OnBg
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPlaylistName.isNotBlank()) {
                                vm.createPlaylist(newPlaylistName)
                                showNewPlaylistDialog = false
                                newPlaylistName = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PhantasiaColors.Primary)
                    ) { Text("Create", color = Color.Black, fontWeight = FontWeight.Bold) }
                },
                dismissButton = {
                    TextButton(onClick = { showNewPlaylistDialog = false; newPlaylistName = "" }) {
                        Text("Cancel", color = PhantasiaColors.OnDim)
                    }
                }
            )
        }

        // Import sheet
        if (showImportSheet) {
            ModalBottomSheet(
                onDismissRequest = { showImportSheet = false; importUrl = ""; importMode = null },
                containerColor = PhantasiaColors.SurfaceHigh
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp, 8.dp, 18.dp, 36.dp)) {
                    Text(
                        "Import Music & Playlists",
                        style = MaterialTheme.typography.titleLarge,
                        color = PhantasiaColors.OnBg,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    Text(
                        "YouTube Music",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFFFF4444),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ImportOptionRow(
                        icon = Icons.Default.Link,
                        title = "Import YTM playlist link",
                        subtitle = "Paste a public YouTube Music playlist link",
                        colour = Color(0xFFFF4444),
                        onClick = { importMode = ImportMode.YTM_PLAYLIST }
                    )
                    ImportOptionRow(
                        icon = Icons.Default.Favorite,
                        title = "Sync Liked Songs",
                        subtitle = "Import all your YTM liked tracks",
                        colour = Color(0xFFFF4444),
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

                    Spacer(Modifier.height(14.dp))

                    Text(
                        "Spotify",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF1DB954),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ImportOptionRow(
                        icon = Icons.Default.Link,
                        title = "Import Spotify playlist link",
                        subtitle = "Paste a Spotify playlist or album URL",
                        colour = Color(0xFF1DB954),
                        onClick = { importMode = ImportMode.SPOTIFY_PLAYLIST }
                    )
                    ImportOptionRow(
                        icon = Icons.Default.Favorite,
                        title = "Sync Spotify Saved Songs",
                        subtitle = "Import all your Spotify liked tracks",
                        colour = Color(0xFF1DB954),
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
                            if (importMode == ImportMode.YTM_PLAYLIST) "Paste YouTube Music playlist URL"
                            else "Paste Spotify playlist URL",
                            style = MaterialTheme.typography.labelMedium,
                            color = PhantasiaColors.OnDim,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = importUrl,
                            onValueChange = { importUrl = it },
                            placeholder = {
                                Text(
                                    if (importMode == ImportMode.YTM_PLAYLIST) "https://music.youtube.com/playlist?list=..."
                                    else "https://open.spotify.com/playlist/...",
                                    color = PhantasiaColors.OnHint,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = PhantasiaColors.Primary,
                                unfocusedIndicatorColor = PhantasiaColors.Outline,
                                focusedTextColor = PhantasiaColors.OnBg,
                                unfocusedTextColor = PhantasiaColors.OnBg
                            ),
                            singleLine = true
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                val urlToImport = importUrl
                                val currentMode = importMode
                                showImportSheet = false
                                importUrl = ""
                                importMode = null
                                if (currentMode == ImportMode.YTM_PLAYLIST) {
                                    accountVm.importYtmPlaylistUrl(urlToImport)
                                } else if (currentMode == ImportMode.SPOTIFY_PLAYLIST) {
                                    accountVm.importSpotifyPlaylistUrl(urlToImport)
                                }
                            },
                            enabled = importUrl.isNotBlank(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PhantasiaColors.Primary)
                        ) { Text("Import Now", color = Color.Black, fontWeight = FontWeight.Bold) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImportOptionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    colour: Color,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PhantasiaColors.SurfaceCard,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colour.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = colour, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = PhantasiaColors.OnBg, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = PhantasiaColors.OnDim, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
