package com.phantasia.music.security

import android.os.Build
import android.os.Debug
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

    private fun isTestKeysBuild() = Build.TAGS?.contains("test-keys") == true
    private fun isDebuggerConnected() = Debug.isDebuggerConnected() || Debug.waitingForDebugger()

    fun standardNetworkInterceptor(): Interceptor {
        return Interceptor { chain ->
            chain.proceed(chain.request())
        }
    }
}

