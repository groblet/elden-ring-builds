package com.example.ytsummary.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ytsummary.R

private val URL_REGEX = Regex("""https?://\S+""")

private const val GEMINI_PACKAGE = "com.google.android.apps.bard"

private const val PROMPT_PREFIX = "Look at this link. " +
    "Write the entire response in GitHub-flavored Markdown (the syntax GitHub renders in " +
    "issues, PRs, and README files): use '#'-style headers, '-' bullet lists, '**bold**', " +
    "and standard GFM tables if needed, with no LaTeX, no HTML tags, and no code fences " +
    "unless quoting actual code. " +
    "If it's an Elden Ring build video, write a build guide formatted as Markdown, using " +
    "relevant emojis in section headers and bullet points, with these sections (skip any " +
    "section the video has no info for): " +
    "## 🧙 Build Overview (name/theme and playstyle in 1-2 sentences), " +
    "## 📊 Stats (a bullet list of all attribute levels: Vigor, Mind, Endurance, Strength, " +
    "Dexterity, Intelligence, Faith, Arcane, and total level), " +
    "## ⚔️ Weapons (each weapon, its upgrade level/affinity, and any talismans), " +
    "## 🛡️ Gear (armor pieces and any other equipped items), " +
    "## ✨ Spells (sorceries/incantations used), " +
    "## 🧪 Buffs (consumables, ashes of war, or other temporary buffs used), " +
    "## 🎯 Tactics (how the build is played: combos, positioning, when to use what). " +
    "Otherwise, write a concise BLUF-style Markdown summary: a one-line " +
    "## 🎯 Bottom Line, then ## 📝 Key Takeaways as a bullet list, using relevant emojis " +
    "throughout. Here is the link: "

/**
 * Share target for any link: forwards the URL plus a content-aware prompt straight to the
 * Gemini app via ACTION_SEND, so it's summarized using the user's own Gemini account (no API
 * key, no API quota) instead of calling the Gemini API directly. The prompt asks Gemini itself
 * to pick the output format (Elden Ring build guide vs. generic BLUF/TL;DR) based on the
 * content, since the app has no way to classify the link before Gemini looks at it.
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
            textStatus.text = "Share a link to this app to summarize it with Gemini."
            return
        }

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val url = sharedText?.let { URL_REGEX.find(it)?.value }

        if (url == null) {
            textStatus.text = "No link found in the shared text."
            return
        }

        sendToGemini(url)
    }

    private fun sendToGemini(url: String) {
        val geminiIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, PROMPT_PREFIX + url)
            setPackage(GEMINI_PACKAGE)
        }

        try {
            startActivity(geminiIntent)
            textStatus.text = "Sent to Gemini: $url"
            finish()
        } catch (e: ActivityNotFoundException) {
            textStatus.text = "Gemini app isn't installed. Install it from the Play Store, then share the link again."
        }
    }
}
