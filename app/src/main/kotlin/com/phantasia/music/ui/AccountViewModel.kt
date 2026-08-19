package com.phantasia.music.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phantasia.music.accounts.SpotifyImporter
import com.phantasia.music.accounts.YouTubeMusicImporter
import com.phantasia.music.security.SecurePreferenceManager
import com.phantasia.music.storage.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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
    private val prefs: SecurePreferenceManager,
    private val ytmImporter: YouTubeMusicImporter,
    private val spotifyImporter: SpotifyImporter,
    private val playlistDao: PlaylistDao
) : ViewModel() {

    private val _state = MutableStateFlow(AccountUiState())
    val state: StateFlow<AccountUiState> = _state.asStateFlow()

    private val _importProgress = MutableStateFlow(ImportProgress())
    val importProgress: StateFlow<ImportProgress> = _importProgress.asStateFlow()

    init {
        loadAccountState()
        observePlaylists()
    }

    private fun loadAccountState() {
        val ytmConnected = prefs.getBoolean("ytm_connected", false)
        val spotifyConnected = prefs.getBoolean("spotify_connected", false)

        val accountsList = mutableListOf<AccountEntity>()

        if (ytmConnected) {
            accountsList.add(
                AccountEntity(
                    service = AccountService.YOUTUBE_MUSIC,
                    email = prefs.getString("ytm_email", "Google User") ?: "Google User",
                    displayName = prefs.getString("ytm_display_name", "YouTube Music") ?: "YouTube Music",
                    avatarUrl = prefs.getString("ytm_avatar_url", "") ?: "",
                    accessToken = prefs.getString("ytm_access_token", "") ?: "",
                    refreshToken = prefs.getString("ytm_refresh_token", "") ?: "",
                    expiresIn = (prefs.getString("ytm_expires_in", "0") ?: "0").toLongOrNull() ?: 0L
                )
            )
        }

        if (spotifyConnected) {
            accountsList.add(
                AccountEntity(
                    service = AccountService.SPOTIFY,
                    email = prefs.getString("spotify_email", "Spotify User") ?: "Spotify User",
                    displayName = prefs.getString("spotify_display_name", "Spotify") ?: "Spotify",
                    avatarUrl = prefs.getString("spotify_avatar_url", "") ?: "",
                    accessToken = prefs.getString("spotify_access_token", "") ?: "",
                    refreshToken = prefs.getString("spotify_refresh_token", "") ?: "",
                    expiresIn = (prefs.getString("spotify_expires_in", "0") ?: "0").toLongOrNull() ?: 0L
                )
            )
        }

        _state.value = _state.value.copy(
            accounts = accountsList,
            isYtmConnected = ytmConnected,
            isSpotifyConnected = spotifyConnected
        )
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            playlistDao.getAll().collect { playlistsWithSongs ->
                val ytmPls = mutableListOf<ImportedPlaylistEntity>()
                val spotifyPls = mutableListOf<ImportedPlaylistEntity>()

                playlistsWithSongs.forEach { item ->
                    val plEntity = ImportedPlaylistEntity(
                        id = item.playlist.playlistId.toString(),
                        name = item.playlist.name,
                        trackCount = item.songs.size
                    )
                    if (item.playlist.name.contains("YTM", ignoreCase = true) || item.playlist.name.contains("YouTube", ignoreCase = true)) {
                        ytmPls.add(plEntity)
                    } else if (item.playlist.name.contains("Spotify", ignoreCase = true)) {
                        spotifyPls.add(plEntity)
                    }
                }

                _state.value = _state.value.copy(
                    ytmPlaylists = ytmPls,
                    spotifyPlaylists = spotifyPls
                )
            }
        }
    }

    fun onYtmCookieReceived(cookie: String) {
        prefs.putString("ytm_cookie", cookie)
        prefs.putBoolean("ytm_connected", true)
        prefs.putString("ytm_display_name", "Google Account")
        prefs.putString("ytm_email", "Logged in via Google")
        loadAccountState()
        _state.value = _state.value.copy(syncMessage = "YouTube Music connected successfully")
        syncYtm()
    }

    fun onSpotifyTokenReceived(token: String) {
        prefs.putString("spotify_cookie", token)
        prefs.putString("spotify_access_token", token)
        prefs.putBoolean("spotify_connected", true)
        prefs.putString("spotify_display_name", "Spotify Account")
        prefs.putString("spotify_email", "Connected to Spotify")
        loadAccountState()
        _state.value = _state.value.copy(syncMessage = "Spotify connected successfully")
        syncSpotify()
    }

    fun onYtmLoginSuccess(
        displayName: String,
        email: String,
        avatarUrl: String,
        accessToken: String,
        refreshToken: String,
        expiresIn: Long
    ) {
        saveAccount("ytm", displayName, email, avatarUrl, accessToken, refreshToken, expiresIn)
        loadAccountState()
        _state.value = _state.value.copy(syncMessage = "YouTube Music signed in")
        syncYtm()
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
        loadAccountState()
        _state.value = _state.value.copy(syncMessage = "Spotify signed in")
        syncSpotify()
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

    fun importYtmPlaylistUrl(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, error = null)
            _importProgress.value = ImportProgress(true, "Connecting to YouTube Music...", 0, 0)

            val result = ytmImporter.importPlaylistFromUrl(url) { current, total ->
                _importProgress.value = ImportProgress(true, "Importing $current of $total songs...", current, total)
            }

            _importProgress.value = ImportProgress(false, "", 0, 0)
            _state.value = _state.value.copy(isSyncing = false)

            result.onSuccess { res ->
                _state.value = _state.value.copy(
                    syncMessage = "Imported \"${res.playlistName}\" (${res.totalTracks} songs)"
                )
            }.onFailure { err ->
                _state.value = _state.value.copy(error = err.message ?: "Failed to import YTM playlist")
            }
        }
    }

    fun importSpotifyPlaylistUrl(url: String) {
        if (url.isBlank()) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, error = null)
            _importProgress.value = ImportProgress(true, "Fetching Spotify playlist details...", 0, 0)

            val result = spotifyImporter.importPlaylistFromUrl(url) { current, total, trackName ->
                _importProgress.value = ImportProgress(true, "Matching $current/$total: $trackName", current, total)
            }

            _importProgress.value = ImportProgress(false, "", 0, 0)
            _state.value = _state.value.copy(isSyncing = false)

            result.onSuccess { res ->
                _state.value = _state.value.copy(
                    syncMessage = "Imported \"${res.playlistName}\" (${res.matchedTracks}/${res.totalTracks} tracks matched)"
                )
            }.onFailure { err ->
                _state.value = _state.value.copy(error = err.message ?: "Failed to import Spotify playlist")
            }
        }
    }

    fun syncYtm() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isSyncing = true, error = null)
            _importProgress.value = ImportProgress(true, "Syncing YouTube Music liked songs...", 0, 0)

            val result = ytmImporter.importLikedSongs { current, total ->
                _importProgress.value = ImportProgress(true, "Synced $current of $total liked songs", current, total)
            }

            _importProgress.value = ImportProgress(false, "", 0, 0)
            _state.value = _state.value.copy(isSyncing = false)

            result.onSuccess { count ->
                _state.value = _state.value.copy(syncMessage = "Successfully synced $count YouTube Music liked songs")
            }.onFailure { err ->
                _state.value = _state.value.copy(error = err.message ?: "Could not sync YouTube Music liked songs")
            }
        }
    }

    fun syncSpotify() {
        viewModelScope.launch {
            val token = prefs.getString("spotify_access_token", "") ?: ""
            if (token.isBlank()) {
                _state.value = _state.value.copy(
                    syncMessage = "Spotify account connected. You can import playlists via URL anytime."
                )
                return@launch
            }

            _state.value = _state.value.copy(isSyncing = true, error = null)
            _importProgress.value = ImportProgress(true, "Syncing Spotify saved tracks...", 0, 0)

            val result = spotifyImporter.importLikedSongsFromToken(token) { current, total, trackName ->
                _importProgress.value = ImportProgress(true, "Matching $current/$total: $trackName", current, total)
            }

            _importProgress.value = ImportProgress(false, "", 0, 0)
            _state.value = _state.value.copy(isSyncing = false)

            result.onSuccess { count ->
                _state.value = _state.value.copy(syncMessage = "Successfully synced $count Spotify songs to Library")
            }.onFailure { err ->
                _state.value = _state.value.copy(error = err.message ?: "Could not sync Spotify saved tracks")
            }
        }
    }

    fun logoutYtm() {
        prefs.remove("ytm_connected")
        prefs.remove("ytm_cookie")
        prefs.remove("ytm_access_token")
        prefs.remove("ytm_refresh_token")
        prefs.remove("ytm_display_name")
        prefs.remove("ytm_email")
        prefs.remove("ytm_avatar_url")
        loadAccountState()
        _state.value = _state.value.copy(syncMessage = "Disconnected YouTube Music account")
    }

    fun logoutSpotify() {
        prefs.remove("spotify_connected")
        prefs.remove("spotify_cookie")
        prefs.remove("spotify_access_token")
        prefs.remove("spotify_refresh_token")
        prefs.remove("spotify_display_name")
        prefs.remove("spotify_email")
        prefs.remove("spotify_avatar_url")
        loadAccountState()
        _state.value = _state.value.copy(syncMessage = "Disconnected Spotify account")
    }

    fun clearMessage() {
        _state.value = _state.value.copy(syncMessage = null, error = null)
    }
}
