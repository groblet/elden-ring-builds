# Link Summary Bot (Android)

A minimal Android app that registers as a system share target for
`text/plain` content. Share any link to it and it hands the link straight to
the Gemini app with a content-aware summary prompt — no API key, no API
quota, just your existing Gemini account.

## Project layout

```
app/src/main/
  AndroidManifest.xml                 -- ACTION_SEND intent-filter
  java/com/example/ytsummary/
    ui/MainActivity.kt                -- handles the share intent, forwards to Gemini
  res/layout/activity_main.xml
```

## How it works

1. Manifest declares an `<intent-filter>` on `MainActivity` for
   `android.intent.action.SEND` with `android:mimeType="text/plain"`, so the
   app appears in the system Share sheet for any text/link content.
2. `MainActivity` pulls the first URL out of the shared text with a regex,
   prepends a prompt, and launches an `ACTION_SEND` intent targeted at the
   Gemini app (package `com.google.android.apps.bard`) with that text as
   `EXTRA_TEXT`.
3. Gemini opens with the prompt pre-filled and summarizes the link using the
   user's own Gemini account — no Gemini API key or quota involved. The
   activity finishes immediately after handing off.

This avoids the Gemini *API*, which on the free tier has a `0` quota for
models like `gemini-2.5-pro` and otherwise requires billing — calling the
Gemini app directly uses whatever plan (e.g. Gemini Pro/Advanced) the user is
already signed into.

### The prompt

Since the app can't classify a link before Gemini actually looks at it, the
prompt asks Gemini to pick the output format itself:

- **Elden Ring build video** -> a Markdown build guide with emoji section
  headers: `🧙 Build Overview`, `📊 Stats`, `⚔️ Weapons`, `🛡️ Gear`,
  `✨ Spells`, `🧪 Buffs`, `🎯 Tactics` (sections with no info in the video are
  skipped).
- **Anything else** (articles, other videos, etc.) -> a concise BLUF-style
  Markdown summary: a one-line `🎯 Bottom Line`, then `📝 Key Takeaways` as a
  bullet list, with relevant emojis throughout.

### Caveat

Gemini's share-intent handler pre-fills the prompt text but may still require
one tap on its own "send" button to actually submit it — Android apps can't
auto-submit input inside another app's UI. This is still a single extra tap
versus typing/pasting the prompt and link manually.

## Usage

1. Make sure the Gemini app is installed and signed in.
2. From any app, share a link to "Link Summary Bot".
3. The Gemini app opens with the link and prompt ready to send.
