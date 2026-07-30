package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import com.phantasia.music.security.SecurePreferenceManager
import com.phantasia.music.storage.AccountEntity
import com.phantasia.music.storage.AccountService
import com.phantasia.music.storage.ImportedPlaylistEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AccountUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val ytmPlaylists: List<ImportedPlaylistEntity> = emptyList(),
    val spotifyPlaylists: List<ImportedPlaylistEntity> = emptyList(),
    val isSyncing: Boolean = false,
    val syncMessage: String? = null,
    val error: String? = null,
    val isYtmConnected: Boolean = false,
    val isSpotifyConnected: Boolean = false
)

@HiltViewModel
class AccountViewModel @Inject constructor(
    private val prefs: SecurePreferenceManager
) : ViewModel() {

    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

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

    fun onSpotifyTokenReceived(token: String) {
        // Stub implementation
    }

    fun onYtmCookieReceived(cookie: String) {
        // Stub implementation
    }

    fun syncYtm() {
        // Stub implementation
    }

    fun syncSpotify() {
        // Stub implementation
    }

    fun logoutYtm() {
        // Stub implementation
    }

    fun logoutSpotify() {
        // Stub implementation
    }

    fun clearMessage() {
        _state.value = _state.value.copy(syncMessage = null, error = null)
    }
}
