package com.phantasia.music.ui

import android.annotation.SuppressLint
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.security.MessageDigest
import java.security.SecureRandom
import android.util.Base64

// !! IMPORTANT: Replace this with your real Spotify Client ID from
// developer.spotify.com/dashboard
// If blank, the login screen will show setup instructions instead.
private const val SPOTIFY_CLIENT_ID = ""   // <-- paste your client ID here

@SuppressLint("SetJavaScriptEnabled")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyLoginScreen(nav: NavController) {
    val vm: AccountViewModel = hiltViewModel()



    var isExchanging  by remember { mutableStateOf(false) }
    var errorMessage  by remember { mutableStateOf<String?>(null) }
    val codeVerifier  = remember { generateVerifier() }
    val codeChallenge = remember { generateChallenge(codeVerifier) }

    val authUrl = remember {
        Uri.Builder()
            .scheme("https")
            .authority("accounts.spotify.com")
            .path("/authorize")
            .appendQueryParameter("client_id",             SPOTIFY_CLIENT_ID)
            .appendQueryParameter("response_type",         "code")
            .appendQueryParameter("redirect_uri",          "phantasia://spotify-callback")
            .appendQueryParameter("scope",
                "playlist-read-private playlist-read-collaborative user-library-read " +
                "user-read-private user-read-email")
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge",        codeChallenge)
            .build()
            .toString()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect Spotify", color = PhantasiaColors.OnSurface) },
                navigationIcon = {
                    IconButton(onClick = { if (!isExchanging) nav.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PhantasiaColors.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PhantasiaColors.Surface)
            )
        },
        containerColor = PhantasiaColors.Midnight
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isExchanging -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF1DB954))
                            Spacer(Modifier.height(16.dp))
                            Text("Connecting to Spotify…",
                                color = PhantasiaColors.OnDim,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)) {
                            Text("Login failed", color = PhantasiaColors.OnSurface,
                                style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(errorMessage!!, color = PhantasiaColors.Error,
                                style = MaterialTheme.typography.bodySmall,
                                textAlign = TextAlign.Center)
                            Spacer(Modifier.height(16.dp))
                            Button(onClick = { errorMessage = null }) { Text("Try again") }
                        }
                    }
                }
                else -> {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory  = { context ->
                            WebView(context).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled   = true
                                }
                                webViewClient = object : android.webkit.WebViewClient() {
                                    override fun onPageFinished(view: WebView, url: String?) {
                                        url ?: return
                                        if (url.startsWith("https://accounts.spotify.com") || url.startsWith("https://open.spotify.com")) {
                                            val cookie = android.webkit.CookieManager.getInstance().getCookie(url)
                                            if (!cookie.isNullOrBlank() && "sp_dc" in cookie && !isExchanging) {
                                                isExchanging = true
                                                // Extract just the sp_dc token
                                                val spDc = cookie.split("; ").find { it.startsWith("sp_dc=") }?.substringAfter("sp_dc=")
                                                if (spDc != null) {
                                                    vm.onSpotifyTokenReceived(spDc)
                                                    android.os.Handler(android.os.Looper.getMainLooper())
                                                        .postDelayed({ nav.navigateUp() }, 600)
                                                } else {
                                                    isExchanging = false
                                                }
                                            }
                                        }
                                    }
                                }
                                loadUrl(authUrl)
                            }
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SpotifySetupInstructions(nav: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect Spotify", color = PhantasiaColors.OnSurface) },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PhantasiaColors.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PhantasiaColors.Surface)
            )
        },
        containerColor = PhantasiaColors.Midnight
    ) { padding ->
        Column(
            modifier            = Modifier.fillMaxSize().padding(padding).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("🎵", style = MaterialTheme.typography.displayMedium)
            Spacer(Modifier.height(20.dp))
            Text("Set up Spotify",
                style     = MaterialTheme.typography.headlineSmall,
                color     = PhantasiaColors.OnSurface,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center)
            Spacer(Modifier.height(12.dp))

            Card(modifier = Modifier.fillMaxWidth(),
                shape  = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PhantasiaColors.SurfaceCard)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SetupStep("1", "Go to developer.spotify.com/dashboard")
                    SetupStep("2", "Create a new app")
                    SetupStep("3", "Add redirect URI:\nphantasia://spotify-callback")
                    SetupStep("4", "Copy your Client ID")
                    SetupStep("5", "Open SpotifyLoginScreen.kt and paste it into SPOTIFY_CLIENT_ID")
                    SetupStep("6", "Rebuild the app")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetupStep(number: String, text: String) {
    Row(modifier = Modifier.padding(vertical = 6.dp)) {
        Surface(shape = RoundedCornerShape(50),
            color = PhantasiaColors.Primary.copy(alpha = 0.2f)) {
            Text(number, color = PhantasiaColors.Primary,
                style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(text, color = PhantasiaColors.OnSurface,
            style = MaterialTheme.typography.bodyMedium)
    }
}

private fun generateVerifier(): String {
    val bytes = ByteArray(32)
    SecureRandom().nextBytes(bytes)
    return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}

private fun generateChallenge(verifier: String): String {
    val bytes = MessageDigest.getInstance("SHA-256")
        .digest(verifier.toByteArray(Charsets.US_ASCII))
    return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
}
