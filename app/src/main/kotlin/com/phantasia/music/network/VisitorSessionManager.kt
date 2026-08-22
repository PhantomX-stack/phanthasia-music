package com.phantasia.music.network

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads music.youtube.com in a hidden WebView once, letting YouTube's own
 * page set whatever session cookies and anti-bot signals a real browser
 * visit would produce — then reuses those on our own InnerTube calls.
 * This is deliberately NOT a from-scratch reimplementation of YouTube's
 * bot-detection internals (an unofficial client can never promise to
 * solve that forever) — it's the same "be a real page load first" trick
 * that already works for YTM login, applied even when nobody is logged in.
 */
@SuppressLint("SetJavaScriptEnabled")
@Singleton
class VisitorSessionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var warmed = false
    private var warmupDeferred: CompletableDeferred<Unit>? = null

    suspend fun ensureWarm() {
        if (warmed) return
        withContext(Dispatchers.Main) {
            val deferred = warmupDeferred ?: CompletableDeferred<Unit>().also { warmupDeferred = it }
            if (!deferred.isCompleted) {
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView, url: String?) {
                            warmed = true
                            deferred.complete(Unit)
                        }
                    }
                    loadUrl("https://music.youtube.com/")
                }
            }
            deferred.await()
        }
    }

    fun getSessionCookie(): String? =
        CookieManager.getInstance().getCookie("https://music.youtube.com")

    /** Call this after a request fails — forces a fresh session next time. */
    fun invalidate() {
        warmed = false
        warmupDeferred = null
    }
}
