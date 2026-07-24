package com.phantasia.music.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController) {
    val vm: SettingsViewModel = hiltViewModel()
    val s by vm.state.collectAsState()

    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            TopAppBar(
                title          = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }

        item { SettingsSectionHeader("Playback") }

        item {
            SettingsDropdown(
                icon    = Icons.Default.Hd,
                title   = "Audio quality",
                value   = s.audioQuality.label,
                options = AudioQuality.values().map { it.label },
                onSelect = { label ->
                    AudioQuality.values().find { it.label == label }?.let { vm.setAudioQuality(it) }
                }
            )
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Crossfade", style = MaterialTheme.typography.bodyLarge)
                        Text("${s.crossfadeSeconds}s between tracks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Slider(
                    value         = s.crossfadeSeconds.toFloat(),
                    onValueChange = { vm.setCrossfade(it.toInt()) },
                    valueRange    = 0f..12f,
                    steps         = 11,
                    modifier      = Modifier.padding(start = 40.dp)
                )
            }
        }

        item {
            SettingsToggle(
                icon    = Icons.Default.Equalizer,
                title   = "Normalize volume",
                subtitle = "Keep volume consistent across tracks",
                checked  = s.normalizeVolume,
                onToggle = { vm.setNormalizeVolume(it) }
            )
        }

        item { SettingsSectionHeader("Network") }

        item {
            SettingsToggle(
                icon     = Icons.Default.SignalCellularAlt,
                title    = "Stream on cellular",
                subtitle = "Use mobile data for streaming",
                checked  = s.streamOnCellular,
                onToggle = { vm.setStreamOnCellular(it) }
            )
        }

        item {
            SettingsToggle(
                icon     = Icons.Default.Wifi,
                title    = "Download on Wi-Fi only",
                subtitle = "Cache tracks only when on Wi-Fi",
                checked  = s.downloadOnWifiOnly,
                onToggle = { vm.setDownloadOnWifiOnly(it) }
            )
        }

        item {
            SettingsDropdown(
                icon    = Icons.Default.Storage,
                title   = "Cache size",
                value   = s.cacheSize.label,
                options = CacheSize.values().map { it.label },
                onSelect = { label ->
                    CacheSize.values().find { it.label == label }?.let { vm.setCacheSize(it) }
                }
            )
        }

        item { SettingsSectionHeader("Appearance") }

        item {
            SettingsDropdown(
                icon    = Icons.Default.DarkMode,
                title   = "Dark mode",
                value   = s.darkMode.label,
                options = DarkModeOption.values().map { it.label },
                onSelect = { label ->
                    DarkModeOption.values().find { it.label == label }?.let { vm.setDarkMode(it) }
                }
            )
        }

        item { SettingsSectionHeader("About") }

        item {
            ListItem(
                headlineContent  = { Text("Version") },
                supportingContent = { Text("1.0.0") },
                leadingContent   = {
                    Icon(Icons.Default.Info, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            )
        }

        item {
            ListItem(
                headlineContent = { Text("Clear cache") },
                leadingContent  = {
                    Icon(Icons.Default.Delete, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                modifier = Modifier
            )
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelLarge,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 56.dp, top = 24.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsToggle(
    icon:     ImageVector,
    title:    String,
    subtitle: String,
    checked:  Boolean,
    onToggle: (Boolean) -> Unit
) {
    ListItem(
        headlineContent   = { Text(title) },
        supportingContent = { Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent    = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent   = { Switch(checked = checked, onCheckedChange = onToggle) }
    )
}

@Composable
private fun SettingsDropdown(
    icon:     ImageVector,
    title:    String,
    value:    String,
    options:  List<String>,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ListItem(
        headlineContent   = { Text(title) },
        supportingContent = { Text(value, color = MaterialTheme.colorScheme.primary) },
        leadingContent    = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent   = {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { opt ->
                        DropdownMenuItem(
                            text    = { Text(opt) },
                            onClick = { onSelect(opt); expanded = false }
                        )
                    }
                }
            }
        }
    )
}
