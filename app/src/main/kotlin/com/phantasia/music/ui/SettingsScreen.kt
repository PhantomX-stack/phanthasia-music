package com.phantasia.music.ui

import androidx.compose.animation.*
import com.phantasia.music.storage.DownloadQuality
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.phantasia.music.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController) {
    val vm: SettingsViewModel = hiltViewModel()
    val s by vm.state.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                listOf(PhantasiaColors.GradTop, PhantasiaColors.GradMid, PhantasiaColors.GradBot)
            ))
    ) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {

            // ── Top bar with search ────────────────────────────────────────────
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(52.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                            if (!nav.popBackStack()) {
                                nav.navigate(Route.Library.path)
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PhantasiaColors.OnBg)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text("Settings", style = MaterialTheme.typography.headlineSmall,
                            color = PhantasiaColors.OnBg, fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f))
                        IconButton(onClick = { isSearching = !isSearching; if (!isSearching) searchQuery = "" }) {
                            Icon(if (isSearching) Icons.Default.Close else Icons.Default.Search,
                                null, tint = PhantasiaColors.OnBg)
                        }
                    }

                    // Search bar
                    AnimatedVisibility(isSearching,
                        enter = expandVertically() + fadeIn(),
                        exit  = shrinkVertically() + fadeOut()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(PhantasiaColors.SurfaceCard)
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Search, null, tint = PhantasiaColors.OnDim,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            BasicTextField(
                                value = searchQuery, onValueChange = { searchQuery = it },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = PhantasiaColors.OnSurface),
                                cursorBrush = SolidColor(PhantasiaColors.Primary),
                                modifier = Modifier.weight(1f),
                                decorationBox = { inner ->
                                    if (searchQuery.isEmpty()) Text("Search settings…",
                                        color = PhantasiaColors.OnDim,
                                        style = MaterialTheme.typography.bodyMedium)
                                    inner()
                                }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // ── Account card at top (Velune-style hero) ────────────────────────
            item {
                SettingsAccountCard(
                    onConnectYtm     = { nav.navigate(Route.YtmLogin.path) },
                    onConnectSpotify = { nav.navigate(Route.SpotifyLogin.path) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // Helper: only show if searchQuery matches
            fun show(vararg keywords: String) =
                searchQuery.isBlank() || keywords.any { it.contains(searchQuery, ignoreCase = true) }

            // ── PLAYBACK ───────────────────────────────────────────────────────
            if (show("playback", "audio", "quality", "crossfade", "normalize",
                    "silence", "auto play", "error", "queue")) {
                item { SettingsSectionHeader("Playback") }
                item {
                    SettingsCard {
                        SettingsDropdownRow(Icons.Default.HighQuality, "Audio quality",
                            s.audioQuality.label,
                            AudioQuality.values().map { "${it.label} — ${it.bitrate}" }
                        ) { label ->
                            AudioQuality.values().find { "${it.label} — ${it.bitrate}" == label }
                                ?.let { vm.setAudioQuality(it) }
                        }
                        SettingsDivider()
                        SettingsSliderRow(Icons.Default.Tune, "Crossfade",
                            "${s.crossfadeSeconds}s", s.crossfadeSeconds.toFloat(), 0f..12f, 11
                        ) { vm.setCrossfade(it.toInt()) }
                        SettingsDivider()
                        SettingsToggleRow(Icons.Default.GraphicEq, "Normalize volume",
                            "Keep loudness consistent", s.normalizeVolume) { vm.setNormalizeVolume(it) }
                        SettingsDivider()
                        SettingsToggleRow(Icons.AutoMirrored.Filled.VolumeOff, "Skip silence",
                            "Skip silent gaps between tracks", s.skipSilence) { vm.setSkipSilence(it) }
                        SettingsDivider()
                        SettingsToggleRow(Icons.AutoMirrored.Filled.QueueMusic, "Auto-play related",
                            "Continue with recommendations after queue ends",
                            s.autoPlayRelated) { vm.setAutoPlayRelated(it) }
                        SettingsDivider()
                        SettingsToggleRow(Icons.Default.SkipNext, "Continue on error",
                            "Skip to next song if one fails", s.continueOnError) { vm.setContinueOnError(it) }
                        SettingsDivider()
                        SettingsToggleRow(Icons.Default.SaveAlt, "Persist queue",
                            "Restore queue when app reopens", s.persistQueue) { vm.setPersistQueue(it) }
                    }
                }
            }

            // ── PLAYER UI ──────────────────────────────────────────────────────
            if (show("player", "background", "lyrics", "notification", "thumbnail")) {
                item { SettingsSectionHeader("Player") }
                item {
                    SettingsCard {
                        // Background style with visual chips
                        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Palette, null,
                                    tint = PhantasiaColors.Primary, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(14.dp))
                                Text("Player background", style = MaterialTheme.typography.bodyLarge,
                                    color = PhantasiaColors.OnSurface, modifier = Modifier.weight(1f))
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(start = 36.dp)) {
                                PlayerBackground.values().forEach { style ->
                                    FilterChip(
                                        selected = s.playerBackground == style,
                                        onClick  = { vm.setPlayerBackground(style) },
                                        label    = { Text(style.label,
                                            style = MaterialTheme.typography.labelSmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PhantasiaColors.Primary,
                                            selectedLabelColor     = Color.White,
                                            containerColor         = PhantasiaColors.SurfaceHigh,
                                            labelColor             = PhantasiaColors.OnDim
                                        )
                                    )
                                }
                            }
                        }
                        SettingsDivider()
                        SettingsToggleRow(Icons.Default.Lyrics, "Show lyrics by default",
                            "Open player with lyrics visible", s.showLyricsByDefault) { vm.setShowLyricsByDefault(it) }
                        SettingsDivider()
                        SettingsDropdownRow(Icons.AutoMirrored.Filled.ViewQuilt, "Lyrics position",
                            s.lyricsPosition.label, LyricsPosition.values().map { it.label }) { label ->
                            LyricsPosition.values().find { it.label == label }?.let { vm.setLyricsPosition(it) }
                        }
                        SettingsDivider()
                        SettingsToggleRow(Icons.Default.NotificationsActive, "Artwork in notification",
                            "Show song thumbnail in media notification",
                            s.showSongThumbnailInNotif) { vm.setShowThumbnailInNotif(it) }
                    }
                }
            }

            // ── NETWORK ────────────────────────────────────────────────────────
            if (show("network", "cellular", "wifi", "cache", "prefetch", "stream", "download")) {
                item { SettingsSectionHeader("Network & Storage") }
                item {
                    SettingsCard {
                        SettingsToggleRow(Icons.Default.NetworkCell, "Stream on cellular",
                            "Use mobile data for streaming", s.streamOnCellular) { vm.setStreamOnCellular(it) }
                        SettingsDivider()
                        SettingsToggleRow(Icons.Default.Wifi, "Download on Wi-Fi only",
                            "Cache audio only when on Wi-Fi", s.downloadOnWifiOnly) { vm.setDownloadOnWifiOnly(it) }
                        SettingsDivider()
                        SettingsDropdownRow(Icons.Default.Storage, "Max cache size",
                            s.cacheSize.label, CacheSize.values().map { it.label }) { label ->
                            CacheSize.values().find { it.label == label }?.let { vm.setCacheSize(it) }
                        }
                        SettingsDivider()
                        SettingsDivider()
                        SettingsDropdownRow(Icons.Default.Download, "Download quality",
                            s.downloadQuality.label,
                            DownloadQuality.values().map { it.label }) { label ->
                            DownloadQuality.values().find { it.label == label }?.let { vm.setDownloadQuality(it) }
                        }
                        SettingsDivider()
                        SettingsToggleRow(Icons.Default.CloudDownload, "Prefetch next song",
                            "Buffer the next song before it starts", s.prefetchEnabled) { vm.setPrefetchEnabled(it) }
                    }
                }
            }

            // ── CONTENT ────────────────────────────────────────────────────────
            if (show("content", "language", "country", "region", "explicit", "safe")) {
                item { SettingsSectionHeader("Content") }
                item {
                    SettingsCard {
                        SettingsDropdownRow(Icons.Default.Language, "Content language",
                            s.contentLanguage.label,
                            ContentLanguage.values().map { it.label }) { label ->
                            ContentLanguage.values().find { it.label == label }?.let { vm.setContentLanguage(it) }
                        }
                        SettingsDivider()
                        SettingsDropdownRow(Icons.Default.Public, "Content country",
                            s.contentCountry.label,
                            ContentCountry.values().map { it.label }) { label ->
                            ContentCountry.values().find { it.label == label }?.let { vm.setContentCountry(it) }
                        }
                        SettingsDivider()
                        SettingsToggleRow(Icons.Default.Warning, "Allow explicit content",
                            "Show songs with explicit lyrics", s.enableExplicit) { vm.setEnableExplicit(it) }
                        SettingsDivider()
                        SettingsToggleRow(Icons.Default.Shield, "Safe search",
                            "Filter potentially mature content", s.safeSearch) { vm.setSafeSearch(it) }
                    }
                }
            }

            // ── PRIVACY ────────────────────────────────────────────────────────
            if (show("privacy", "history", "listen", "search")) {
                item { SettingsSectionHeader("Privacy") }
                item {
                    SettingsCard {
                        SettingsToggleRow(Icons.Default.Visibility, "Pause listen history",
                            "Stop recording what you play", s.pauseListenHistory) { vm.setPauseListenHistory(it) }
                        SettingsDivider()
                        SettingsToggleRow(Icons.Default.SearchOff, "Pause search history",
                            "Don't save recent searches", s.pauseSearchHistory) { vm.setPauseSearchHistory(it) }
                        SettingsDivider()
                        SettingsActionRow(Icons.Default.DeleteSweep, "Clear search history",
                            "Remove all saved searches") { vm.clearSearchHistory() }
                    }
                }
            }

            // ── ACCOUNTS ──────────────────────────────────────────────────────
            if (show("account", "youtube", "spotify", "connect", "login")) {
                item { SettingsSectionHeader("Accounts") }
                item {
                    SettingsCard {
                        SettingsNavRow(Icons.Default.AccountCircle,
                            "Connected accounts",
                            "YouTube Music, Spotify") { nav.navigate(Route.Accounts.path) }
                    }
                }
            }

            // ── STATS ─────────────────────────────────────────────────────────
            if (show("stats", "listen time", "top songs", "history")) {
                item { SettingsSectionHeader("Stats") }
                item {
                    SettingsCard {
                        SettingsNavRow(Icons.Default.BarChart,
                            "Listening stats",
                            "Time spent, top songs, play counts") { nav.navigate(Route.Stats.path) }
                    }
                }
            }

            // ── ABOUT ─────────────────────────────────────────────────────────
            if (show("about", "version", "license", "info")) {
                item { SettingsSectionHeader("About") }
                item {
                    SettingsCard {
                        ListItem(
                            headlineContent   = { Text("Phantasia Music",
                                color = PhantasiaColors.OnSurface) },
                            supportingContent = { Text("Version 1.0.0",
                                color = PhantasiaColors.OnDim) },
                            leadingContent    = {
                                Box(modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp))
                                    .background(PhantasiaColors.PrimaryDim),
                                    contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.MusicNote, null,
                                        tint = Color.White, modifier = Modifier.size(22.dp))
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

// ── Account hero card ─────────────────────────────────────────────────────────
@Composable
private fun SettingsAccountCard(
    onConnectYtm:     () -> Unit,
    onConnectSpotify: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = PhantasiaColors.SurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(Brush.linearGradient(
                        listOf(PhantasiaColors.Primary, PhantasiaColors.Secondary)
                    )), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.AccountCircle, null,
                        tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Your Accounts", style = MaterialTheme.typography.titleSmall,
                        color = PhantasiaColors.OnSurface, fontWeight = FontWeight.SemiBold)
                    Text("Connect to import your music library",
                        style = MaterialTheme.typography.bodySmall, color = PhantasiaColors.OnDim)
                }
                Icon(Icons.Default.ChevronRight, null, tint = PhantasiaColors.OnDim)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // YouTube Music chip
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFF000015), onClick = onConnectYtm) {
                    Row(modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.PlayArrow, null,
                            tint = Color(0xFFFF4444), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("YouTube Music", style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFFFF4444), maxLines = 1)
                    }
                }
                // Spotify chip
                Surface(modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1DB95415), onClick = onConnectSpotify) {
                    Row(modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {
                        Icon(Icons.Default.MusicNote, null,
                            tint = Color(0xFF1DB954), modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Spotify", style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF1DB954))
                    }
                }
            }
        }
    }
}

