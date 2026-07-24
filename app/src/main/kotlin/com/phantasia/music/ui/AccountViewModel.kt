package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import com.phantasia.music.security.SecurePreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val prefs: SecurePreferenceManager
) : ViewModel() {

    fun onYtmLoginSuccess(
        displayName: String,
        email: String,
        avatarUrl: String,
        accessToken: String,
        refreshToken: String,
        expiresIn: Long
    ) {
        saveAccount("ytm", displayName, email, avatarUrl, accessToken, refreshToken, expiresIn)
    }

    fun onSpotifyLoginSuccess(
        displayName: String,
        email: String,
        avatarUrl: String,
        accessToken: String,
        refreshToken: String,
        expiresIn: Long
    ) {
        saveAccount("spotify", displayName, email, avatarUrl, accessToken, refreshToken, expiresIn)
    }

    private fun saveAccount(
        prefix: String,
        displayName: String,
        email: String,
        avatarUrl: String,
        accessToken: String,
        refreshToken: String,
        expiresIn: Long
    ) {
        prefs.putString("${prefix}_display_name", displayName)
        prefs.putString("${prefix}_email", email)
        prefs.putString("${prefix}_avatar_url", avatarUrl)
        prefs.putString("${prefix}_access_token", accessToken)
        prefs.putString("${prefix}_refresh_token", refreshToken)
        prefs.putString("${prefix}_expires_in", expiresIn.toString())
        prefs.putBoolean("${prefix}_connected", true)
    }
}
