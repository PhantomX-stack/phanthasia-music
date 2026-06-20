package com.phantasia.music.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.phantasia.music.Route

@Composable
fun HomeScreen(nav: NavController, padding: PaddingValues) {
    LazyColumn(modifier=Modifier.fillMaxSize().padding(padding), contentPadding=PaddingValues(16.dp)) {
        item { Text("Phantasia Music", style=MaterialTheme.typography.titleLarge, modifier=Modifier.padding(bottom=24.dp)) }
        item { OutlinedCard(modifier=Modifier.fillMaxWidth().padding(bottom=8.dp),
            onClick={ nav.navigate(Route.Search.path) }) { Text("Search for music", modifier=Modifier.padding(16.dp)) } }
        item { OutlinedCard(modifier=Modifier.fillMaxWidth(),
            onClick={ nav.navigate(Route.Library.path) }) { Text("Your Library", modifier=Modifier.padding(16.dp)) } }
    }
}
