package com.phantasia.music.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.phantasia.music.BuildConfig
import kotlinx.coroutines.launch

private data class SettingsQuickAction(
    val icon: ImageVector,
    val label: String,
    val targetIndex: Int,
    val accentColor: Color,
)

private data class SettingsCategory(
    val title: String,
    val items: List<SettingsItem>,
)

private data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String? = null,
    val keywords: List<String> = emptyList(),
    val content: @Composable () -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(nav: NavController) {
    val vm: SettingsViewModel = hiltViewModel()
    val s by vm.state.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    val context = LocalContext.current
    val notificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        null
    }
    var isNotificationGranted by remember {
        mutableStateOf(
            notificationPermission == null ||
                ContextCompat.checkSelfPermission(context, notificationPermission) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        isNotificationGranted = granted || isNotificationGranted
    }
    val shouldShowPermissionHint = !isNotificationGranted

    val categories = listOf(
        SettingsCategory(
            title = "Playback",
            items = listOf(
                SettingsItem(
                    icon = Icons.Default.Hd,
                    title = "Audio quality",
                    subtitle = s.audioQuality.label,
                    keywords = listOf("sound", "quality", "hd", "streaming"),
                ) {
                    SettingsDropdown(
                        icon = Icons.Default.Hd,
                        title = "Audio quality",
                        value = s.audioQuality.label,
                        options = AudioQuality.values().map { it.label },
                        onSelect = { label -> AudioQuality.values().find { it.label == label }?.let(vm::setAudioQuality) },
                    )
                },
                SettingsItem(
                    icon = Icons.Default.Tune,
                    title = "Crossfade",
                    subtitle = "${s.crossfadeSeconds}s between tracks",
                    keywords = listOf("transition", "gapless", "fade"),
                ) {
                    CrossfadeSetting(seconds = s.crossfadeSeconds, onChange = vm::setCrossfade)
                },
                SettingsItem(
                    icon = Icons.Default.Equalizer,
                    title = "Normalize volume",
                    subtitle = "Keep volume consistent across tracks",
                    keywords = listOf("equalizer", "loudness", "volume"),
                ) {
                    SettingsToggle(
                        icon = Icons.Default.Equalizer,
                        title = "Normalize volume",
                        subtitle = "Keep volume consistent across tracks",
                        checked = s.normalizeVolume,
                        onToggle = vm::setNormalizeVolume,
                    )
                },
            ),
        ),
        SettingsCategory(
            title = "Network & storage",
            items = listOf(
                SettingsItem(
                    icon = Icons.Default.SignalCellularAlt,
                    title = "Stream on cellular",
                    subtitle = "Use mobile data for streaming",
                    keywords = listOf("mobile", "data", "network"),
                ) {
                    SettingsToggle(
                        icon = Icons.Default.SignalCellularAlt,
                        title = "Stream on cellular",
                        subtitle = "Use mobile data for streaming",
                        checked = s.streamOnCellular,
                        onToggle = vm::setStreamOnCellular,
                    )
                },
                SettingsItem(
                    icon = Icons.Default.Wifi,
                    title = "Download on Wi-Fi only",
                    subtitle = "Cache tracks only when on Wi-Fi",
                    keywords = listOf("download", "offline", "wifi"),
                ) {
                    SettingsToggle(
                        icon = Icons.Default.Wifi,
                        title = "Download on Wi-Fi only",
                        subtitle = "Cache tracks only when on Wi-Fi",
                        checked = s.downloadOnWifiOnly,
                        onToggle = vm::setDownloadOnWifiOnly,
                    )
                },
                SettingsItem(
                    icon = Icons.Default.Storage,
                    title = "Cache size",
                    subtitle = s.cacheSize.label,
                    keywords = listOf("storage", "offline", "cleanup"),
                ) {
                    SettingsDropdown(
                        icon = Icons.Default.Storage,
                        title = "Cache size",
                        value = s.cacheSize.label,
                        options = CacheSize.values().map { it.label },
                        onSelect = { label -> CacheSize.values().find { it.label == label }?.let(vm::setCacheSize) },
                    )
                },
            ),
        ),
        SettingsCategory(
            title = "Appearance & app",
            items = listOf(
                SettingsItem(
                    icon = Icons.Default.DarkMode,
                    title = "Dark mode",
                    subtitle = s.darkMode.label,
                    keywords = listOf("theme", "appearance", "system"),
                ) {
                    SettingsDropdown(
                        icon = Icons.Default.DarkMode,
                        title = "Dark mode",
                        value = s.darkMode.label,
                        options = DarkModeOption.values().map { it.label },
                        onSelect = { label -> DarkModeOption.values().find { it.label == label }?.let(vm::setDarkMode) },
                    )
                },
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Clear cache",
                    subtitle = "Free storage used by cached tracks",
                    keywords = listOf("storage", "delete", "cleanup"),
                ) {
                    ListItem(
                        headlineContent = { Text("Clear cache") },
                        supportingContent = { Text("Free storage used by cached tracks") },
                        leadingContent = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    )
                },
            ),
        ),
    )

    val quickActions = listOf(
        SettingsQuickAction(Icons.Default.DarkMode, "Appearance", 11, MaterialTheme.colorScheme.primary),
        SettingsQuickAction(Icons.Default.Equalizer, "Player", 3, MaterialTheme.colorScheme.tertiary),
        SettingsQuickAction(Icons.Default.Storage, "Storage", 7, MaterialTheme.colorScheme.secondary),
    )
    val filteredCategories = filterSettingsCategories(categories, query)
    val filteredQuickActions = filterQuickActions(quickActions, query)

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 32.dp),
    ) {
        item {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        }

        item {
            SettingsHeroHeader(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 4.dp, bottom = 14.dp),
            )
        }

        if (shouldShowPermissionHint) {
            item {
                AnimatedVisibility(visible = true) {
                    PremiumPermissionCard(
                        onRequestPermission = {
                            notificationPermission?.let(permissionLauncher::launch)
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 14.dp),
                    )
                }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                placeholder = { Text("Search settings") },
            )
        }

        if (filteredQuickActions.isNotEmpty()) {
            item {
                SettingsQuickActionsGrid(
                    title = "Quick actions",
                    actions = filteredQuickActions,
                    onActionClick = { action ->
                        query = ""
                        scope.launch { listState.animateScrollToItem(action.targetIndex) }
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(top = 8.dp, bottom = 12.dp),
                )
            }
        }

        if (filteredCategories.isEmpty()) {
            item {
                EmptyResultsCard(
                    title = "No settings found",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                )
            }
        }

        filteredCategories.forEach { category ->
            item { SettingsSectionHeader(category.title) }
            items(category.items, key = { it.title }) { item -> item.content() }
        }
    }
}

