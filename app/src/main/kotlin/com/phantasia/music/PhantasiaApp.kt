package com.phantasia.music

import android.app.Application
import coil.Coil
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.hilt.android.HiltAndroidApp
import java.io.File

@HiltAndroidApp
class PhantasiaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(
            ImageLoader.Builder(this)
                .memoryCache { MemoryCache.Builder(this).maxSizePercent(0.20).build() }
                .diskCache {
                    DiskCache.Builder()
                        .directory(File(cacheDir, "coil_cache"))
                        .maxSizeBytes(256L * 1024 * 1024)
                        .build()
                }
                .crossfade(true)
                .build()
        )
    }
}
