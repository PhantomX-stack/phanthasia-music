package com.phantasia.music.ui

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YtmLoginScreen(nav: NavController) {
    val vm: AccountViewModel = hiltViewModel()
    var loadingProgress by remember { mutableIntStateOf(0) }
    var isCapturing     by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign in to YouTube Music", color = PhantasiaColors.OnSurface) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!isCapturing) nav.navigateUp()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = PhantasiaColors.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = PhantasiaColors.Surface)
            )
        },
        containerColor = PhantasiaColors.Midnight
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isCapturing) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = PhantasiaColors.Primary)
                        Spacer(Modifier.height(16.dp))
                        Text("Connecting to YouTube Music…",
                            color = PhantasiaColors.OnDim,
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (errorMessage != null) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)) {
                        Text("Login error", color = PhantasiaColors.OnSurface,
                            style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text(errorMessage!!, color = PhantasiaColors.Error,
                            style = MaterialTheme.typography.bodySmall)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { errorMessage = null }) { Text("Try again") }
                    }
                }
            } else {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory  = { context ->
                        WebView(context).apply {
                            settings.apply {
                                javaScriptEnabled        = true
                                domStorageEnabled        = true
                                databaseEnabled          = true
                                setSupportZoom(true)
                                builtInZoomControls      = true
                                displayZoomControls      = false
                                javaScriptCanOpenWindowsAutomatically = true
                                setMediaPlaybackRequiresUserGesture(false)
                            }

                            // Clear cookies first so user always sees fresh login
                            val webView = this; CookieManager.getInstance().apply {
                                removeAllCookies(null)
                                flush()
                                setAcceptCookie(true)
                                setAcceptThirdPartyCookies(webView, true)
                            }

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView, url: String?) {
                                    url ?: return
                                    // Only capture when we land on music.youtube.com after auth
                                    if (url.startsWith("https://music.youtube.com") &&
                                        !url.contains("accounts.google") &&
                                        !isCapturing) {
                                        val cookie = CookieManager.getInstance().getCookie(url)
                                        if (!cookie.isNullOrBlank() && "SAPISID" in cookie) {
                                            isCapturing = true
                                            vm.onYtmCookieReceived(cookie)
                                            // Navigate back after short delay so user sees it worked
                                            android.os.Handler(android.os.Looper.getMainLooper())
                                                .postDelayed({ nav.navigateUp() }, 600)
                                        }
                                    }
                                }

                                override fun onReceivedError(
                                    view: WebView, request: WebResourceRequest,
                                    error: WebResourceError
                                ) {
                                    if (request.isForMainFrame) {
                                        errorMessage = "Page error: ${error.description}"
                                    }
                                }
                            }

                            loadUrl(
                                "https://accounts.google.com/ServiceLogin" +
                                "?continue=https%3A%2F%2Fmusic.youtube.com%2F" +
                                "&hl=en&followup=https%3A%2F%2Fmusic.youtube.com%2F"
                            )
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
