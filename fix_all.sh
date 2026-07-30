git checkout 36e8ea8
git branch -D temp_fixes || true
git checkout -b temp_fixes

# 1. Navigation & Theme
cat << 'PY' > patch_nav_theme.py
import re

with open('app/src/main/kotlin/com/phantasia/music/AppNavigation.kt', 'r') as f:
    content = f.read()

insets_old = "contentWindowInsets = WindowInsets(0, 0, 0, 0),"
insets_new = "contentWindowInsets = WindowInsets.systemBars,"
content = content.replace(insets_old, insets_new)

old_popup = """                                            popUpTo(nav.graph.findStartDestination().id) {
                                                saveState = true
                                            }"""
new_popup = """                                            popUpTo(nav.graph.findStartDestination().id) {
                                                saveState = true
                                                inclusive = false
                                            }"""
content = content.replace(old_popup, new_popup)

with open('app/src/main/kotlin/com/phantasia/music/AppNavigation.kt', 'w') as f:
    f.write(content)

with open('app/src/main/kotlin/com/phantasia/music/ui/Theme.kt', 'r') as f:
    content = f.read()

replacements = {
    "val Midnight   = Color(0xFF050510)": "val Midnight   = Color(0xCC050510)",
    "val Midnight2  = Color(0xFF0A0A1A)": "val Midnight2  = Color(0xCC0A0A1A)",
    "val Surface    = Color(0xFF12122A)": "val Surface    = Color(0x8012122A)",
    "val SurfaceHigh= Color(0xFF1A1A3A)": "val SurfaceHigh= Color(0x801A1A3A)",
    "val SurfaceCard= Color(0xFF181830)": "val SurfaceCard= Color(0x66181830)"
}
for old, new in replacements.items():
    content = content.replace(old, new)

with open('app/src/main/kotlin/com/phantasia/music/ui/Theme.kt', 'w') as f:
    f.write(content)

for screen in ['SearchScreen.kt', 'HomeScreen.kt', 'AccountsScreen.kt', 'SettingsScreen.kt']:
    path = f'app/src/main/kotlin/com/phantasia/music/ui/{screen}'
    with open(path, 'r') as f:
        content = f.read()

    bg_old = """        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(
                PhantasiaColors.Midnight, PhantasiaColors.GradMid, PhantasiaColors.GradBot
            ))
        )"""
    bg_new = """        modifier = Modifier.fillMaxSize().background(PhantasiaColors.Midnight)"""
    content = content.replace(bg_old, bg_new)

    bg_old2 = """        modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(
            PhantasiaColors.GradTop, PhantasiaColors.GradMid, PhantasiaColors.GradBot
        ))
    )"""
    content = content.replace(bg_old2, bg_new)

    bg_old3 = """        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(
                listOf(PhantasiaColors.GradTop, PhantasiaColors.GradMid, PhantasiaColors.GradBot)
            ))"""
    content = content.replace(bg_old3, """        modifier = Modifier
            .fillMaxSize()
            .background(PhantasiaColors.Midnight)""")

    with open(path, 'w') as f:
        f.write(content)
PY
python3 patch_nav_theme.py

# 2. Settings Redesign
cat << 'PY' > patch_settings.py
with open('app/src/main/kotlin/com/phantasia/music/ui/SettingsScreen.kt', 'r') as f:
    content = f.read()

bg_old = """    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = PhantasiaColors.OnSurface) },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PhantasiaColors.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = PhantasiaColors.Midnight
    )"""

bg_new = """    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = PhantasiaColors.OnSurface) },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PhantasiaColors.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    )"""

content = content.replace(bg_old, bg_new)

main_content_old = """                    // Search bar
                    AnimatedVisibility(isSearching,"""

