package com.phantasia.music.network

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.MainThread
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * A single hidden WebView that YouTube's own JavaScript runs inside.
 * Used to solve the signature/n-param cipher by RUNNING YouTube's real
 * decipher function instead of hand-reimplementing its logic in Kotlin.
 * Hand-reimplementing breaks every time YouTube renames obfuscated
 * variables; running their own code doesn't, because we never need to
 * understand what the code does — we just execute it verbatim.
 */
@SuppressLint("SetJavaScriptEnabled")
@Singleton
class InnerTubeJsRuntime @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var webView: WebView? = null

    @MainThread
    private fun ensureWebView(): WebView {
        webView?.let { return it }
        val wv = WebView(context).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            loadUrl("https://www.youtube.com/robots.txt")
        }
        webView = wv
        return wv
    }

    private suspend fun evaluate(script: String): String = withContext(Dispatchers.Main) {
        suspendCancellableCoroutine { cont ->
            ensureWebView().evaluateJavascript(script) { result ->
                val unwrapped = result?.trim('"')?.replace("\\\"", "\"").orEmpty()
                if (cont.isActive) cont.resume(unwrapped)
            }
        }
    }

    /**
     * fullFunctionSource is the COMPLETE function text extracted from
     * player.js — e.g. "function abc(a){...}" or "abc=function(a){...}".
     * Runs it as an immediately-invoked expression against the target
     * string and returns the result.
     */
    suspend fun runDecipher(fullFunctionSource: String, input: String): String {
        val escaped = input.replace("\\", "\\\\").replace("'", "\\'")
        return evaluate("($fullFunctionSource)('$escaped')")
    }
}
