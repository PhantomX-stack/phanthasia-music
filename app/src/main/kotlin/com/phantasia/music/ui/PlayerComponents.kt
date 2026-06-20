package com.phantasia.music.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.phantasia.music.network.TrackModel

@Composable
fun TrackCard(track: TrackModel, onClick: () -> Unit) {
    Row(modifier=Modifier.fillMaxWidth().clickable{onClick()}.padding(horizontal=16.dp, vertical=8.dp),
        verticalAlignment=Alignment.CenterVertically) {
        AsyncImage(model=track.artworkUrl, contentDescription=track.title,
            contentScale=ContentScale.Crop, modifier=Modifier.size(48.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(track.title,      style=MaterialTheme.typography.titleMedium, maxLines=1)
            Text(track.artistName, style=MaterialTheme.typography.bodySmall,   maxLines=1)
        }
    }
}

@Composable
fun PlaybackControls(state: PlayerUiState.Playing, onEvent: (PlayerUiEvent) -> Unit) {
    Column(modifier=Modifier.fillMaxWidth().padding(horizontal=24.dp),
        horizontalAlignment=Alignment.CenterHorizontally) {
        Text(state.track.title, style=MaterialTheme.typography.titleLarge)
        Text(state.track.artistName, style=MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Slider(value=state.positionMs.toFloat(),
            onValueChange={ onEvent(PlayerUiEvent.Seek(it.toLong())) },
            valueRange=0f..state.durationMs.toFloat().coerceAtLeast(1f))
        Row(modifier=Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceEvenly,
            verticalAlignment=Alignment.CenterVertically) {
            IconButton(onClick={onEvent(PlayerUiEvent.ToggleShuffle)}) { Text("⇀") }
            IconButton(onClick={onEvent(PlayerUiEvent.SkipPrev)})      { Text("⏮") }
            FilledIconButton(onClick={
                if(state.isPlaying) onEvent(PlayerUiEvent.Pause) else onEvent(PlayerUiEvent.Play)
            }) { Text(if(state.isPlaying) "⏸" else "▶") }
            IconButton(onClick={onEvent(PlayerUiEvent.SkipNext)})    { Text("⏭") }
            IconButton(onClick={onEvent(PlayerUiEvent.CycleRepeat)}) { Text("↻") }
        }
    }
}

@Composable
fun MiniPlayerBar(state: PlayerUiState.Playing, onEvent: (PlayerUiEvent)->Unit, onClick: ()->Unit) {
    Surface(tonalElevation=4.dp) {
        Row(modifier=Modifier.fillMaxWidth().clickable{onClick()}.padding(horizontal=16.dp, vertical=8.dp),
            verticalAlignment=Alignment.CenterVertically) {
            AsyncImage(model=state.track.artworkUrl, contentDescription=null,
                contentScale=ContentScale.Crop, modifier=Modifier.size(40.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(state.track.title,      style=MaterialTheme.typography.bodyMedium, maxLines=1)
                Text(state.track.artistName, style=MaterialTheme.typography.bodySmall,  maxLines=1)
            }
            IconButton(onClick={
                if(state.isPlaying) onEvent(PlayerUiEvent.Pause) else onEvent(PlayerUiEvent.Play)
            }) { Text(if(state.isPlaying) "⏸" else "▶") }
        }
    }
}

@Composable
fun PlayerComponents(state: PlayerUiState.Playing, onEvent: (PlayerUiEvent)->Unit) {
    Column(modifier=Modifier.fillMaxWidth().padding(top=320.dp),
        horizontalAlignment=Alignment.CenterHorizontally) {
        PlaybackControls(state, onEvent)
    }
}
