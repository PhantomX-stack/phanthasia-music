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
    LazyColumn(modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text("Phantasia Music", style = MaterialTheme.typography.titleLarge) }
        item { Button(onClick = { nav.navigate(Route.Search.path) }, modifier = Modifier.fillMaxWidth()) { Text("Search music") } }
        item { Button(onClick = { nav.navigate(Route.Library.path) }, modifier = Modifier.fillMaxWidth()) { Text("Open library") } }
    }
}
