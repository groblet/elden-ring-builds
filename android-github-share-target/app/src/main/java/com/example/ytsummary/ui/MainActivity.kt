package com.example.ytsummary.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.ytsummary.R
import com.example.ytsummary.api.GeminiClient
import com.example.ytsummary.data.GeminiRepository
import com.example.ytsummary.data.SecurePrefsManager
import com.example.ytsummary.data.SummaryResult
import kotlinx.coroutines.launch

private val YOUTUBE_URL_REGEX =
    Regex("""https?://(?:www\.|m\.)?(?:youtube\.com/\S*|youtu\.be/\S+)""")

/**
 * Handles two entry paths:
 *  1. Launched normally from the app drawer -> shows the API key settings form.
 *  2. Launched via the system Share sheet (ACTION_SEND, text/plain) with a YouTube
 *     link -> if an API key is already saved, summarizes the video immediately and
 *     copies the markdown summary to the clipboard with no further taps required.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var editApiKey: EditText
    private lateinit var buttonSave: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var textStatus: TextView

    private lateinit var securePrefs: SecurePrefsManager
    private val repository by lazy { GeminiRepository(GeminiClient.api) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        securePrefs = SecurePrefsManager(applicationContext)

        editApiKey = findViewById(R.id.editApiKey)
        buttonSave = findViewById(R.id.buttonSave)
        progressBar = findViewById(R.id.progressBar)
        textStatus = findViewById(R.id.textStatus)

        editApiKey.setText(securePrefs.getApiKey())
        buttonSave.setOnClickListener { onSaveClicked() }

        handleIncomingShareIntent(intent)
    }

    private fun onSaveClicked() {
        val apiKey = editApiKey.text.toString().trim()
        if (apiKey.isEmpty()) {
            textStatus.text = "Please enter a Gemini API key."
            return
        }
        securePrefs.saveApiKey(apiKey)
        textStatus.text = "API key saved."
    }

    /** Extracts the YouTube link and, if a key is already saved, summarizes right away. */
    private fun handleIncomingShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") return

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return
        val youtubeUrl = YOUTUBE_URL_REGEX.find(sharedText)?.value

        if (youtubeUrl == null) {
            textStatus.text = "No YouTube link found in the shared text."
            return
        }

        val apiKey = securePrefs.getApiKey()
        if (apiKey.isEmpty()) {
            textStatus.text = "Add your Gemini API key below, then share the video again."
            return
        }

        summarize(apiKey, youtubeUrl)
    }

    private fun summarize(apiKey: String, youtubeUrl: String) {
        setLoading(true)
        textStatus.text = "Summarizing $youtubeUrl ..."

        lifecycleScope.launch {
            val result = repository.summarizeYoutubeVideo(apiKey, youtubeUrl)
            setLoading(false)
            when (result) {
                is SummaryResult.Success -> {
                    copyToClipboard(result.markdown)
                    textStatus.text = "Summary copied to clipboard."
                    Toast.makeText(this@MainActivity, "Summary copied to clipboard", Toast.LENGTH_SHORT).show()
                    finish()
                }
                is SummaryResult.Failure -> {
                    textStatus.text = "Failed: ${result.message}"
                }
            }
        }
    }

    private fun copyToClipboard(markdown: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("YouTube summary", markdown))
    }

    private fun setLoading(loading: Boolean) {
        progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        buttonSave.isEnabled = !loading
    }
}
