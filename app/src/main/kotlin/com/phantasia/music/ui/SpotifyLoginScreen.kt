package com.phantasia.music.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.util.Base64
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.phantasia.music.accounts.SpotifyCallbackActivity
import com.phantasia.music.accounts.SpotifyImporter
import com.phantasia.music.security.SecurePreferenceManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import java.security.MessageDigest
import java.security.SecureRandom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyLoginScreen(nav: NavController) {
    val vm: AccountViewModel = hiltViewModel()
    val context = LocalContext.current
    var isWaiting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent ?: return
                val displayName = intent.getStringExtra("display_name").orEmpty()
                val email = intent.getStringExtra("email").orEmpty()
                val avatarUrl = intent.getStringExtra("avatar_url").orEmpty()
                val accessToken = intent.getStringExtra("access_token").orEmpty()
                val refreshToken = intent.getStringExtra("refresh_token").orEmpty()
                val expiresIn = intent.getLongExtra("expires_in", 3600L)
                vm.onSpotifyLoginSuccess(displayName, email, avatarUrl, accessToken, refreshToken, expiresIn)
                nav.navigateUp()
            }
        }
        val filter = IntentFilter(SpotifyCallbackActivity.ACTION_SPOTIFY_LOGIN_SUCCESS)
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect Spotify") },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎵", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(24.dp))
            Text(
                "Connect your Spotify account",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Import your playlists and liked songs from Spotify. Tracks will play via YouTube Music. " +
                    "We request read-only access and never modify your account.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(32.dp))

            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            if (isWaiting) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(12.dp))
                Text(
                    "Waiting for Spotify authorization…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Button(
                    onClick = {
                        errorMessage = null
                        isWaiting = true
                        launchSpotifyOAuth(context)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.extraLarge,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                ) {
                    Text(
                        "Connect with Spotify",
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            TextButton(onClick = { nav.navigateUp() }) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun launchSpotifyOAuth(context: Context) {
    val verifier = generateCodeVerifier()
    val challenge = generateCodeChallenge(verifier)
    val securePrefs = EntryPointAccessors.fromApplication(
        context.applicationContext,
        SpotifyLoginEntryPoint::class.java
    ).securePreferenceManager()
    securePrefs.putString(SpotifyCallbackActivity.KEY_CODE_VERIFIER, verifier)

    val authUrl = Uri.Builder()
        .scheme("https")
        .authority("accounts.spotify.com")
        .path("/authorize")
        .appendQueryParameter("client_id", SpotifyImporter.CLIENT_ID)
        .appendQueryParameter("response_type", "code")
        .appendQueryParameter("redirect_uri", SpotifyImporter.REDIRECT_URI)
        .appendQueryParameter("scope", SpotifyImporter.SCOPES)
        .appendQueryParameter("code_challenge_method", "S256")
        .appendQueryParameter("code_challenge", challenge)
        .build()

    CustomTabsIntent.Builder()
        .setShowTitle(true)
        .build()
        .launchUrl(context, authUrl)
}

private fun generateCodeVerifier(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}

private fun generateCodeChallenge(verifier: String): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII))
    return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SpotifyLoginEntryPoint {
    fun securePreferenceManager(): SecurePreferenceManager
}
