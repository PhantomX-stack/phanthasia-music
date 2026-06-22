package com.phantasia.music.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

sealed interface CipherOp {
    object Reverse                : CipherOp
    data class Splice(val n: Int) : CipherOp
    data class Swap(val b: Int)   : CipherOp
}

/**
 * Decrypts YouTube/InnerTube stream URLs.
 * n-param  : removes bandwidth throttling
 * s-param  : decodes AES signature cipher from base.js
 * All results cached in-memory by player JS version.
 */
@Singleton
class CipherEngine @Inject constructor(private val http: HttpClient) {

    private val opsCache = ConcurrentHashMap<String, List<CipherOp>>()
    private val nCache   = ConcurrentHashMap<String, String>()

    suspend fun resolveStreamUrl(rawUrl: String, playerUrl: String): String {
        var url = rawUrl
        Regex("[?&]n=([^&]+)").find(url)?.let {
            val raw = it.groupValues[1]
            val dec = decryptN(raw, playerUrl)
            if (dec != raw) url = url.replace("n=$raw", "n=$dec")
        }
        Regex("[?&]s=([^&]+)").find(url)?.let {
            val raw = it.groupValues[1]
            val dec = decryptSig(raw, playerUrl)
            url = url.replace("s=$raw", "").trimEnd('&', '?') + "&sig=$dec"
        }
        return url
    }

    private suspend fun decryptSig(sig: String, playerUrl: String): String {
        if (sig.isBlank()) return sig
        val ops = opsCache.getOrPut(playerUrl) {
            runCatching { parseOps(http.get(playerUrl).bodyAsText()) }.getOrDefault(emptyList())
        }
        return applyOps(sig, ops)
    }

    private suspend fun decryptN(n: String, playerUrl: String): String {
        if (n.isBlank()) return n
        return nCache.getOrPut("$playerUrl::$n") {
            runCatching { extractN(http.get(playerUrl).bodyAsText(), n) }.getOrDefault(n)
        }
    }

    private fun parseOps(js: String): List<CipherOp> {
        val ops  = mutableListOf<CipherOp>()
        val id   = "[a-zA-Z_][a-zA-Z0-9_]*"
        val body = Regex("$id=function[(]a[)][{]a=a[.]split[(][)][)](.+?)return a[.]join[(][)][)][}]")
                       .find(js)?.groupValues?.getOrNull(1) ?: return ops
        val hName = Regex(";($id)[.]")
                        .find(body)?.groupValues?.getOrNull(1) ?: return ops
        val esc   = Regex.escapeReplacement(hName)
        val hBody = Regex("var $esc=[{](.+?)[}];", RegexOption.DOT_MATCHES_ALL)
                        .find(js)?.groupValues?.getOrNull(1) ?: return ops
        val revF = Regex("($id):function[(]a[)][{]a[.]reverse[(][)]").find(hBody)?.groupValues?.getOrNull(1)
        val splF = Regex("($id):function[(]a,b[)][{]a[.]splice[(]0").find(hBody)?.groupValues?.getOrNull(1)
        val swpF = Regex("($id):function[(]a,b[)][{]var c=a").find(hBody)?.groupValues?.getOrNull(1)
        Regex("$esc[.]($id)[(]a,([0-9]+)[)]").findAll(body).forEach { c ->
            val m = c.groupValues[1]; val n = c.groupValues[2].toIntOrNull() ?: return@forEach
            when (m) { revF->ops.add(CipherOp.Reverse); splF->ops.add(CipherOp.Splice(n)); swpF->ops.add(CipherOp.Swap(n)) }
        }
        return ops
    }

    private fun applyOps(sig: String, ops: List<CipherOp>): String {
        val c = sig.toMutableList()
        for (op in ops) when (op) {
            is CipherOp.Reverse -> c.reverse()
            is CipherOp.Splice  -> repeat(op.n.coerceAtMost(c.size)) { c.removeAt(0) }
            is CipherOp.Swap    -> if (c.size > 1) { val i=op.b%c.size; val t=c[0]; c[0]=c[i]; c[i]=t }
        }
        return c.joinToString("")
    }

    private fun extractN(js: String, n: String): String = try {
        val id   = "[a-zA-Z_][a-zA-Z0-9_]*"
        val name = Regex("[.]get[(]\"n\"[)][)]&&[(]b=($id)[([][)]")
                       .find(js)?.groupValues?.getOrNull(1) ?: return n
        val esc  = Regex.escapeReplacement(name)
        val bodyRx = Regex(
            "$esc\s*=\s*function\s*[(]a[)]\s*[{](.+?);\s*return\s+b[.]join[(][)][)]\s*[}]",
            RegexOption.DOT_MATCHES_ALL,
        )
        val funcBody = bodyRx.find(js)?.groupValues?.getOrNull(1) ?: return n
        val eng = javax.script.ScriptEngineManager().getEngineByName("rhino") ?: return n
        (eng.eval("(function(a){" + funcBody + ";return b.join("")})(\'" + n + "\')") as? String) ?: n
    } catch (_: Exception) { n }
}
