package com.phantasia.music.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

data class LrcLine(val timeMs: Long, val text: String)

fun parseLrc(raw: String): List<LrcLine> {
    val rx = Regex("""\[(\d+):(\d+)\.(\d+)](.*)""")
    return raw.lines().mapNotNull { l ->
        val m = rx.find(l.trim()) ?: return@mapNotNull null
        val (min, sec, cs, text) = m.destructured
        LrcLine((min.toLong()*60_000)+(sec.toLong()*1_000)+(cs.toLong()*10), text.trim())
    }.sortedBy { it.timeMs }
}

@Composable
fun LyricsDisplay(lines: List<LrcLine>, positionMs: Long, modifier: Modifier = Modifier) {
    val ls = rememberLazyListState()
    val ai = remember(positionMs) { lines.indexOfLast { it.timeMs <= positionMs }.coerceAtLeast(0) }
    LaunchedEffect(ai) { if (lines.isNotEmpty()) ls.animateScrollToItem(ai, -200) }
    LazyColumn(state = ls, modifier = modifier) {
        items(lines.size) { i ->
            val a = i == ai
            Text(lines[i].text, style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = if (a) FontWeight.Bold else FontWeight.Normal,
                color = if (a) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f)),
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp, horizontal = 24.dp))
        }
    }
}
