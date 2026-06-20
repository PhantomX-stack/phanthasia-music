package com.phantasia.music.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferenceManager @Inject constructor(@ApplicationContext private val ctx: Context) {
    private val prefs: SharedPreferences by lazy {
        val key = MasterKey.Builder(ctx).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(ctx, "phantasia_secure_prefs", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM)
    }
    fun putString(k: String, v: String)            { prefs.edit().putString(k, v).apply() }
    fun getString(k: String, d: String? = null)    = prefs.getString(k, d)
    fun putBoolean(k: String, v: Boolean)          { prefs.edit().putBoolean(k, v).apply() }
    fun getBoolean(k: String, d: Boolean = false)  = prefs.getBoolean(k, d)
    fun remove(k: String)                          { prefs.edit().remove(k).apply() }
}

@Module @InstallIn(SingletonComponent::class)
object SecurityModule {
    @Provides @Singleton
    fun provideSecurePrefs(@ApplicationContext ctx: Context) = SecurePreferenceManager(ctx)
}
