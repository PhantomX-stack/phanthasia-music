package com.phantasia.music.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.phantasia.music.network.MusicRepository
import com.phantasia.music.network.TrackModel
import com.phantasia.music.storage.DownloadDao
import com.phantasia.music.storage.DownloadEntity
import com.phantasia.music.storage.DownloadQuality
import com.phantasia.music.storage.DownloadStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────
@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val dao:  DownloadDao,
    private val repo: MusicRepository
) : ViewModel() {

    val allDownloads = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val completedDownloads = dao.getCompleted()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val activeDownloads = dao.getActive()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun downloadTrack(
        track:   TrackModel,
        quality: DownloadQuality = DownloadQuality.HIGH,
        dir:     File
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if already downloaded
            val existing = dao.getById(track.videoId)
            if (existing?.status == DownloadStatus.DONE &&
                File(existing.filePath).exists()) return@launch

            dao.upsert(DownloadEntity(
                videoId    = track.videoId,
                title      = track.title,
                artistName = track.artistName,
                albumTitle = track.albumTitle,
                artworkUrl = track.artworkUrl,
                quality    = quality,
                status     = DownloadStatus.QUEUED
            ))

            try {
                dao.updateProgress(track.videoId, DownloadStatus.DOWNLOADING, 0)

                val stream = repo.getStream(track.videoId) ?: run {
                    dao.updateProgress(track.videoId, DownloadStatus.FAILED, 0)
                    return@launch
                }

                // Create Phantasia Music folder
                val musicDir = File(dir, "Phantasia Music")
                if (!musicDir.exists()) musicDir.mkdirs()

                val fileName = "${track.title} - ${track.artistName}.m4a"
                    .replace(Regex("[^a-zA-Z0-9 ._-]"), "")
                val outFile = File(musicDir, fileName)

                // Stream download with progress
                val connection = URL(stream.streamUrl).openConnection()
                connection.connect()
                val totalBytes = connection.contentLengthLong
                var downloadedBytes = 0L

                connection.getInputStream().use { input ->
                    FileOutputStream(outFile).use { output ->
                        val buffer = ByteArray(8192)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            val progress = if (totalBytes > 0)
                                ((downloadedBytes * 100) / totalBytes).toInt() else 0
                            dao.updateProgress(track.videoId, DownloadStatus.DOWNLOADING, progress)
                        }
                    }
                }

                dao.markDone(track.videoId, DownloadStatus.DONE,
                    outFile.absolutePath, System.currentTimeMillis())

            } catch (e: Exception) {
                dao.updateProgress(track.videoId, DownloadStatus.FAILED, 0)
            }
        }
    }

    fun deleteDownload(download: DownloadEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete file
            File(download.filePath).takeIf { it.exists() }?.delete()
            dao.delete(download)
        }
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadScreen(nav: NavController) {
    val vm: DownloadViewModel = hiltViewModel()
    val context = LocalContext.current
    val completed by vm.completedDownloads.collectAsState()
    val active    by vm.activeDownloads.collectAsState()

    // Permission launcher for Android 10 and below
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handle result */ }

    var selectedTab by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(
            PhantasiaColors.Midnight, PhantasiaColors.GradMid, PhantasiaColors.GradBot
        ))
    )) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.height(56.dp))

            // Title
            Text("Downloads",
                style      = MaterialTheme.typography.headlineSmall,
                color      = PhantasiaColors.OnSurface,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))

            // Tabs
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.07f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                DownloadTab("Downloaded", selected = selectedTab == 0) { selectedTab = 0 }
                DownloadTab("In progress", selected = selectedTab == 1) { selectedTab = 1 }
            }

            Spacer(Modifier.height(8.dp))

            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(150)) },
                label = "download_tab"
            ) { tab ->
                when (tab) {
                    0 -> {
                        // Completed downloads = offline folder
                        if (completed.isEmpty()) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)) {
                                    Text("📥", style = MaterialTheme.typography.displaySmall)
                                    Spacer(Modifier.height(12.dp))
                                    Text("No downloads yet",
                                        color = PhantasiaColors.OnDim,
                                        style = MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.height(8.dp))
                                    Text("Search for a song and tap the download icon",
                                        color = PhantasiaColors.OnHint,
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                                item {
                                    Text(
                                        "Phantasia Music · ${completed.size} songs",
                                        style    = MaterialTheme.typography.labelSmall,
                                        color    = PhantasiaColors.OnDim,
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                                    )
                                }
                                items(completed) { dl ->
                                    DownloadedTrackRow(
                                        download = dl,
                                        onPlay   = { /* play from file */ },
                                        onDelete = { vm.deleteDownload(dl) }
                                    )
                                }
                            }
                        }
                    }
                    1 -> {
                        // Active downloads
                        if (active.isEmpty()) {
                            Box(Modifier.fillMaxSize(), Alignment.Center) {
                                Text("No active downloads",
                                    color = PhantasiaColors.OnDim,
                                    style = MaterialTheme.typography.bodyLarge)
                            }
                        } else {
                            LazyColumn(contentPadding = PaddingValues(bottom = 120.dp)) {
                                items(active) { dl ->
                                    ActiveDownloadRow(download = dl)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadTab(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg by animateColorAsState(
        targetValue   = if (selected) PhantasiaColors.Primary else Color.Transparent,
        animationSpec = tween(250), label = "dl_tab"
    )
    Text(
        text     = label,
        style    = MaterialTheme.typography.labelMedium,
        color    = if (selected) Color.White else PhantasiaColors.OnDim,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
internal fun DownloadedTrackRow(
    download: DownloadEntity,
    onPlay:   () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.07f))) {
            AsyncImage(model = download.artworkUrl, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            // Downloaded indicator
            Box(modifier = Modifier.align(Alignment.BottomEnd).size(16.dp)
                .clip(CircleShape).background(PhantasiaColors.Success)) {
                Icon(Icons.Default.Check, null, tint = Color.White,
                    modifier = Modifier.size(10.dp).align(Alignment.Center))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(download.title, color = PhantasiaColors.OnSurface,
                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${download.artistName} · ${download.quality.label}",
                color = PhantasiaColors.OnDim, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = { showDeleteDialog = true }, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.DeleteOutline, "Delete",
                tint = PhantasiaColors.OnHint, modifier = Modifier.size(18.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor   = PhantasiaColors.SurfaceHigh,
            title            = { Text("Delete download?", color = PhantasiaColors.OnSurface) },
            text             = { Text("\"${download.title}\" will be removed from your device.",
                color = PhantasiaColors.OnDim) },
            confirmButton    = {
                TextButton(onClick = { onDelete(); showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = PhantasiaColors.Error)) {
                    Text("Delete")
                }
            },
            dismissButton    = {
                TextButton(onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = PhantasiaColors.Primary)) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ActiveDownloadRow(download: DownloadEntity) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(52.dp).clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.07f))) {
            AsyncImage(model = download.artworkUrl, contentDescription = null,
                contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(download.title, color = PhantasiaColors.OnSurface,
                style = MaterialTheme.typography.bodyMedium, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress     = { download.progress / 100f },
                modifier     = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(50)),
                color        = PhantasiaColors.Primary,
                trackColor   = Color.White.copy(alpha = 0.1f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                if (download.status == DownloadStatus.DOWNLOADING) "${download.progress}%"
                else download.status.name.lowercase().replaceFirstChar { it.uppercase() },
                color = PhantasiaColors.OnDim, style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
