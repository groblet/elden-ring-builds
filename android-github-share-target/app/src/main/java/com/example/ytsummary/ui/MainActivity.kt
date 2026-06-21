package com.example.ytsummary.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ytsummary.R

private val YOUTUBE_URL_REGEX =
    Regex("""https?://(?:www\.|m\.)?(?:youtube\.com/\S*|youtu\.be/\S+)""")

private const val GEMINI_PACKAGE = "com.google.android.apps.bard"

private const val PROMPT_PREFIX = "Watch this Elden Ring build video and write a build guide " +
    "formatted as Markdown, using relevant emojis in section headers and bullet points. " +
    "Include these sections, leaving any out only if the video has no info for them: " +
    "## 🧙 Build Overview (name/theme and playstyle in 1-2 sentences), " +
    "## 📊 Stats (a bullet list of all attribute levels: Vigor, Mind, Endurance, Strength, " +
    "Dexterity, Intelligence, Faith, Arcane, and total level), " +
    "## ⚔️ Weapons (each weapon, its upgrade level/affinity, and any talismans), " +
    "## 🛡️ Gear (armor pieces and any other equipped items), " +
    "## ✨ Spells (sorceries/incantations used), " +
    "## 🧪 Buffs (consumables, ashes of war, or other temporary buffs used), " +
    "## 🎯 Tactics (how the build is played: combos, positioning, when to use what). " +
    "Here is the video: "

/**
 * Share target for YouTube links: forwards the URL plus a summary prompt straight to the
 * Gemini app via ACTION_SEND, so the video is summarized using the user's own Gemini account
 * (no API key, no API quota) instead of calling the Gemini API directly.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var textStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textStatus = findViewById(R.id.textStatus)

        handleIncomingShareIntent(intent)
    }

    private fun handleIncomingShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") {
            textStatus.text = "Share a YouTube link to this app to summarize it with Gemini."
            return
        }

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val youtubeUrl = sharedText?.let { YOUTUBE_URL_REGEX.find(it)?.value }

        if (youtubeUrl == null) {
            textStatus.text = "No YouTube link found in the shared text."
            return
        }

        sendToGemini(youtubeUrl)
    }

    private fun sendToGemini(youtubeUrl: String) {
        val geminiIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, PROMPT_PREFIX + youtubeUrl)
            setPackage(GEMINI_PACKAGE)
        }

        try {
            startActivity(geminiIntent)
            textStatus.text = "Sent to Gemini: $youtubeUrl"
            finish()
        } catch (e: ActivityNotFoundException) {
            textStatus.text = "Gemini app isn't installed. Install it from the Play Store, then share the video again."
        }
    }
}
