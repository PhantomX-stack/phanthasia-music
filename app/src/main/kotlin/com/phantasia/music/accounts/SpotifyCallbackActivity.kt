package com.phantasia.music.accounts

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.phantasia.music.security.SecurePreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Parameters
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import javax.inject.Inject

@AndroidEntryPoint
class SpotifyCallbackActivity : ComponentActivity() {

    companion object {
        const val ACTION_SPOTIFY_LOGIN_SUCCESS = "com.phantasia.music.SPOTIFY_LOGIN_SUCCESS"
        const val KEY_CODE_VERIFIER = "spotify_pkce_verifier"
        private const val TAG = "SpotifyCallbackActivity"
    }

    @Inject lateinit var http: HttpClient
    @Inject lateinit var securePrefs: SecurePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri = intent?.data
        val code = uri?.getQueryParameter("code")
        val error = uri?.getQueryParameter("error")

        when {
            error != null -> {
                Log.e(TAG, "Spotify OAuth error: $error")
                finish()
            }
            code != null -> exchangeCodeForTokens(code)
            else -> {
                Log.e(TAG, "No code or error in callback URI: $uri")
                finish()
            }
        }
    }

    private fun exchangeCodeForTokens(code: String) {
        val verifier = securePrefs.getString(KEY_CODE_VERIFIER) ?: run {
            Log.e(TAG, "PKCE verifier not found in secure prefs")
            finish()
            return
        }

        lifecycleScope.launch {
            runCatching {
                val response = http.submitForm(
                    url = "https://accounts.spotify.com/api/token",
                    formParameters = Parameters.build {
                        append("grant_type", "authorization_code")
                        append("code", code)
                        append("redirect_uri", SpotifyImporter.REDIRECT_URI)
                        append("client_id", SpotifyImporter.CLIENT_ID)
                        append("code_verifier", verifier)
                    }
                ).bodyAsText()

                val json = Json.parseToJsonElement(response).jsonObject
                val accessToken = json["access_token"]?.jsonPrimitive?.content ?: error("No access_token")
                val refreshToken = json["refresh_token"]?.jsonPrimitive?.content.orEmpty()
                val expiresIn = json["expires_in"]?.jsonPrimitive?.longOrNull ?: 3600L

                val profileResp = http.get("https://api.spotify.com/v1/me") {
                    header("Authorization", "Bearer $accessToken")
                }.bodyAsText()
                val profile = Json.parseToJsonElement(profileResp).jsonObject
                val displayName = profile["display_name"]?.jsonPrimitive?.content.orEmpty()
                val email = profile["email"]?.jsonPrimitive?.content.orEmpty()
                val avatarUrl = profile["images"]
                    ?.jsonArray
                    ?.firstOrNull()
                    ?.jsonObject
                    ?.get("url")
                    ?.jsonPrimitive
                    ?.content
                    .orEmpty()

                val intent = android.content.Intent(ACTION_SPOTIFY_LOGIN_SUCCESS).apply {
                    putExtra("display_name", displayName)
                    putExtra("email", email)
                    putExtra("avatar_url", avatarUrl)
                    putExtra("access_token", accessToken)
                    putExtra("refresh_token", refreshToken)
                    putExtra("expires_in", expiresIn)
                    setPackage(packageName)
                }
                sendBroadcast(intent)
                securePrefs.remove(KEY_CODE_VERIFIER)
            }.onFailure { e ->
                Log.e(TAG, "Token exchange failed", e)
            }
            finish()
        }
    }
}
