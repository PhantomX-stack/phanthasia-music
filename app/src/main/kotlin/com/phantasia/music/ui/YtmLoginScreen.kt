package com.phantasia.music.ui

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YtmLoginScreen(nav: NavController) {
    val vm: AccountViewModel = hiltViewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sign in to YouTube Music",
                    color = PhantasiaColors.OnSurface) },
                navigationIcon = {
                    IconButton(onClick = { nav.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = PhantasiaColors.OnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PhantasiaColors.Surface)
            )
        },
        containerColor = PhantasiaColors.Midnight
    ) { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory  = { context ->
                WebView(context).apply {
                    settings.apply {
                        javaScriptEnabled   = true
                        domStorageEnabled   = true
                        databaseEnabled     = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        setMediaPlaybackRequiresUserGesture(false)
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String?) {
                            // Capture cookie when redirected to YTM — same as Velune
                            if (url?.startsWith("https://music.youtube.com") == true) {
                                val cookie = CookieManager.getInstance().getCookie(url)
                                if (!cookie.isNullOrBlank() && "SAPISID" in cookie) {
                                    // vm.onYtmCookieReceived(cookie) -> not implemented yet in AccountViewModel
                                    nav.navigateUp()
                                }
                            }
                        }
                    }
                    loadUrl(
                        "https://accounts.google.com/ServiceLogin" +
                        "?continue=https%3A%2F%2Fmusic.youtube.com"
                    )
                }
            }
        )
    }
}
