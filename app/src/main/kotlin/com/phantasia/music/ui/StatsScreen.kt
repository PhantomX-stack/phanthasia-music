package com.phantasia.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phantasia.music.storage.PlayCountEntity
import com.phantasia.music.storage.StatsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────
@HiltViewModel
class StatsViewModel @Inject constructor(private val dao: StatsDao) : ViewModel() {
    private fun now() = System.currentTimeMillis()
    private val DAY   = TimeUnit.DAYS.toMillis(1)
    private val WEEK  = TimeUnit.DAYS.toMillis(7)
    private val MONTH = TimeUnit.DAYS.toMillis(30)

    val topSongs     = dao.getTopSongs(20)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val listenToday  = dao.getTotalListenTimeMs(now() - DAY)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val listenWeek   = dao.getTotalListenTimeMs(now() - WEEK)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val listenMonth  = dao.getTotalListenTimeMs(now() - MONTH)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
    val playsToday   = dao.getPlayCountSince(now() - DAY)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val playsWeek    = dao.getPlayCountSince(now() - WEEK)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val uniqueWeek   = dao.getUniqueSongsSince(now() - WEEK)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(nav: NavController) {
    val vm: StatsViewModel = hiltViewModel()
    val topSongs    by vm.topSongs.collectAsState()
    val listenToday by vm.listenToday.collectAsState()
    val listenWeek  by vm.listenWeek.collectAsState()
    val listenMonth by vm.listenMonth.collectAsState()
    val playsToday  by vm.playsToday.collectAsState()
    val playsWeek   by vm.playsWeek.collectAsState()
    val uniqueWeek  by vm.uniqueWeek.collectAsState()

    val hasNoData = topSongs.isEmpty() && (listenToday ?: 0L) == 0L

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(
            PhantasiaColors.GradTop, PhantasiaColors.GradMid, PhantasiaColors.GradBot
        ))
    )) {
        LazyColumn(
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                Spacer(Modifier.height(56.dp))
                Text("Stats",
                    style    = MaterialTheme.typography.headlineSmall,
                    color    = PhantasiaColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            }

            if (hasNoData) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 80.dp),
                        contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)) {
                            Text("🎵", style = MaterialTheme.typography.displayMedium)
                            Spacer(Modifier.height(16.dp))
                            Text("Play some music to see your stats",
                                color = PhantasiaColors.OnDim,
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            } else {
                // Today
                item {
                    StatSectionLabel("Today")
                    Row(modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(Modifier.weight(1f), Icons.Default.Timer,
                            "Listen time", fmtDuration(listenToday ?: 0L),
                            PhantasiaColors.Primary)
                        StatCard(Modifier.weight(1f), Icons.Default.MusicNote,
                            "Songs played", "$playsToday",
                            PhantasiaColors.Secondary)
                    }
                }

                // This week
                item {
                    StatSectionLabel("This week")
                    Row(modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(Modifier.weight(1f), Icons.Default.Timer,
                            "Listen time", fmtDuration(listenWeek ?: 0L),
                            PhantasiaColors.PrimaryGlow)
                        StatCard(Modifier.weight(1f), Icons.Default.Album,
                            "Unique songs", "$uniqueWeek",
                            PhantasiaColors.Tertiary)
                    }
                    Row(modifier = Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        StatCard(Modifier.weight(1f), Icons.Default.Repeat,
                            "Total plays", "$playsWeek",
                            PhantasiaColors.Secondary)
                        StatCard(Modifier.weight(1f), Icons.Default.CalendarMonth,
                            "This month", fmtDuration(listenMonth ?: 0L),
                            PhantasiaColors.Primary)
                    }
                }

                // Top songs
                if (topSongs.isNotEmpty()) {
                    item { StatSectionLabel("Most played") }
                    items(topSongs) { song -> TopSongRow(song) }
                }
            }
        }
    }
}

@Composable
private fun StatSectionLabel(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium,
        color = PhantasiaColors.OnSurface, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
}

@Composable
private fun StatCard(
    modifier: Modifier, icon: ImageVector,
    label: String, value: String, color: Color
) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PhantasiaColors.SurfaceCard)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.titleLarge,
                color = PhantasiaColors.OnSurface, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall, color = PhantasiaColors.OnDim)
        }
    }
}

@Composable
private fun TopSongRow(song: PlayCountEntity) {
    ListItem(
        headlineContent   = { Text(song.title, color = PhantasiaColors.OnSurface,
            maxLines = 1, overflow = TextOverflow.Ellipsis) },
        supportingContent = { Text(song.artistName, color = PhantasiaColors.OnDim) },
        leadingContent    = {
            Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp))
                .background(PhantasiaColors.SurfaceCard)) {
                if (song.artworkUrl.isNotEmpty()) {
                    AsyncImage(model = song.artworkUrl, contentDescription = null,
                        contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
            }
        },
        trailingContent   = {
            Surface(shape = RoundedCornerShape(20.dp),
                color = PhantasiaColors.Primary.copy(alpha = 0.2f)) {
                Text("${song.count}×", style = MaterialTheme.typography.labelLarge,
                    color = PhantasiaColors.Primary, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        },
        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
    )
}

private fun fmtDuration(ms: Long): String {
    if (ms <= 0) return "0m"
    val h = TimeUnit.MILLISECONDS.toHours(ms)
    val m = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