// ── Reusable Settings UI components ──────────────────────────────────────────
@Composable
fun SettingsSectionHeader(title: String) {
    Text(title.uppercase(), style = MaterialTheme.typography.labelSmall,
        color = PhantasiaColors.Primary, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 6.dp))
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape    = RoundedCornerShape(22.dp),
        colors   = CardDefaults.cardColors(containerColor = PhantasiaColors.SurfaceCard)
    ) { Column(modifier = Modifier.padding(vertical = 4.dp), content = content) }
}

@Composable
fun SettingsDivider() = HorizontalDivider(
    modifier   = Modifier.padding(start = 64.dp, end = 18.dp),
    thickness  = 0.5.dp,
    color      = PhantasiaColors.Outline.copy(alpha = 0.35f)
)

@Composable
fun SettingsToggleRow(
    icon: ImageVector, title: String, subtitle: String,
    checked: Boolean, onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent   = { Text(title, color = PhantasiaColors.OnSurface, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(subtitle, color = PhantasiaColors.OnDim,
            style = MaterialTheme.typography.bodyMedium) },
        leadingContent    = {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                .background(PhantasiaColors.Primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PhantasiaColors.Primary, modifier = Modifier.size(24.dp))
            }
        },
        trailingContent   = {
            Switch(checked = checked, onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor    = Color.White,
                    checkedTrackColor    = PhantasiaColors.Primary,
                    uncheckedThumbColor  = PhantasiaColors.OnDim,
                    uncheckedTrackColor  = PhantasiaColors.SurfaceHigh
                ))
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SettingsDropdownRow(
    icon: ImageVector, title: String, value: String,
    options: List<String>, onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ListItem(
        headlineContent   = { Text(title, color = PhantasiaColors.OnSurface, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(value, color = PhantasiaColors.Primary,
            style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold) },
        leadingContent    = {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                .background(PhantasiaColors.Primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PhantasiaColors.Primary, modifier = Modifier.size(24.dp))
            }
        },
        trailingContent   = {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, null, tint = PhantasiaColors.OnDim, modifier = Modifier.size(28.dp))
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                    containerColor = PhantasiaColors.SurfaceHigh) {
                    options.forEach { opt ->
                        DropdownMenuItem(
                            text    = { Text(opt, color = PhantasiaColors.OnSurface,
                                style = MaterialTheme.typography.bodyLarge) },
                            onClick = { onSelect(opt); expanded = false },
                            leadingIcon = if (opt.contains(value.substringBefore(" "), ignoreCase = true)) ({
                                Icon(Icons.Default.Check, null, tint = PhantasiaColors.Primary,
                                    modifier = Modifier.size(18.dp))
                            }) else null
                        )
                    }
                }
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

@Composable
fun SettingsSliderRow(
    icon: ImageVector, title: String, valueLabel: String,
    value: Float, range: ClosedFloatingPointRange<Float>, steps: Int,
    onChange: (Float) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                .background(PhantasiaColors.Primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PhantasiaColors.Primary, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(title, color = PhantasiaColors.OnSurface,
                style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text(valueLabel, color = PhantasiaColors.Primary,
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range, steps = steps,
            modifier = Modifier.padding(start = 56.dp, top = 4.dp),
            colors = SliderDefaults.colors(thumbColor = PhantasiaColors.Primary,
                activeTrackColor = PhantasiaColors.Primary,
                inactiveTrackColor = PhantasiaColors.Primary.copy(alpha = 0.25f)))
    }
}

@Composable
fun SettingsNavRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent   = { Text(title, color = PhantasiaColors.OnSurface, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(subtitle, color = PhantasiaColors.OnDim,
            style = MaterialTheme.typography.bodyMedium) },
        leadingContent    = {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                .background(PhantasiaColors.Primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PhantasiaColors.Primary, modifier = Modifier.size(24.dp))
            }
        },
        trailingContent   = {
            Icon(Icons.Default.ChevronRight, null, tint = PhantasiaColors.OnDim,
                modifier = Modifier.size(24.dp))
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun SettingsActionRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent   = { Text(title, color = PhantasiaColors.Error, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(subtitle, color = PhantasiaColors.OnDim,
            style = MaterialTheme.typography.bodyMedium) },
        leadingContent    = {
            Box(modifier = Modifier.size(42.dp).clip(RoundedCornerShape(14.dp))
                .background(PhantasiaColors.Error.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PhantasiaColors.Error, modifier = Modifier.size(24.dp))
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}
