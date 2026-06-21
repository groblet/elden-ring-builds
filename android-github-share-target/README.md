# YT Summary Bot (Android)

A minimal Android app that registers as a system share target for
`text/plain` content. Share a YouTube link to it and it summarizes the
video with Gemini, then copies the Markdown summary straight to your
clipboard — no extra taps needed once your API key is saved.

## Project layout

```
app/src/main/
  AndroidManifest.xml                 -- ACTION_SEND intent-filter
  java/com/example/ytsummary/
    ui/MainActivity.kt                -- handles the share intent, settings UI, clipboard copy
    api/GeminiApi.kt                  -- Retrofit interface (generateContent endpoint)
    api/GeminiClient.kt               -- Retrofit/OkHttp singleton
    data/GeminiRepository.kt          -- builds the request, coroutine call, error mapping
    data/SecurePrefsManager.kt        -- EncryptedSharedPreferences wrapper for the API key
    model/GeminiModels.kt             -- request/response DTOs
  res/layout/activity_main.xml
```

## How it works

1. Manifest declares an `<intent-filter>` on `MainActivity` for
   `android.intent.action.SEND` with `android:mimeType="text/plain"`, so the
   app appears in the system Share sheet (e.g. when sharing a video from the
   YouTube app).
2. `MainActivity` pulls the URL out of the shared text with a regex and, if a
   Gemini API key is already saved, immediately calls Gemini — no further
   user interaction required.
3. On success, the Markdown summary is copied to the clipboard via
   `ClipboardManager` and the activity finishes; paste it anywhere.

## Networking

Gemini supports watching a YouTube video directly: the request passes the
video URL as a `fileData.fileUri` part alongside a text prompt, so there's no
separate transcript-fetching step.

```
POST /v1beta/models/{model}:generateContent?key=<API_KEY>
{
  "contents": [{
    "parts": [
      { "fileData": { "fileUri": "<youtube-url>" } },
      { "text": "Watch this YouTube video and write a concise summary..." }
    ]
  }]
}
```

`GeminiRepository.summarizeYoutubeVideo()`:
- Runs the whole call inside `withContext(Dispatchers.IO)` so it never blocks
  the main thread, called from a `lifecycleScope.launch { }` coroutine in the
  Activity.
- Extracts the summary text from
  `candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text`.

### Error handling

- **Network failures** (`IOException` — no connectivity, DNS, timeout) are
  caught and surfaced as `SummaryResult.Failure("Network error: ...")`.
- **HTTP-level failures** (`response.isSuccessful == false`) are not
  exceptions in Retrofit/coroutines — they come back as a normal `Response`,
  so the code parses Gemini's JSON error body (`{"error": {"message": ...}}`)
  to give a specific reason (e.g. invalid API key, quota exceeded).
- **Anything else** (unexpected JSON shape, empty summary) is caught by a
  generic `catch (e: Exception)` so the coroutine never crashes the app.
- The result is a sealed class (`SummaryResult.Success` / `SummaryResult.Failure`)
  rather than thrown exceptions, so the Activity only has to do an exhaustive
  `when` to render the outcome — no `try/catch` needed in the UI layer.

## Storing the Gemini API key securely

Never store the API key in plain `SharedPreferences` — anyone with a rooted
device (or `adb backup` on a debuggable build) can read those values in
clear text. Instead `SecurePrefsManager` uses
[`androidx.security.crypto.EncryptedSharedPreferences`]:

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val prefs = EncryptedSharedPreferences.create(
    context,
    "ytsummary_secure_prefs",
    masterKey,
    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
)
```

- The `MasterKey` is generated and held inside the **Android Keystore**, so
  the AES key used to encrypt preference values never exists in app-readable
  memory or on disk in extractable form.
- Both preference **keys and values** are encrypted (`AES256_SIV` for keys,
  `AES256_GCM` for values), which also prevents an attacker from learning
  which preference keys exist by inspecting the file.
- Add the dependency: `androidx.security:security-crypto:1.1.0-alpha06`.

## Usage

1. Open the app, paste your Gemini API key, tap "Save API Key".
2. From YouTube (or any app), share a video link to "YT Summary Bot".
3. Wait a moment — the Markdown summary lands on your clipboard
   automatically. Paste it wherever you like.
