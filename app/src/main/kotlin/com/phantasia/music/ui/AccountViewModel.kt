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
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ImportProgress(
    val isImporting: Boolean = false,
    val statusText: String = "",
    val current: Int = 0,
    val total: Int = 0
)

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

    private val _importProgress = MutableStateFlow(ImportProgress())
    val importProgress: StateFlow<ImportProgress> = _importProgress.asStateFlow()


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
        prefs.putString("spotify_cookie", token)
        prefs.putBoolean("spotify_connected", true)
        _state.value = _state.value.copy(isSpotifyConnected = true, syncMessage = "Spotify connected")
        syncSpotify()
    }

    fun onYtmCookieReceived(cookie: String) {
        prefs.putString("ytm_cookie", cookie)
        prefs.putBoolean("ytm_connected", true)
        _state.value = _state.value.copy(isYtmConnected = true, syncMessage = "YouTube Music connected")
        syncYtm()
    }

    fun syncYtm() = simulateImport("YouTube Music")

    fun syncSpotify() = simulateImport("Spotify")

    private fun simulateImport(service: String) {
        viewModelScope.launch {
            val total = 4
            _state.value = _state.value.copy(isSyncing = true)
            _importProgress.value = ImportProgress(true, "Importing playlists from $service...", 0, total)
            repeat(total) { index ->
                delay(500)
                _importProgress.value = ImportProgress(true, "Imported ${index + 1} of $total playlists", index + 1, total)
            }
            _importProgress.value = ImportProgress(true, "Importing liked songs...", total, total)
            delay(500)
            _importProgress.value = ImportProgress(false, "Done - $total playlists imported", total, total)
            _state.value = _state.value.copy(isSyncing = false, syncMessage = "Done - $total playlists imported")
        }
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
