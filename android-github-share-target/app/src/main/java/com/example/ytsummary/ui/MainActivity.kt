package com.example.ytsummary.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ytsummary.R

private val URL_REGEX = Regex("""https?://\S+""")

private const val GEMINI_PACKAGE = "com.google.android.apps.bard"
private const val CLAUDE_PACKAGE = "com.anthropic.claude"

private const val PROMPT_PREFIX = "Look at this link. " +
    "The output must be a complete Markdown (.md) file: the entire response should be valid " +
    "Markdown source, with nothing before or after it, suitable for saving directly as a .md " +
    "file. Write it in GitHub-flavored Markdown (the syntax GitHub renders in issues, PRs, " +
    "and README files): use '#'-style headers, '-' bullet lists, '**bold**', and standard " +
    "GFM tables if needed, with no LaTeX, no HTML tags, and no code fences unless quoting " +
    "actual code. Do not include any video timestamps (e.g. mm:ss or hh:mm:ss markers, or " +
    "phrases like 'at 3:45') anywhere in the output. " +
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
 * Share target for any link: lets the user choose Gemini or Claude, then forwards the URL
 * plus a content-aware prompt straight to that app via ACTION_SEND, so it's summarized using
 * the user's own account (no API key, no API quota) instead of calling either API directly.
 * The prompt asks the model itself to pick the output format (Elden Ring build guide vs.
 * generic BLUF/TL;DR) based on the content, since the app has no way to classify the link
 * before the model looks at it.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var textStatus: TextView
    private lateinit var layoutChooser: View
    private lateinit var buttonGemini: Button
    private lateinit var buttonClaude: Button

    private var pendingUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textStatus = findViewById(R.id.textStatus)
        layoutChooser = findViewById(R.id.layoutChooser)
        buttonGemini = findViewById(R.id.buttonGemini)
        buttonClaude = findViewById(R.id.buttonClaude)

        buttonGemini.setOnClickListener { pendingUrl?.let { sendToApp(it, GEMINI_PACKAGE, "Gemini") } }
        buttonClaude.setOnClickListener { pendingUrl?.let { sendToApp(it, CLAUDE_PACKAGE, "Claude") } }

        handleIncomingShareIntent(intent)
    }

    private fun handleIncomingShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") {
            textStatus.text = "Share a link to this app to summarize it with Gemini or Claude."
            return
        }

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val url = sharedText?.let { URL_REGEX.find(it)?.value }

        if (url == null) {
            textStatus.text = "No link found in the shared text."
            return
        }

        pendingUrl = url
        textStatus.text = "Summarize $url with:"
        layoutChooser.visibility = View.VISIBLE
    }

    private fun sendToApp(url: String, packageName: String, label: String) {
        val targetIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, PROMPT_PREFIX + url)
            setPackage(packageName)
        }

        try {
            startActivity(targetIntent)
            textStatus.text = "Sent to $label: $url"
            finish()
        } catch (e: ActivityNotFoundException) {
            textStatus.text = "$label app isn't installed. Install it from the Play Store, then share the link again."
        }
    }
}
