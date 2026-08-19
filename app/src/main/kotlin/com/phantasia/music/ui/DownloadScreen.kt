package com.phantasia.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.phantasia.music.storage.DownloadDao
import com.phantasia.music.storage.DownloadEntity
import com.phantasia.music.storage.DownloadStatus
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val downloadDao: DownloadDao
) : ViewModel() {
    val downloads: StateFlow<List<DownloadEntity>> = downloadDao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteDownload(entity: DownloadEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadDao.delete(entity)
        }
    }

    fun queueDownload(track: TrackModel) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadDao.upsert(
                DownloadEntity(
                    videoId = track.videoId,
                    title = track.title,
                    artistName = track.artistName,
                    albumTitle = track.albumTitle,
                    artworkUrl = track.artworkUrl,
                    status = DownloadStatus.DONE,
                    progress = 100
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(nav: NavController) {
    val vm: DownloadViewModel = hiltViewModel()
    val downloads by vm.downloads.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Downloaded Songs", color = PhantasiaColors.OnSurface) },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PhantasiaColors.OnSurface)
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
            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "${downloads.size} songs available offline",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PhantasiaColors.OnDim,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }

            if (downloads.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Download, null, tint = PhantasiaColors.OnHint, modifier = Modifier.size(48.dp))
                            Spacer(Modifier.height(12.dp))
                            Text("No downloaded songs yet", color = PhantasiaColors.OnDim, style = MaterialTheme.typography.bodyLarge)
                            Spacer(Modifier.height(4.dp))
                            Text("Download songs from search or player to listen offline", color = PhantasiaColors.OnHint, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            } else {
                items(downloads, key = { it.videoId }) { item ->
                    ListItem(
                        headlineContent = {
                            Text(
                                item.title,
                                color = PhantasiaColors.OnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        supportingContent = {
                            Text(
                                item.artistName,
                                color = PhantasiaColors.OnDim,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(PhantasiaColors.SurfaceCard)
                            ) {
                                AsyncImage(
                                    model = item.artworkUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        },
                        trailingContent = {
                            IconButton(onClick = { vm.deleteDownload(item) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = PhantasiaColors.OnDim)
                            }
                        },
                        colors = ListItemDefaults.colors(containerColor = PhantasiaColors.SurfaceCard),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { nav.navigate(Route.Player.build(item.videoId)) }
                    )
                }
            }
        }
    }
}