new_content = """                item {
                    SettingsCategory(
                        title = "Appearance",
                        icon = Icons.Default.Palette,
                        expanded = expandedCategory == "Appearance",
                        onClick = { expandedCategory = if (expandedCategory == "Appearance") null else "Appearance" }
                    ) {
                        SettingsToggleRow(Icons.Default.FormatPaint, "Dynamic Colour", "Extract theme from artwork", s.dynamicColour, vm::setDynamicColour)
                        SettingsToggleRow(Icons.Default.DarkMode, "Pure Black", "Use pitch black background", s.pureBlack, vm::setPureBlack)
                    }
                }

                item {
                    SettingsCategory(
                        title = "Account",
                        icon = Icons.Default.AccountCircle,
                        expanded = expandedCategory == "Account",
                        onClick = { expandedCategory = if (expandedCategory == "Account") null else "Account" }
                    ) {
                        SettingsNavRow(Icons.Default.Person, "Manage Accounts", "Connect Spotify, YouTube Music") {
                            nav.navigate(Route.Accounts.path)
                        }
                    }
                }

                item {
                    SettingsCategory(
                        title = "Player & Audio",
                        icon = Icons.Default.Audiotrack,
                        expanded = expandedCategory == "Player & Audio",
                        onClick = { expandedCategory = if (expandedCategory == "Player & Audio") null else "Player & Audio" }
                    ) {
                        SettingsToggleRow(Icons.Default.VolumeUp, "Normalize Volume", "Keep audio levels consistent", s.normalizeVolume, vm::setNormalizeVolume)
                        SettingsToggleRow(Icons.Default.SkipNext, "Skip Silence", "Skip silent parts of audio", s.skipSilence, vm::setSkipSilence)
                        SettingsSliderRow(Icons.Default.LinearScale, "Crossfade", "${s.crossfadeSeconds}s", s.crossfadeSeconds.toFloat(), 0f..10f, 10) { vm.setCrossfade(it.toInt()) }
                        SettingsDropdownRow(Icons.Default.HighQuality, "Audio Quality", s.audioQuality.label, AudioQuality.values().map { it.label }) { lbl ->
                            vm.setAudioQuality(AudioQuality.values().first { it.label == lbl })
                        }
                        SettingsDropdownRow(Icons.Default.Wallpaper, "Player Background", s.playerBackground.label, PlayerBackground.values().map { it.label }) { lbl ->
                            vm.setPlayerBackground(PlayerBackground.values().first { it.label == lbl })
                        }
                        SettingsDropdownRow(Icons.Default.Subtitles, "Lyrics Position", s.lyricsPosition.label, LyricsPosition.values().map { it.label }) { lbl ->
                            vm.setLyricsPosition(LyricsPosition.values().first { it.label == lbl })
                        }
                        SettingsToggleRow(Icons.Default.Lyrics, "Show Lyrics By Default", "Auto-open lyrics tab", s.showLyricsByDefault, vm::setShowLyricsByDefault)
                        SettingsToggleRow(Icons.Default.Image, "Notification Thumbnail", "Show artwork in notification", s.showSongThumbnailInNotif, vm::setShowThumbnailInNotif)
                    }
                }

                item {
                    SettingsCategory(
                        title = "Network & Storage",
                        icon = Icons.Default.Wifi,
                        expanded = expandedCategory == "Network & Storage",
                        onClick = { expandedCategory = if (expandedCategory == "Network & Storage") null else "Network & Storage" }
                    ) {
                        SettingsToggleRow(Icons.Default.CellTower, "Stream on Cellular", "Use mobile data", s.streamOnCellular, vm::setStreamOnCellular)
                        SettingsToggleRow(Icons.Default.SignalWifi4Bar, "Download on Wi-Fi Only", "Save cellular data", s.downloadOnWifiOnly, vm::setDownloadOnWifiOnly)
                        SettingsDropdownRow(Icons.Default.Download, "Download Quality", s.downloadQuality.name, DownloadQuality.values().map { it.name }) { lbl ->
                            vm.setDownloadQuality(DownloadQuality.values().first { it.name == lbl })
                        }
                        SettingsDropdownRow(Icons.Default.Storage, "Max Cache Size", s.cacheSize.label, CacheSize.values().map { it.label }) { lbl ->
                            vm.setCacheSize(CacheSize.values().first { it.label == lbl })
                        }
                        SettingsToggleRow(Icons.Default.FastForward, "Prefetch Next Song", "Load ahead for gapless play", s.prefetchEnabled, vm::setPrefetchEnabled)
                    }
                }

                item {
                    SettingsCategory(
                        title = "Content",
                        icon = Icons.Default.Language,
                        expanded = expandedCategory == "Content",
                        onClick = { expandedCategory = if (expandedCategory == "Content") null else "Content" }
                    ) {
                        SettingsDropdownRow(Icons.Default.Flag, "Language", s.contentLanguage.label, ContentLanguage.values().map { it.label }) { lbl ->
                            vm.setContentLanguage(ContentLanguage.values().first { it.label == lbl })
                        }
                        SettingsDropdownRow(Icons.Default.Public, "Country", s.contentCountry.label, ContentCountry.values().map { it.label }) { lbl ->
                            vm.setContentCountry(ContentCountry.values().first { it.label == lbl })
                        }
                        SettingsToggleRow(Icons.Default.Explicit, "Explicit Content", "Allow explicit songs", s.enableExplicit, vm::setEnableExplicit)
                    }
                }

                item {
                    SettingsCategory(
                        title = "Privacy",
                        icon = Icons.Default.Security,
                        expanded = expandedCategory == "Privacy",
                        onClick = { expandedCategory = if (expandedCategory == "Privacy") null else "Privacy" }
                    ) {
                        SettingsToggleRow(Icons.Default.HistoryToggleOff, "Pause Listen History", "Don't save played songs", s.pauseListenHistory, vm::setPauseListenHistory)
                        SettingsToggleRow(Icons.Default.SearchOff, "Pause Search History", "Don't save search queries", s.pauseSearchHistory, vm::setPauseSearchHistory)
                        SettingsActionRow(Icons.Default.DeleteForever, "Clear Search History", "Remove all past searches") { vm.clearSearchHistory() }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCategory(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PhantasiaColors.SurfaceCard)
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PhantasiaColors.Primary)
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PhantasiaColors.OnSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = PhantasiaColors.OnDim
            )
        }
        if (expanded) {
            HorizontalDivider(color = PhantasiaColors.Outline.copy(alpha = 0.5f))
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}"""
# Wait, I don't want to break the file, let me do this manually with full replacement.
PY

