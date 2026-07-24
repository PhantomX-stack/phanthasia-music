package com.phantasia.music.accounts

import com.phantasia.music.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpotifyImporter @Inject constructor() {
    companion object {
        // Set your Spotify Client ID from developer.spotify.com/dashboard
        // App works without this — Spotify import will be disabled until set
        const val CLIENT_ID = BuildConfig.SPOTIFY_CLIENT_ID
        const val REDIRECT_URI = "phantasia://spotify-callback"
        const val SCOPES = "playlist-read-private user-library-read"
    }
}
