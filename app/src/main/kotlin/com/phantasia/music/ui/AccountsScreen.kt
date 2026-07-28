package com.phantasia.music.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.phantasia.music.Route

@Composable
fun AccountsScreen(nav: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Connect accounts", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { nav.navigate(Route.YtmLogin.path) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect YouTube Music")
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { nav.navigate(Route.SpotifyLogin.path) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Connect Spotify")
        }
    }
}
