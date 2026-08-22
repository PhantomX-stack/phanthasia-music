package com.phantasia.music.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CipherEngine @Inject constructor(
    @InnerTubeHttp private val http: HttpClient,
    private val jsRuntime: InnerTubeJsRuntime
) {
    private val playerJsCache = ConcurrentHashMap<String, String>()
    private val sigFunctionCache = ConcurrentHashMap<String, String>()
    private val nFunctionCache = ConcurrentHashMap<String, String>()

    suspend fun resolveStreamUrl(rawUrl: String, playerUrl: String): String {
        if (rawUrl.isBlank()) return rawUrl
        var url = rawUrl
        url = applyNParam(url, playerUrl)
        url = applySParam(url, playerUrl)
        return url
    }

    private suspend fun applyNParam(url: String, playerUrl: String): String {
        val match = Regex("[?&]n=([^&]+)").find(url) ?: return url
        val raw = match.groupValues[1]
        val fnSource = nFunctionCache.getOrPut(playerUrl) {
            runCatching {
                val js = fetchJs(playerUrl)
                val name = findNFunctionName(js) ?: error("n-function name not found")
                extractFunctionSource(js, name) ?: error("n-function source not found")
            }.getOrDefault("")
        }
        if (fnSource.isBlank()) return url
        val decoded = runCatching { jsRuntime.runDecipher(fnSource, raw) }.getOrDefault(raw)
        return if (decoded.isNotBlank() && decoded != raw) url.replace("n=$raw", "n=$decoded") else url
    }

    private suspend fun applySParam(url: String, playerUrl: String): String {
        val match = Regex("[?&]s=([^&]+)").find(url) ?: return url
        val raw = match.groupValues[1]
        val fnSource = sigFunctionCache.getOrPut(playerUrl) {
            runCatching {
                val js = fetchJs(playerUrl)
                val name = findSigFunctionName(js) ?: error("sig-function name not found")
                extractFunctionSource(js, name) ?: error("sig-function source not found")
            }.getOrDefault("")
        }
        if (fnSource.isBlank()) return url
        val decoded = runCatching { jsRuntime.runDecipher(fnSource, raw) }.getOrDefault("")
        return if (decoded.isNotBlank()) {
            url.replace("s=$raw", "").trimEnd('&', '?') + "&sig=$decoded"
        } else url
    }

    private suspend fun fetchJs(playerUrl: String): String =
        playerJsCache.getOrPut(playerUrl) { http.get(playerUrl).bodyAsText() }

    // ── Finding the (renamed-every-few-weeks) function names ──────────────
    // Several fallback patterns tried in order — YouTube rotates between a
    // handful of call-site shapes over time. This part needs occasional
    // maintenance as YouTube changes their code; the DECIPHER LOGIC never
    // does, since we execute their real function instead of re-deriving it.

    private fun findNFunctionName(js: String): String? {
        val patterns = listOf(
            Regex("""\.get\("n"\)\)&&\(b=([a-zA-Z0-9${'$'}_]+)(?:\[\d+\])?\("""),
            Regex("""[a-zA-Z0-9${'$'}_]+\.set\("n",([a-zA-Z0-9${'$'}_]+)\("""),
            Regex(""";([a-zA-Z0-9${'$'}_]{2,4})=function\(a\)\{var b=a\.split\(""\)""")
        )
        for (p in patterns) p.find(js)?.let { return it.groupValues[1] }
        return null
    }

    private fun findSigFunctionName(js: String): String? {
        val patterns = listOf(
            Regex("""\bsig\s*=\s*([a-zA-Z0-9${'$'}_]+)\("""),
            Regex("""["']signature["']\s*,\s*([a-zA-Z0-9${'$'}_]+)\(""")
        )
        for (p in patterns) p.find(js)?.let { return it.groupValues[1] }
        return null
    }

    /**
     * Extracts the COMPLETE function text (declaration through matching
     * closing brace), correctly handling nested { } inside the body —
     * plain regex alone breaks on nested braces, this walks and counts.
     */
    private fun extractFunctionSource(js: String, functionName: String): String? {
        val escapedName = Regex.escape(functionName)
        val startPattern = Regex("(?:function\\s+$escapedName\\s*\\(|$escapedName\\s*=\\s*function\\s*\\()")
        val match = startPattern.find(js) ?: return null

        var i = match.range.last
        while (i < js.length && js[i] != '{') i++
        if (i >= js.length) return null

        var depth = 0
        var end = i
        while (end < js.length) {
            when (js[end]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return js.substring(match.range.first, end + 1)
                }
            }
            end++
        }
        return null
    }
}
