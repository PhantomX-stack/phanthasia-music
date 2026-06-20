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
    LazyColumn(modifier=Modifier.fillMaxSize().padding(padding), contentPadding=PaddingValues(16.dp)) {
        item {
            OutlinedTextField(value=query, onValueChange={ vm.onEvent(SearchUiEvent.QueryChanged(it)) },
                placeholder={ Text("Search songs, artists…") }, modifier=Modifier.fillMaxWidth().padding(bottom=12.dp))
        }
        when (val s = state) {
            is SearchUiState.Loading -> item { CircularProgressIndicator(Modifier.padding(16.dp)) }
            is SearchUiState.Error   -> item { Text("Error: ${s.message}", color=MaterialTheme.colorScheme.error) }
            is SearchUiState.Results -> items(s.items.size) { i ->
                (s.items[i] as? SearchResultModel.TrackResult)?.let { r ->
                    TrackCard(r.track) { nav.navigate(Route.Player.build(r.track.videoId)) }
                }
            }
            else -> {}
        }
    }
}
