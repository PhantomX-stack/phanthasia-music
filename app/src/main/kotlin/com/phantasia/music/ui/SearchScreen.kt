package com.phantasia.music.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.phantasia.music.Route
import com.phantasia.music.network.SearchResultModel

@Composable
fun SearchScreen(nav: NavController, padding: PaddingValues) {
    val vm: SearchViewModel = hiltViewModel()
    val query by vm.query.collectAsState()
    val state by vm.uiState.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Search", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = query, onValueChange = { vm.onEvent(SearchUiEvent.QueryChanged(it)) },
            placeholder = { Text("Search songs, artists…") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        when (val s = state) {
            is SearchUiState.Loading -> CircularProgressIndicator()
            is SearchUiState.Error   -> Text("Error: ${s.message}", color = MaterialTheme.colorScheme.error)
            is SearchUiState.Results -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(s.items.size) { i ->
                    (s.items[i] as? SearchResultModel.TrackResult)?.let { r ->
                        TrackCard(r.track) { nav.navigate(Route.Player.build(r.track.videoId)) }
                    }
                }
            }
            else -> {}
        }
    }
}
