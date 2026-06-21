# Link Summary Bot (Android)

A minimal Android app that registers as a system share target for
`text/plain` content. Share any link to it, pick Gemini or Claude, and it
hands the link straight to that app with a content-aware summary prompt — no
API key, no API quota, just your existing account.

## Project layout

```
app/src/main/
  AndroidManifest.xml                 -- ACTION_SEND intent-filter
  java/com/example/ytsummary/
    ui/MainActivity.kt                -- handles the share intent, app chooser, forwards the prompt
  res/layout/activity_main.xml
```

## How it works

1. Manifest declares an `<intent-filter>` on `MainActivity` for
   `android.intent.action.SEND` with `android:mimeType="text/plain"`, so the
   app appears in the system Share sheet for any text/link content.
2. `MainActivity` pulls the first URL out of the shared text with a regex and
   shows two buttons: **Send to Gemini** and **Send to Claude**.
3. Tapping one launches an `ACTION_SEND` intent targeted at that app's
   package (`com.google.android.apps.bard` for Gemini, `com.anthropic.claude`
   for Claude) with the prompt + URL as `EXTRA_TEXT`.
4. The chosen app opens with the prompt pre-filled and summarizes the link
   using the user's own account — no API key or quota involved. The activity
   finishes immediately after handing off.

This avoids both apps' *APIs*, which require billing/quota — calling the apps
directly uses whatever plan (e.g. Gemini Pro/Advanced, Claude Pro) the user is
already signed into.

### The prompt

Since the app can't classify a link before the model actually looks at it,
the prompt asks the model to pick the output format itself, and to always
return the result as a complete Markdown (`.md`) file — the entire response
must be valid Markdown source with nothing before or after it:

- **Elden Ring build video** -> a Markdown build guide with emoji section
  headers: `🧙 Build Overview`, `📊 Stats`, `⚔️ Weapons`, `🛡️ Gear`,
  `✨ Spells`, `🧪 Buffs`, `🎯 Tactics` (sections with no info in the video are
  skipped).
- **Anything else** (articles, other videos, etc.) -> a concise BLUF-style
  Markdown summary: a one-line `🎯 Bottom Line`, then `📝 Key Takeaways` as a
  bullet list, with relevant emojis throughout.

The prompt also requires GitHub-flavored Markdown syntax (so it pastes
cleanly into a GitHub issue/PR/README) and explicitly excludes video
timestamps from the output.

### Caveat

Both apps' share-intent handlers pre-fill the prompt text but may still
require one tap on their own "send" button to actually submit it — Android
apps can't auto-submit input inside another app's UI. This is still a single
extra tap versus typing/pasting the prompt and link manually.

## Usage

1. Make sure the Gemini and/or Claude app is installed and signed in.
2. From any app, share a link to "Link Summary Bot".
3. Tap "Send to Gemini" or "Send to Claude".
4. The chosen app opens with the link and prompt ready to send.
