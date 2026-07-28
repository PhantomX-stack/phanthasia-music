package com.phantasia.music.storage

enum class AccountService {
    YOUTUBE_MUSIC, SPOTIFY
}

data class AccountEntity(
    val service: AccountService,
    val email: String,
    val displayName: String,
    val avatarUrl: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class ImportedPlaylistEntity(
    val id: String,
    val name: String,
    val trackCount: Int
)