cat << 'EOF' > app/src/main/kotlin/com/phantasia/music/ui/SettingsScreen.kt
package com.phantasia.music.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.phantasia.music.storage.DownloadQuality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController) {
    val vm: SettingsViewModel = hiltViewModel()
    val s by vm.state.collectAsState()

    var expandedCategory by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = PhantasiaColors.OnSurface) },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PhantasiaColors.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(PhantasiaColors.Midnight)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    SettingsCategory(
                        title = "Appearance",
                        icon = Icons.Default.Palette,
                        expanded = expandedCategory == "Appearance",
                        onClick = { expandedCategory = if (expandedCategory == "Appearance") null else "Appearance" }
                    ) {
                        SettingsToggleRow(Icons.Default.FormatPaint, "Dynamic Colour", "Extract theme from artwork", s.dynamicColour, vm::setDynamicColour)
                        SettingsToggleRow(Icons.Default.DarkMode, "Pure Black", "Use pitch black background", s.pureBlack, vm::setPureBlack)
                    }
                }

                item {
                    SettingsCategory(
                        title = "Account",
                        icon = Icons.Default.AccountCircle,
                        expanded = expandedCategory == "Account",
                        onClick = { expandedCategory = if (expandedCategory == "Account") null else "Account" }
                    ) {
                        SettingsNavRow(Icons.Default.Person, "Manage Accounts", "Connect Spotify, YouTube Music") {
                            nav.navigate(Route.Accounts.path)
                        }
                    }
                }

                item {
                    SettingsCategory(
                        title = "Player & Audio",
                        icon = Icons.Default.Audiotrack,
                        expanded = expandedCategory == "Player & Audio",
                        onClick = { expandedCategory = if (expandedCategory == "Player & Audio") null else "Player & Audio" }
                    ) {
                        SettingsToggleRow(Icons.Default.VolumeUp, "Normalize Volume", "Keep audio levels consistent", s.normalizeVolume, vm::setNormalizeVolume)
                        SettingsToggleRow(Icons.Default.SkipNext, "Skip Silence", "Skip silent parts of audio", s.skipSilence, vm::setSkipSilence)
                        SettingsSliderRow(Icons.Default.LinearScale, "Crossfade", "${s.crossfadeSeconds}s", s.crossfadeSeconds.toFloat(), 0f..10f, 10) { vm.setCrossfade(it.toInt()) }
                        SettingsDropdownRow(Icons.Default.HighQuality, "Audio Quality", s.audioQuality.label, AudioQuality.values().map { it.label }) { lbl ->
                            vm.setAudioQuality(AudioQuality.values().first { it.label == lbl })
                        }
                        SettingsToggleRow(Icons.Default.Autorenew, "Auto-Play Related", "Play similar songs after queue ends", s.autoPlayRelated, vm::setAutoPlayRelated)
                        SettingsToggleRow(Icons.Default.ErrorOutline, "Continue On Error", "Skip broken tracks automatically", s.continueOnError, vm::setContinueOnError)
                        SettingsToggleRow(Icons.Default.Save, "Persist Queue", "Save queue between sessions", s.persistQueue, vm::setPersistQueue)
                        SettingsDropdownRow(Icons.Default.Wallpaper, "Player Background", s.playerBackground.label, PlayerBackground.values().map { it.label }) { lbl ->
                            vm.setPlayerBackground(PlayerBackground.values().first { it.label == lbl })
                        }
                        SettingsDropdownRow(Icons.Default.Subtitles, "Lyrics Position", s.lyricsPosition.label, LyricsPosition.values().map { it.label }) { lbl ->
                            vm.setLyricsPosition(LyricsPosition.values().first { it.label == lbl })
                        }
                        SettingsToggleRow(Icons.Default.Lyrics, "Show Lyrics By Default", "Auto-open lyrics tab", s.showLyricsByDefault, vm::setShowLyricsByDefault)
                        SettingsToggleRow(Icons.Default.Image, "Notification Thumbnail", "Show artwork in notification", s.showSongThumbnailInNotif, vm::setShowThumbnailInNotif)
                    }
                }

                item {
                    SettingsCategory(
                        title = "Network & Storage",
                        icon = Icons.Default.Wifi,
                        expanded = expandedCategory == "Network & Storage",
                        onClick = { expandedCategory = if (expandedCategory == "Network & Storage") null else "Network & Storage" }
                    ) {
                        SettingsToggleRow(Icons.Default.CellTower, "Stream on Cellular", "Use mobile data", s.streamOnCellular, vm::setStreamOnCellular)
                        SettingsToggleRow(Icons.Default.SignalWifi4Bar, "Download on Wi-Fi Only", "Save cellular data", s.downloadOnWifiOnly, vm::setDownloadOnWifiOnly)
                        SettingsDropdownRow(Icons.Default.Download, "Download Quality", s.downloadQuality.name, DownloadQuality.values().map { it.name }) { lbl ->
                            vm.setDownloadQuality(DownloadQuality.values().first { it.name == lbl })
                        }
                        SettingsDropdownRow(Icons.Default.Storage, "Max Cache Size", s.cacheSize.label, CacheSize.values().map { it.label }) { lbl ->
                            vm.setCacheSize(CacheSize.values().first { it.label == lbl })
                        }
                        SettingsToggleRow(Icons.Default.FastForward, "Prefetch Next Song", "Load ahead for gapless play", s.prefetchEnabled, vm::setPrefetchEnabled)
                    }
                }

                item {
                    SettingsCategory(
                        title = "Content",
                        icon = Icons.Default.Language,
                        expanded = expandedCategory == "Content",
                        onClick = { expandedCategory = if (expandedCategory == "Content") null else "Content" }
                    ) {
                        SettingsDropdownRow(Icons.Default.Flag, "Language", s.contentLanguage.label, ContentLanguage.values().map { it.label }) { lbl ->
                            vm.setContentLanguage(ContentLanguage.values().first { it.label == lbl })
                        }
                        SettingsDropdownRow(Icons.Default.Public, "Country", s.contentCountry.label, ContentCountry.values().map { it.label }) { lbl ->
                            vm.setContentCountry(ContentCountry.values().first { it.label == lbl })
                        }
                        SettingsToggleRow(Icons.Default.Explicit, "Explicit Content", "Allow explicit songs", s.enableExplicit, vm::setEnableExplicit)
                    }
                }

                item {
                    SettingsCategory(
                        title = "Privacy",
                        icon = Icons.Default.Security,
                        expanded = expandedCategory == "Privacy",
                        onClick = { expandedCategory = if (expandedCategory == "Privacy") null else "Privacy" }
                    ) {
                        SettingsToggleRow(Icons.Default.HistoryToggleOff, "Pause Listen History", "Don't save played songs", s.pauseListenHistory, vm::setPauseListenHistory)
                        SettingsToggleRow(Icons.Default.SearchOff, "Pause Search History", "Don't save search queries", s.pauseSearchHistory, vm::setPauseSearchHistory)
                        SettingsActionRow(Icons.Default.DeleteForever, "Clear Search History", "Remove all past searches") { vm.clearSearchHistory() }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsCategory(
    title: String,
    icon: ImageVector,
    expanded: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PhantasiaColors.SurfaceCard)
            .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = PhantasiaColors.Primary)
            Spacer(Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = PhantasiaColors.OnSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = PhantasiaColors.OnDim
            )
        }
        if (expanded) {
            HorizontalDivider(color = PhantasiaColors.Outline.copy(alpha = 0.5f))
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun SettingsToggleRow(
    icon: ImageVector, title: String, subtitle: String,
    checked: Boolean, onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent   = { Text(title, color = PhantasiaColors.OnSurface) },
        supportingContent = { Text(subtitle, color = PhantasiaColors.OnDim,
            style = MaterialTheme.typography.bodySmall) },
        leadingContent    = {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(PhantasiaColors.Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PhantasiaColors.Primary, modifier = Modifier.size(20.dp))
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
        headlineContent   = { Text(title, color = PhantasiaColors.OnSurface) },
        supportingContent = { Text(value, color = PhantasiaColors.Primary,
            style = MaterialTheme.typography.bodySmall) },
        leadingContent    = {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(PhantasiaColors.Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PhantasiaColors.Primary, modifier = Modifier.size(20.dp))
            }
        },
        trailingContent   = {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, null, tint = PhantasiaColors.OnDim)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false },
                    containerColor = PhantasiaColors.SurfaceHigh) {
                    options.forEach { opt ->
                        DropdownMenuItem(
                            text    = { Text(opt, color = PhantasiaColors.OnSurface,
                                style = MaterialTheme.typography.bodyMedium) },
                            onClick = { onSelect(opt); expanded = false },
                            leadingIcon = if (opt.contains(value.substringBefore(" "), ignoreCase = true)) ({
                                Icon(Icons.Default.Check, null, tint = PhantasiaColors.Primary,
                                    modifier = Modifier.size(16.dp))
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
    Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(PhantasiaColors.Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PhantasiaColors.Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Text(title, color = PhantasiaColors.OnSurface,
                style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Text(valueLabel, color = PhantasiaColors.Primary,
                style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        Slider(value = value, onValueChange = onChange, valueRange = range, steps = steps,
            modifier = Modifier.padding(start = 50.dp),
            colors = SliderDefaults.colors(thumbColor = PhantasiaColors.Primary,
                activeTrackColor = PhantasiaColors.Primary,
                inactiveTrackColor = PhantasiaColors.Primary.copy(alpha = 0.25f)))
    }
}

@Composable
fun SettingsNavRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent   = { Text(title, color = PhantasiaColors.OnSurface) },
        supportingContent = { Text(subtitle, color = PhantasiaColors.OnDim,
            style = MaterialTheme.typography.bodySmall) },
        leadingContent    = {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(PhantasiaColors.Primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PhantasiaColors.Primary, modifier = Modifier.size(20.dp))
            }
        },
        trailingContent   = {
            Icon(Icons.Default.ChevronRight, null, tint = PhantasiaColors.OnDim,
                modifier = Modifier.size(20.dp))
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun SettingsActionRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    ListItem(
        headlineContent   = { Text(title, color = PhantasiaColors.Error) },
        supportingContent = { Text(subtitle, color = PhantasiaColors.OnDim,
            style = MaterialTheme.typography.bodySmall) },
        leadingContent    = {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(PhantasiaColors.Error.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = PhantasiaColors.Error, modifier = Modifier.size(20.dp))
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        modifier = Modifier.clickable { onClick() }
    )
}
