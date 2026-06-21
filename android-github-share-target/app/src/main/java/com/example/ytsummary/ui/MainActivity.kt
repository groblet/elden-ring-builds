package com.example.ytsummary.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.ytsummary.R

private val URL_REGEX = Regex("""https?://\S+""")

private const val GEMINI_PACKAGE = "com.google.android.apps.bard"
private const val CLAUDE_PACKAGE = "com.anthropic.claude"

private const val MD_FILE_INSTRUCTION = "The output must be a complete Markdown (.md) file: " +
    "the entire response should be valid Markdown source, with nothing before or after it, " +
    "suitable for saving directly as a .md file. "

private const val PROMPT_PREFIX = "Look at this link. " +
    "Write it in GitHub-flavored Markdown (the syntax GitHub renders in issues, PRs, " +
    "and README files): use '#'-style headers, '-' bullet lists, '**bold**', and standard " +
    "GFM tables if needed, with no LaTeX, no HTML tags, and no code fences unless quoting " +
    "actual code. Do not include any video timestamps (e.g. mm:ss or hh:mm:ss markers, or " +
    "phrases like 'at 3:45') anywhere in the output. " +
    "If it's an Elden Ring build video, act as an Elden Ring build expert and create a " +
    "detailed build guide for the build shown, adhering to these strict formatting rules: " +
    "use British spelling; do not use tables, use bullet points for all lists; do not use " +
    "em dashes; keep the language concise and direct. Use this structure: " +
    "## ⚔️ Build: [Name] " +
    "## 📊 Attributes " +
    "- Vigor: [Number] " +
    "- Mind: [Number] " +
    "- Endurance: [Number] " +
    "- Strength: [Number] " +
    "- Dexterity: [Number] " +
    "- Intelligence: [Number] " +
    "- Faith: [Number] " +
    "- Arcane: [Number] " +
    "## 🗡️ Equipment " +
    "- Right Hand: [Weapon 1, Weapon 2] " +
    "- Left Hand: [Weapon 1, Weapon 2] " +
    "- Armour: [Head, Chest, Arms, Legs] " +
    "## 💍 Talismans " +
    "- [Talisman 1] " +
    "- [Talisman 2] " +
    "- [Talisman 3] " +
    "- [Talisman 4] " +
    "## ✨ Spells & Ash of War " +
    "- Sorceries/Incantations: [List] " +
    "- Ash of War: [Name and Affinity] " +
    "## 🎮 Playstyle Summary " +
    "[Brief explanation of how to execute the build effectively.] " +
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
    private lateinit var checkboxRequireMd: CheckBox
    private lateinit var layoutChooser: View
    private lateinit var buttonGemini: Button
    private lateinit var buttonClaude: Button

    private var pendingUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textStatus = findViewById(R.id.textStatus)
        checkboxRequireMd = findViewById(R.id.checkboxRequireMd)
        layoutChooser = findViewById(R.id.layoutChooser)
        buttonGemini = findViewById(R.id.buttonGemini)
        buttonClaude = findViewById(R.id.buttonClaude)

        buttonGemini.setOnClickListener { pendingUrl?.let { sendToApp(it, GEMINI_PACKAGE, "Gemini") } }
        buttonClaude.setOnClickListener { pendingUrl?.let { sendToApp(it, CLAUDE_PACKAGE, "Claude") } }

        handleIncomingShareIntent(intent)
    }

    private fun handleIncomingShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") {
            textStatus.text = "SHARE A LINK TO SUMMARIZE\nWITH GEMINI OR CLAUDE"
            return
        }

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val url = sharedText?.let { URL_REGEX.find(it)?.value }

        if (url == null) {
            textStatus.text = "NO LINK FOUND IN SHARED TEXT"
            return
        }

        pendingUrl = url
        textStatus.text = "SUMMARIZE:\n$url"
        checkboxRequireMd.visibility = View.VISIBLE
        layoutChooser.visibility = View.VISIBLE
    }

    private fun sendToApp(url: String, packageName: String, label: String) {
        val prompt = if (checkboxRequireMd.isChecked) MD_FILE_INSTRUCTION + PROMPT_PREFIX else PROMPT_PREFIX

        val targetIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, prompt + url)
            setPackage(packageName)
        }

        try {
            startActivity(targetIntent)
            textStatus.text = "SENT TO ${label.uppercase()}:\n$url"
            finish()
        } catch (e: ActivityNotFoundException) {
            textStatus.text = "$label APP ISN'T INSTALLED.\nINSTALL IT, THEN SHARE AGAIN."
        }
    }
}
