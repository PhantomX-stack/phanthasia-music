package com.phantasia.music.ui

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpotifyLoginScreen(nav: NavController) {
    val vm: AccountViewModel = hiltViewModel()
    var isCapturing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connect Spotify", color = PhantasiaColors.OnSurface) },
                navigationIcon = {
                    IconButton(onClick = { if (!isCapturing) nav.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = PhantasiaColors.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PhantasiaColors.Surface)
            )
        },
        containerColor = PhantasiaColors.Midnight
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isCapturing -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFF1DB954))
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Connecting to Spotify…",
                                color = PhantasiaColors.OnDim,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                errorMessage != null -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text("Login failed", color = PhantasiaColors.OnSurface, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(errorMessage!!, color = PhantasiaColors.Error, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = { errorMessage = null },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1DB954))
                            ) {
                                Text("Try again")
                            }
                        }
                    }
                }
                else -> {
                    DisposableEffect(Unit) {
                        onDispose {
                            // Clean up WebView resources on exit
                        }
                    }
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    setSupportZoom(true)
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    javaScriptCanOpenWindowsAutomatically = true
                                    userAgentString = "Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                }

                                val webView = this
                                CookieManager.getInstance().apply {
                                    setAcceptCookie(true)
                                    setAcceptThirdPartyCookies(webView, true)
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView, url: String?) {
                                        url ?: return
                                        val cookie = CookieManager.getInstance().getCookie(url) ?: ""
                                        if ("sp_dc" in cookie && !isCapturing) {
                                            val spDc = cookie.split(";")
                                                .map { it.trim() }
                                                .find { it.startsWith("sp_dc=") }
                                                ?.substringAfter("sp_dc=")

                                            if (!spDc.isNullOrBlank()) {
                                                isCapturing = true
                                                vm.onSpotifyTokenReceived(spDc)
                                                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                                    nav.navigateUp()
                                                }, 600)
                                            }
                                        }
                                    }

                                    override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                                        if (request.isForMainFrame) {
                                            errorMessage = "Page error: ${error.description}"
                                        }
                                    }
                                }

                                loadUrl("https://accounts.spotify.com/en/login?continue=https%3A%2F%2Fopen.spotify.com%2F")
                            }
                        },
                        onRelease = { webView ->
                            webView.stopLoading()
                            webView.destroy()
                        }
                    )
                }
            }
        }
    }
}
