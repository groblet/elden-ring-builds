# GitHub Share Target (Android)

A minimal Android app that registers as a system share target for `text/plain`
content and pushes the shared text to a file in a GitHub repository via the
GitHub Contents API.

## Project layout

```
app/src/main/
  AndroidManifest.xml                 -- ACTION_SEND intent-filter
  java/com/example/githubshare/
    ui/MainActivity.kt                -- handles the incoming share intent + form
    api/GitHubApi.kt                  -- Retrofit interface (PUT contents endpoint)
    api/RetrofitClient.kt             -- Retrofit/OkHttp singleton
    data/GitHubRepository.kt          -- Base64 encoding, coroutine call, error mapping
    data/SecurePrefsManager.kt        -- EncryptedSharedPreferences wrapper for the PAT
    model/GitHubModels.kt             -- request/response DTOs
  res/layout/activity_main.xml
```

## How sharing works

1. Manifest declares an `<intent-filter>` on `MainActivity` for
   `android.intent.action.SEND` with `android:mimeType="text/plain"`, so the
   app appears in the system Share sheet for any app that shares plain text.
2. When launched that way, `intent.getStringExtra(Intent.EXTRA_TEXT)` contains
   the shared text; `MainActivity` pre-fills it into the form.
3. The user supplies (or reuses previously saved) GitHub username, repository,
   file path and Personal Access Token, then taps "Push to GitHub".

## Networking

`GitHubApi.putFile()` maps directly to:

```
PUT /repos/{owner}/{repo}/contents/{path}
Authorization: Bearer <token>
{
  "message": "...",
  "content": "<base64>"
}
```

`GitHubRepository.pushText()`:
- Base64-encodes the shared text with `android.util.Base64` (GitHub requires
  content to be Base64-encoded; this is what `Base64.encodeToString` is for).
- Runs the whole call inside `withContext(Dispatchers.IO)` so it never blocks
  the main thread, called from a `lifecycleScope.launch { }` coroutine in the
  Activity.

### Error handling

- **Network failures** (`IOException` — no connectivity, DNS, timeout) are
  caught and surfaced as `PushResult.Failure("Network error: ...")`.
- **HTTP-level failures** (`response.isSuccessful == false`) are not
  exceptions in Retrofit/coroutines — they come back as a normal `Response`,
  so the code inspects `response.code()` and parses GitHub's JSON error body
  (`{"message": "..."}`) to give a specific reason:
  - `401` → invalid/expired token
  - `403` → token lacks write permission
  - `404` → wrong owner/repo, or token can't see the repo
  - `409` → file already exists (must pass the file's current `sha` to update it)
  - `422` → invalid path/content
- **Anything else** (unexpected JSON shape, serialization errors) is caught by
  a generic `catch (e: Exception)` so the coroutine never crashes the app;
  it's reported as `PushResult.Failure("Unexpected error: ...")`.
- The result is a sealed class (`PushResult.Success` / `PushResult.Failure`)
  rather than thrown exceptions, so the Activity only has to do an exhaustive
  `when` to render the outcome — no `try/catch` needed in the UI layer.

To support overwriting an existing file, fetch the file's current `sha` via
`GET /repos/{owner}/{repo}/contents/{path}` first and pass it in
`CreateFileRequest.sha`; a `409 Conflict` is GitHub's signal that this is
required.

## Storing the Personal Access Token securely

Never store the PAT in plain `SharedPreferences` — anyone with a rooted
device (or `adb backup` on a debuggable build) can read those values in
clear text. Instead `SecurePrefsManager` uses
[`androidx.security.crypto.EncryptedSharedPreferences`]:

```kotlin
val masterKey = MasterKey.Builder(context)
    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
    .build()

val prefs = EncryptedSharedPreferences.create(
    context,
    "github_share_secure_prefs",
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
- Recommended PAT scope: create a **fine-grained personal access token**
  scoped only to the target repository with "Contents: Read and write"
  permission, rather than a classic token with broad `repo` scope.

## Recommended PAT permissions

When generating the token on GitHub, use a fine-grained PAT limited to the
single repository being pushed to, with only the "Contents" repository
permission set to **Read and write**. This limits the blast radius if the
token is ever leaked.
