package com.phantasia.music.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@Composable
fun LibraryScreen(nav: NavController, padding: PaddingValues) {
    val vm: LibraryViewModel = hiltViewModel()
    val favs by vm.favourites.collectAsState()
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Library", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text("${favs.size} favourites", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(favs.size) { i -> Text(favs[i].title, modifier = Modifier.padding(vertical = 6.dp)); Divider() }
        }
    }
}
