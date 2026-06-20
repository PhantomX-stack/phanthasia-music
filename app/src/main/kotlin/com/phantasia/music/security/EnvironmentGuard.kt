package com.phantasia.music.security

import android.os.Build
import android.os.Debug
import okhttp3.CertificatePinner
import okhttp3.Interceptor
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class IntegrityResult(val passed: Boolean, val reason: String?)

@Singleton
class EnvironmentGuard @Inject constructor() {
    fun checkIntegrity(): IntegrityResult {
        if (isRooted())            return IntegrityResult(false, "Root detected")
        if (isTestKeysBuild())     return IntegrityResult(false, "Test-keys build")
        if (isDebuggerConnected()) return IntegrityResult(false, "Debugger attached")
        return IntegrityResult(true, null)
    }
    private fun isRooted() = listOf(
        "/system/xbin/su", "/system/bin/su", "/sbin/su",
        "/su/bin/su", "/data/local/xbin/su"
    ).any { File(it).exists() }
    private fun isTestKeysBuild()     = Build.TAGS?.contains("test-keys") == true
    private fun isDebuggerConnected() = Debug.isDebuggerConnected() || Debug.waitingForDebugger()

    fun sslPinningInterceptor(): Interceptor {
        val pinner = CertificatePinner.Builder()
            .add("*.youtube.com",     "sha256/YZPgTZ+woNCCCIW3LH2CxQeLzB/1m42QcCTBSdgayjs=")
            .add("*.youtube.com",     "sha256/++MBgDH5WGvL9Bcn5Be30cRcL0f5O+NyoXuWtQdX1aI=")
            .add("music.youtube.com", "sha256/YZPgTZ+woNCCCIW3LH2CxQeLzB/1m42QcCTBSdgayjs=")
            .build()
        return Interceptor { chain ->
            val req = chain.request()
            pinner.check(req.url.host, chain.connection()!!.handshake()!!.peerCertificates())
            chain.proceed(req)
        }
    }
}
