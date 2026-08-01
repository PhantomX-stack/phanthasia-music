package com.phantasia.music.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.phantasia.music.Route
import com.phantasia.music.storage.AccountEntity
import com.phantasia.music.storage.AccountService
import com.phantasia.music.storage.ImportedPlaylistEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(nav: NavController) {
    val vm: AccountViewModel = hiltViewModel()
    val state by vm.state.collectAsState()
    val progress by vm.importProgress.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.syncMessage) {
        state.syncMessage?.let { snackbar.showSnackbar(it); vm.clearMessage() }
    }
    LaunchedEffect(state.error) {
        state.error?.let { snackbar.showSnackbar("Error: $it"); vm.clearMessage() }
    }

    Scaffold(
        snackbarHost  = { SnackbarHost(snackbar) },
        containerColor = PhantasiaColors.Midnight,
        topBar = {
            TopAppBar(
                title          = { Text("Connected accounts", color = PhantasiaColors.OnSurface) },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PhantasiaColors.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        if (progress.isImporting) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp).padding(top = padding.calculateTopPadding()),
                colors = CardDefaults.cardColors(containerColor = PhantasiaColors.SurfaceCard)
            ) {
                Column(Modifier.padding(16.dp)) {
                    LinearProgressIndicator(
                        progress = { if (progress.total == 0) 0f else progress.current.toFloat() / progress.total.toFloat() },
                        modifier = Modifier.fillMaxWidth(),
                        color = PhantasiaColors.Primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(progress.statusText, color = PhantasiaColors.OnSurface)
                    Text("~${(progress.total - progress.current).coerceAtLeast(0) * 2}s remaining", color = PhantasiaColors.OnDim, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        LazyColumn(
            modifier       = Modifier.fillMaxSize().padding(padding).background(
                Brush.verticalGradient(listOf(
                    PhantasiaColors.GradTop, PhantasiaColors.GradMid, PhantasiaColors.GradBot
                ))
            ),
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Connect your music accounts to import playlists and liked songs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PhantasiaColors.OnDim,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp))
            }

            // YouTube Music
            item {
                val ytmAccount = state.accounts.find { it.service == AccountService.YOUTUBE_MUSIC }
                ServiceCard(
                    name         = "YouTube Music",
                    colour       = Color(0xFFFF4444),
                    bgColour     = Color(0xFFFF000020),
                    icon         = Icons.Default.MusicNote,
                    account      = ytmAccount,
                    playlists    = state.ytmPlaylists,
                    isSyncing    = state.isSyncing,
                    connectLabel = "Sign in with Google",
                    onConnect    = { nav.navigate(Route.YtmLogin.path) },
                    onSync       = { vm.syncYtm() },
                    onLogout     = { vm.logoutYtm() }
                )
            }

            // Spotify
            item {
                val spotifyAccount = state.accounts.find { it.service == AccountService.SPOTIFY }
                ServiceCard(
                    name         = "Spotify",
                    colour       = Color(0xFF1DB954),
                    bgColour     = Color(0xFF1DB95420),
                    icon         = Icons.Default.Headphones,
                    account      = spotifyAccount,
                    playlists    = state.spotifyPlaylists,
                    isSyncing    = state.isSyncing,
                    connectLabel = "Connect with Spotify",
                    onConnect    = { nav.navigate(Route.SpotifyLogin.path) },
                    onSync       = { vm.syncSpotify() },
                    onLogout     = { vm.logoutSpotify() }
                )
            }

            item {
                Text(
                    "ℹ️  Spotify tracks play via YouTube Music search — Spotify Premium is not required.",
                    style    = MaterialTheme.typography.bodySmall,
                    color    = PhantasiaColors.OnHint,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun ServiceCard(
    name:         String,
    colour:       Color,
    bgColour:     Color,
    icon:         ImageVector,
    account:      AccountEntity?,
    playlists:    List<ImportedPlaylistEntity>,
    isSyncing:    Boolean,
    connectLabel: String,
    onConnect:    () -> Unit,
    onSync:       () -> Unit,
    onLogout:     () -> Unit
) {
    var showLogout by remember { mutableStateOf(false) }
    val connected = account != null

    Card(
        shape  = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PhantasiaColors.SurfaceCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(bgColour),
                    contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = colour, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.titleMedium,
                        color = PhantasiaColors.OnSurface, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (connected) account!!.email.ifBlank { "Connected" }
                        else "Not connected",
                        style = MaterialTheme.typography.bodySmall,
                        color = PhantasiaColors.OnDim
                    )
                }
                if (connected) {
                    Icon(Icons.Default.CheckCircle, "Connected",
                        tint = colour, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(14.dp))

            if (!connected) {
                // Connect button
                Button(
                    onClick  = onConnect,
                    modifier = Modifier.fillMaxWidth(),
                    colors   = ButtonDefaults.buttonColors(containerColor = colour),
                    shape    = RoundedCornerShape(50)
                ) {
                    Icon(Icons.Default.Link, null, modifier = Modifier.size(18.dp),
                        tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text(connectLabel, color = Color.White)
                }
            } else {
                // Playlist summary
                if (playlists.isNotEmpty()) {
                    Text("${playlists.size} playlists imported",
                        style = MaterialTheme.typography.bodySmall,
                        color = PhantasiaColors.OnDim,
                        modifier = Modifier.padding(bottom = 6.dp))
                    playlists.take(3).forEach { pl ->
                        Row(verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)) {
                            Icon(Icons.AutoMirrored.Filled.QueueMusic, null,
                                tint = colour, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("${pl.name} · ${pl.trackCount} songs",
                                style = MaterialTheme.typography.bodySmall,
                                color = PhantasiaColors.OnDim)
                        }
                    }
                    if (playlists.size > 3) {
                        Text("+${playlists.size - 3} more",
                            style = MaterialTheme.typography.labelSmall,
                            color = PhantasiaColors.OnHint,
                            modifier = Modifier.padding(vertical = 2.dp))
                    }
                    Spacer(Modifier.height(10.dp))
                }

                // Sync + Disconnect buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick  = onSync,
                        enabled  = !isSyncing,
                        modifier = Modifier.weight(1f),
                        shape    = RoundedCornerShape(50),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = colour)
                    ) {
                        if (isSyncing) CircularProgressIndicator(
                            Modifier.size(14.dp), strokeWidth = 2.dp, color = colour)
                        else Icon(Icons.Default.Sync, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (isSyncing) "Syncing…" else "Sync")
                    }
                    OutlinedButton(
                        onClick = { showLogout = true },
                        shape   = RoundedCornerShape(50),
                        colors  = ButtonDefaults.outlinedButtonColors(
                            contentColor = PhantasiaColors.Error)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Disconnect")
                    }
                }
            }
        }
    }

    if (showLogout) {
        AlertDialog(
            onDismissRequest = { showLogout = false },
            containerColor   = PhantasiaColors.SurfaceHigh,
            title            = { Text("Disconnect \$name?", color = PhantasiaColors.OnSurface) },
            text             = { Text("All imported playlists will be removed from Phantasia.",
                color = PhantasiaColors.OnDim) },
            confirmButton    = {
                TextButton(onClick = { onLogout(); showLogout = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = PhantasiaColors.Error)) {
                    Text("Disconnect")
                }
            },
            dismissButton    = {
                TextButton(onClick = { showLogout = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = PhantasiaColors.Primary)) {
                    Text("Cancel")
                }
            }
        )
    }
}
