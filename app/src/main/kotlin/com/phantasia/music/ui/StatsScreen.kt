package com.phantasia.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phantasia.music.storage.StatsDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantasia.music.storage.PlayCountEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsDao: StatsDao
) : ViewModel() {
    private val _timeframe = MutableStateFlow(0L) // 0 = all time

    fun setTimeframe(since: Long) {
        _timeframe.value = since
    }

    val topSongs: StateFlow<List<PlayCountEntity>> = _timeframe.flatMapLatest { since ->
        if (since == 0L) statsDao.getTopSongs(20) else statsDao.getTopSongsSince(since, 20)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTimeMs: StateFlow<Long> = _timeframe.flatMapLatest { since ->
        statsDao.getTotalListenTimeMs(since).map { it ?: 0L }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(nav: NavController, vm: StatsViewModel = androidx.hilt.navigation.compose.hiltViewModel()) {
    val topSongs by vm.topSongs.collectAsState()
    val totalTimeMs by vm.totalTimeMs.collectAsState()

    var selectedTimeframe by remember { mutableStateOf(0) }
    val timeframes = listOf("All time", "Last 30 days", "Last 7 days")

    LaunchedEffect(selectedTimeframe) {
        val now = System.currentTimeMillis()
        val since = when (selectedTimeframe) {
            1 -> now - 30L * 24 * 60 * 60 * 1000
            2 -> now - 7L * 24 * 60 * 60 * 1000
            else -> 0L
        }
        vm.setTimeframe(since)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Your Stats", color = PhantasiaColors.OnSurface) },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PhantasiaColors.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = PhantasiaColors.Midnight
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timeframes.forEachIndexed { index, title ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (selectedTimeframe == index) PhantasiaColors.Primary else PhantasiaColors.SurfaceHigh,
                            modifier = Modifier.clickable { selectedTimeframe = index }
                        ) {
                            Text(
                                text = title,
                                color = if (selectedTimeframe == index) Color.White else PhantasiaColors.OnDim,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = PhantasiaColors.SurfaceCard)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = PhantasiaColors.Primary, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Total Listen Time",
                            style = MaterialTheme.typography.titleMedium,
                            color = PhantasiaColors.OnDim
                        )
                        val hours = totalTimeMs / (1000 * 60 * 60)
                        val mins = (totalTimeMs / (1000 * 60)) % 60
                        Text(
                            text = "${hours}h ${mins}m",
                            style = MaterialTheme.typography.displayMedium,
                            color = PhantasiaColors.OnSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Top Songs",
                    style = MaterialTheme.typography.titleLarge,
                    color = PhantasiaColors.OnSurface,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
            }

            items(topSongs.size) { i ->
                val song = topSongs[i]
                ListItem(
                    headlineContent = { Text(song.title, color = PhantasiaColors.OnSurface, maxLines = 1) },
                    supportingContent = { Text("${song.artistName} • Played ${song.count} times", color = PhantasiaColors.OnDim, maxLines = 1) },
                    leadingContent = {
                        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(8.dp)).background(PhantasiaColors.SurfaceHigh)) {
                            AsyncImage(model = song.artworkUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                )
            }
        }
    }
}
