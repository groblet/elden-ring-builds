package com.example.githubshare.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Wraps EncryptedSharedPreferences so the GitHub Personal Access Token
 * and other connection details are never stored in plaintext on disk.
 * The MasterKey is generated and held in the Android Keystore, so the
 * encryption key itself never leaves secure hardware.
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

    fun saveCredentials(username: String, repo: String, filePath: String, token: String) {
        prefs.edit()
            .putString(KEY_USERNAME, username)
            .putString(KEY_REPO, repo)
            .putString(KEY_FILE_PATH, filePath)
            .putString(KEY_TOKEN, token)
            .apply()
    }

    fun getUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""
    fun getRepo(): String = prefs.getString(KEY_REPO, "") ?: ""
    fun getFilePath(): String = prefs.getString(KEY_FILE_PATH, "") ?: ""
    fun getToken(): String = prefs.getString(KEY_TOKEN, "") ?: ""

    companion object {
        private const val PREFS_FILE_NAME = "github_share_secure_prefs"
        private const val KEY_USERNAME = "github_username"
        private const val KEY_REPO = "github_repo"
        private const val KEY_FILE_PATH = "github_file_path"
        private const val KEY_TOKEN = "github_pat"
    }
}
