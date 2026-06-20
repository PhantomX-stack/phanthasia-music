package com.phantasia.music.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.phantasia.music.network.TrackModel

@Composable
fun PlayerScreen(videoId: String, nav: NavController, padding: PaddingValues) {
    val vm: PlayerViewModel = hiltViewModel()
    val state by vm.uiState.collectAsState()
    LaunchedEffect(videoId) { vm.onEvent(PlayerUiEvent.PlayTrack(TrackModel(videoId,"Loading…","","","",0))) }
    Box(modifier=Modifier.fillMaxSize().padding(padding), contentAlignment=Alignment.Center) {
        when (val s = state) {
            is PlayerUiState.Idle    -> Text("Ready")
            is PlayerUiState.Loading -> CircularProgressIndicator()
            is PlayerUiState.Playing -> { DynamicBackground(s.track.artworkUrl); PlayerComponents(s, vm::onEvent) }
        }
    }
}
