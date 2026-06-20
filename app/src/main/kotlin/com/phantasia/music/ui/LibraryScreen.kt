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
    LazyColumn(modifier=Modifier.fillMaxSize().padding(padding), contentPadding=PaddingValues(16.dp)) {
        item { Text("Library", style=MaterialTheme.typography.titleLarge, modifier=Modifier.padding(bottom=16.dp)) }
        items(favs.size) { i -> Text(favs[i].title, modifier=Modifier.padding(vertical=6.dp)); Divider() }
    }
}
