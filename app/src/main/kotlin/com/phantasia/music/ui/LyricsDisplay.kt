package com.phantasia.music.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

data class LrcLine(val timeMs: Long, val text: String)

fun parseLrc(raw: String): List<LrcLine> {
    val rx1 = Regex("""\[(\d+):(\d+)\.(\d+)](.*)""")
    val rx2 = Regex("""\[(\d+):(\d+)](.*)""")
    val result = mutableListOf<LrcLine>()
    raw.lines().forEach { l ->
        val trimmed = l.trim()
        val m1 = rx1.find(trimmed)
        if (m1 != null) {
            val (min, sec, cs, text) = m1.destructured
            val csVal = if (cs.length == 3) cs.toLong() else cs.toLong() * 10
            val ms = (min.toLong() * 60_000) + (sec.toLong() * 1_000) + csVal
            if (text.trim().isNotBlank()) {
                result.add(LrcLine(ms, text.trim()))
            }
            return@forEach
        }
        val m2 = rx2.find(trimmed)
        if (m2 != null) {
            val (min, sec, text) = m2.destructured
            val ms = (min.toLong() * 60_000) + (sec.toLong() * 1_000)
            if (text.trim().isNotBlank()) {
                result.add(LrcLine(ms, text.trim()))
            }
        }
    }
    return result.sortedBy { it.timeMs }
}

@Composable
fun LyricsDisplay(
    lines: List<LrcLine>,
    positionMs: Long,
    isSearching: Boolean = false,
    statusMessage: String? = null,
    songTitle: String = "",
    onSeekTo: (Long) -> Unit = {},
    onSearchOnline: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var autoSyncEnabled by remember { mutableStateOf(true) }
    val isDragged by listState.interactionSource.collectIsDraggedAsState()

    // Detect user manual dragging
    LaunchedEffect(isDragged) {
        if (isDragged) {
            autoSyncEnabled = false
        }
    }

    // Active line index calculation
    val activeIndex = remember(positionMs, lines) {
        if (lines.isEmpty()) -1
        else {
            val idx = lines.indexOfLast { it.timeMs <= positionMs }
            if (idx == -1) 0 else idx
        }
    }

    // Auto-scroll when sync is enabled
    LaunchedEffect(activeIndex, autoSyncEnabled) {
        if (autoSyncEnabled && lines.isNotEmpty() && activeIndex >= 0) {
            // Scroll to center
            listState.animateScrollToItem(
                index = activeIndex,
                scrollOffset = -180
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0D0F18).copy(alpha = 0.92f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ── Top Header Controls ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Synced Lyrics Tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (lines.isNotEmpty()) PhantasiaColors.Primary else PhantasiaColors.Secondary)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (lines.isNotEmpty()) "LIVE SYNCED LYRICS" else "LYRICS",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PhantasiaColors.PrimaryGlow,
                        letterSpacing = 1.2.sp
                    )
                }

                // Search Lyrics Online Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSearching) PhantasiaColors.Primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSearching) PhantasiaColors.Primary else Color.White.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.clickable(enabled = !isSearching) { onSearchOnline() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                color = PhantasiaColors.Primary,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search lyrics online",
                                tint = PhantasiaColors.Primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = if (isSearching) "Searching online…" else "Search lyrics online",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // ── Animated Searching Banner (Parallel Background) ───────────
            AnimatedVisibility(
                visible = isSearching,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PhantasiaColors.Primary.copy(alpha = 0.15f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AnimatedLyricsEqualizer(isAnimating = true)
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Searching web for lyrics in background…",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = PhantasiaColors.PrimaryGlow
                            )
                            Text(
                                if (songTitle.isNotBlank()) "Querying \"$songTitle\" on web" else "Music plays uninterrupted",
                                style = MaterialTheme.typography.labelSmall,
                                color = PhantasiaColors.OnDim,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // ── Lyrics Content or Empty State ─────────────────────────────
            if (lines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = PhantasiaColors.Primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.MusicNote,
                                    contentDescription = null,
                                    tint = PhantasiaColors.Primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "No lyrics found yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "Tap below to search the web for synced lyrics for \"$songTitle\" in background",
                            style = MaterialTheme.typography.bodySmall,
                            color = PhantasiaColors.OnDim,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 20.dp)
                        )
                        Spacer(Modifier.height(20.dp))
                        Button(
                            onClick = { onSearchOnline() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PhantasiaColors.Primary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(24.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Search lyrics online", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(top = 28.dp, bottom = 90.dp, start = 16.dp, end = 16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    itemsIndexed(lines) { index, line ->
                        val isActive = index == activeIndex
                        val isNear = kotlin.math.abs(index - activeIndex) <= 1

                        val scale by animateFloatAsState(
                            targetValue = if (isActive) 1.05f else 1.0f,
                            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f)
                        )

                        val alpha by animateFloatAsState(
                            targetValue = when {
                                isActive -> 1.0f
                                isNear -> 0.75f
                                else -> 0.35f
                            },
                            animationSpec = tween(200)
                        )

                        val lineBgModifier = if (isActive) {
                            Modifier.background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        PhantasiaColors.Primary.copy(alpha = 0.18f),
                                        PhantasiaColors.Secondary.copy(alpha = 0.18f)
                                    )
                                )
                            )
                        } else Modifier

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .then(lineBgModifier)
                                .clickable {
                                    onSeekTo(line.timeMs)
                                    autoSyncEnabled = true
                                }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = line.text,
                                style = if (isActive) MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 20.sp,
                                    lineHeight = 28.sp
                                ) else MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 17.sp,
                                    lineHeight = 24.sp
                                ),
                                fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isActive) PhantasiaColors.PrimaryGlow else Color.White.copy(alpha = alpha),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        // ── Floating "Sync lyrics with song" Button ───────────────────────
        AnimatedVisibility(
            visible = !autoSyncEnabled && lines.isNotEmpty(),
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = PhantasiaColors.Primary,
                shadowElevation = 8.dp,
                modifier = Modifier.clickable {
                    autoSyncEnabled = true
                    if (activeIndex >= 0 && lines.isNotEmpty()) {
                        scope.launch {
                            listState.animateScrollToItem(activeIndex, -180)
                        }
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Sync,
                        contentDescription = "Sync lyrics with song",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Sync lyrics with song",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun AnimatedLyricsEqualizer(isAnimating: Boolean, modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "lyric_eq")
    val heights = listOf(
        inf.animateFloat(
            initialValue = 4f, targetValue = 18f,
            animationSpec = infiniteRepeatable(tween(400, easing = LinearEasing), RepeatMode.Reverse),
            label = "h1"
        ),
        inf.animateFloat(
            initialValue = 18f, targetValue = 6f,
            animationSpec = infiniteRepeatable(tween(320, easing = LinearEasing), RepeatMode.Reverse),
            label = "h2"
        ),
        inf.animateFloat(
            initialValue = 8f, targetValue = 22f,
            animationSpec = infiniteRepeatable(tween(460, easing = LinearEasing), RepeatMode.Reverse),
            label = "h3"
        ),
        inf.animateFloat(
            initialValue = 14f, targetValue = 5f,
            animationSpec = infiniteRepeatable(tween(370, easing = LinearEasing), RepeatMode.Reverse),
            label = "h4"
        )
    )
    Row(
        modifier = modifier.height(24.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(if (isAnimating) h.value.dp else 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PhantasiaColors.Primary)
            )
        }
    }
}
