# YT Summary Bot (Android)

A minimal Android app that registers as a system share target for
`text/plain` content. Share a YouTube link to it and it hands the link
straight to the Gemini app with a summary prompt — no API key, no API
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
   app appears in the system Share sheet (e.g. when sharing a video from the
   YouTube app).
2. `MainActivity` pulls the URL out of the shared text with a regex, prepends
   a summary instruction ("Watch this YouTube video and write a concise
   summary of it formatted as Markdown, with a short title, then key points
   as bullet lists: "), and launches an `ACTION_SEND` intent targeted at the
   Gemini app (package `com.google.android.apps.bard`) with that text as
   `EXTRA_TEXT`.
3. Gemini opens with the prompt pre-filled and watches/summarizes the video
   using the user's own Gemini account — no Gemini API key or quota involved.
   The activity finishes immediately after handing off.

This avoids the Gemini *API*, which on the free tier has a `0` quota for
models like `gemini-2.5-pro` and otherwise requires billing — calling the
Gemini app directly uses whatever plan (e.g. Gemini Pro/Advanced) the user is
already signed into.

### Caveat

Gemini's share-intent handler pre-fills the prompt text but may still require
one tap on its own "send" button to actually submit it — Android apps can't
auto-submit input inside another app's UI. This is still a single extra tap
versus typing/pasting the prompt and link manually.

## Usage

1. Make sure the Gemini app is installed and signed in.
2. From YouTube (or any app), share a video link to "YT Summary Bot".
3. The Gemini app opens with the video link and summary prompt ready to send.
