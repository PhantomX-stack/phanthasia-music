package com.phantasia.music.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PhantasiaMediaService : MediaSessionService() {
    @Inject lateinit var exoPlayer: ExoPlayer
    private var mediaSession: MediaSession? = null

    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(NotificationChannel(
                    "phantasia_playback", "Phantasia Playback",
                    NotificationManager.IMPORTANCE_LOW))
        }
        mediaSession = MediaSession.Builder(this, exoPlayer).setId("PhantasiaSession").build()
    }
    override fun onGetSession(info: MediaSession.ControllerInfo) = mediaSession
    override fun onDestroy() {
        mediaSession?.run { player.release(); release() }
        mediaSession = null; super.onDestroy()
    }
}
