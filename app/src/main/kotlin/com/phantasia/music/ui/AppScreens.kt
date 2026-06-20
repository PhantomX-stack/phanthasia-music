package com.phantasia.music.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.phantasia.music.DataSource
import com.phantasia.music.Route

@Composable
fun HomeScreen(onNavigate: (String) -> Unit, padding: PaddingValues) {
    val items = DataSource.library()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Phantasia Music", style = MaterialTheme.typography.titleLarge)
        }
        item {
            Button(onClick = { onNavigate(Route.Search.path) }, modifier = Modifier.fillMaxWidth()) {
                Text("Search music")
            }
        }
        item {
            Button(onClick = { onNavigate(Route.Library.path) }, modifier = Modifier.fillMaxWidth()) {
                Text("Open library")
            }
        }
        item {
            Text(
                "Recently added",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )
        }
        items(items) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Route.Player.create(item.id)) }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(item.title, style = MaterialTheme.typography.titleMedium)
                    Text(item.subtitle, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

@Composable
fun SearchScreen(onNavigate: (String) -> Unit, padding: PaddingValues) {
    var query by remember { mutableStateOf("") }
    val results = DataSource.search(query)

    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Search", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search tracks") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(results) { item ->
                ListItem(
                    headlineText = { Text(item.title) },
                    supportingText = { Text(item.subtitle) },
                    modifier = Modifier.clickable { onNavigate(Route.Player.create(item.id)) }
                )
            }
        }
    }
}

@Composable
fun LibraryScreen(onNavigate: (String) -> Unit, padding: PaddingValues) {
    val items = DataSource.library()
    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
        Text("Library", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(items) { item ->
                ListItem(
                    headlineText = { Text(item.title) },
                    supportingText = { Text(item.subtitle) },
                    modifier = Modifier.clickable { onNavigate(Route.Player.create(item.id)) }
                )
            }
        }
    }
}

@Composable
fun PlayerScreen(itemId: String, padding: PaddingValues) {
    val item = DataSource.find(itemId)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Now Playing", style = MaterialTheme.typography.titleLarge)
        item?.let {
            Text(it.title, style = MaterialTheme.typography.titleMedium)
            Text(it.subtitle)
        } ?: Text("Track not found")
        Button(onClick = {}) {
            Text("Play")
        }
    }
}