private fun filterQuickActions(actions: List<SettingsQuickAction>, query: String): List<SettingsQuickAction> {
    if (query.isBlank()) return actions
    return actions.filter { it.label.contains(query, ignoreCase = true) }
}

private fun filterSettingsCategories(categories: List<SettingsCategory>, query: String): List<SettingsCategory> {
    if (query.isBlank()) return categories
    return categories.mapNotNull { category ->
        if (category.title.contains(query, ignoreCase = true)) {
            category
        } else {
            val filteredItems = category.items.filter { matchesSearchQuery(it, query) }
            if (filteredItems.isEmpty()) null else category.copy(items = filteredItems)
        }
    }
}

private fun matchesSearchQuery(item: SettingsItem, query: String): Boolean {
    if (item.title.contains(query, ignoreCase = true)) return true
    if (item.subtitle?.contains(query, ignoreCase = true) == true) return true
    return item.keywords.any { keyword ->
        keyword.contains(query, ignoreCase = true) || query.contains(keyword, ignoreCase = true)
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 56.dp, top = 24.dp, bottom = 4.dp),
    )
}

@Composable
private fun CrossfadeSetting(seconds: Int, onChange: (Int) -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Crossfade", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${seconds}s between tracks",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Slider(
            value = seconds.toFloat(),
            onValueChange = { onChange(it.toInt()) },
            valueRange = 0f..12f,
            steps = 11,
            modifier = Modifier.padding(start = 40.dp),
        )
    }
}

@Composable
private fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = { Switch(checked = checked, onCheckedChange = onToggle) },
    )
}

@Composable
private fun SettingsDropdown(
    icon: ImageVector,
    title: String,
    value: String,
    options: List<String>,
    onSelect: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(value, color = MaterialTheme.colorScheme.primary) },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        trailingContent = {
            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    options.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = { onSelect(opt); expanded = false },
                        )
                    }
                }
            }
        },
    )
}



@Composable
private fun SettingsHeroHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Default.Tune,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp),
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Phantasia Music",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = "v${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmptyResultsCard(title: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "Try a different keyword or browse the categories below.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}


@Composable
private fun PremiumPermissionCard(onRequestPermission: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Enable notifications",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Allow playback notifications so Phantasia can keep media controls available while music is playing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onRequestPermission,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 12.dp),
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Allow", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsQuickActionsGrid(
    title: String,
    actions: List<SettingsQuickAction>,
    onActionClick: (SettingsQuickAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (actions.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                                ),
                            ),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                actions.chunked(2).forEach { rowActions ->
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        rowActions.forEach { action ->
                            SettingsQuickActionTile(
                                action = action,
                                onClick = { onActionClick(action) },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowActions.size == 1) Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsQuickActionTile(
    action: SettingsQuickAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "settingsQuickActionScale",
    )

    Surface(
        modifier = modifier
            .height(104.dp)
            .scale(scale),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick,
        interactionSource = interactionSource,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(
                            action.accentColor.copy(alpha = 0.12f),
                            Color.Transparent,
                        ),
                    ),
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(action.accentColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(action.icon, contentDescription = null, tint = action.accentColor, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = action.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
