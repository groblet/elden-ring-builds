package com.example.githubshare.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.githubshare.R
import com.example.githubshare.api.RetrofitClient
import com.example.githubshare.data.GitHubRepository
import com.example.githubshare.data.PushResult
import com.example.githubshare.data.SecurePrefsManager
import kotlinx.coroutines.launch

/**
 * Handles two entry paths:
 *  1. Launched normally from the app drawer -> shows an empty form.
 *  2. Launched via the system Share sheet (ACTION_SEND, text/plain) -> the shared
 *     text is pre-filled into the form so the user only has to confirm and push.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var editUsername: EditText
    private lateinit var editRepo: EditText
    private lateinit var editFilePath: EditText
    private lateinit var editToken: EditText
    private lateinit var editSharedText: EditText
    private lateinit var buttonPush: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textStatus: TextView

    private lateinit var securePrefs: SecurePrefsManager
    private val repository by lazy { GitHubRepository(RetrofitClient.api) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        securePrefs = SecurePrefsManager(applicationContext)

        editUsername = findViewById(R.id.editUsername)
        editRepo = findViewById(R.id.editRepo)
        editFilePath = findViewById(R.id.editFilePath)
        editToken = findViewById(R.id.editToken)
        editSharedText = findViewById(R.id.editSharedText)
        buttonPush = findViewById(R.id.buttonPush)
        progressBar = findViewById(R.id.progressBar)
        textStatus = findViewById(R.id.textStatus)

        restoreSavedCredentials()
        handleIncomingShareIntent(intent)

        buttonPush.setOnClickListener { onPushClicked() }
    }

    /** Pre-fills previously saved connection details so the user doesn't retype them every share. */
    private fun restoreSavedCredentials() {
        editUsername.setText(securePrefs.getUsername())
        editRepo.setText(securePrefs.getRepo())
        editFilePath.setText(securePrefs.getFilePath())
        editToken.setText(securePrefs.getToken())
    }

    /** Extracts the text payload when this Activity was launched from the Share sheet. */
    private fun handleIncomingShareIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrEmpty()) {
                editSharedText.setText(sharedText)
            }
        }
    }

    private fun onPushClicked() {
        val username = editUsername.text.toString().trim()
        val repo = editRepo.text.toString().trim()
        val path = editFilePath.text.toString().trim()
        val token = editToken.text.toString().trim()
        val text = editSharedText.text.toString()

        if (username.isEmpty() || repo.isEmpty() || path.isEmpty() || token.isEmpty()) {
            textStatus.text = "Please fill in username, repository, file path and token."
            return
        }
        if (text.isEmpty()) {
            textStatus.text = "There is no text to push."
            return
        }

        // Persist credentials (encrypted) for next time, then push on a background thread.
        securePrefs.saveCredentials(username, repo, path, token)
        setLoading(true)

        lifecycleScope.launch {
            val result = repository.pushText(
                owner = username,
                repo = repo,
                path = path,
                token = token,
                text = text
            )
            setLoading(false)
            renderResult(result)
        }
    }

    private fun renderResult(result: PushResult) {
        textStatus.text = when (result) {
            is PushResult.Success -> "Pushed successfully" + (result.htmlUrl?.let { ": $it" } ?: ".")
            is PushResult.Failure -> "Failed: ${result.message}"
        }
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        buttonPush.isEnabled = !loading
    }
}
