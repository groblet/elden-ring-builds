package com.example.ytsummary.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Wraps EncryptedSharedPreferences so the Gemini API key is never stored
 * in plaintext on disk. The MasterKey is generated and held in the Android
 * Keystore, so the encryption key itself never leaves secure hardware.
 */
class SecurePrefsManager(context: Context) {

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    fun getApiKey(): String = prefs.getString(KEY_API_KEY, "") ?: ""

    companion object {
        private const val PREFS_FILE_NAME = "ytsummary_secure_prefs"
        private const val KEY_API_KEY = "gemini_api_key"
    }
}
