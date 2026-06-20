package com.example.githubshare.data

import android.util.Base64
import com.example.githubshare.api.GitHubApi
import com.example.githubshare.model.CreateFileRequest
import com.example.githubshare.model.GitHubErrorResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

/** Result wrapper so the UI layer can render success/failure without catching exceptions itself. */
sealed class PushResult {
    data class Success(val htmlUrl: String?) : PushResult()
    data class Failure(val message: String) : PushResult()
}

class GitHubRepository(private val api: GitHubApi) {

    /**
     * Pushes [text] to GitHub at owner/repo/path. Runs entirely on Dispatchers.IO so the
     * caller (an Activity on the main thread) never blocks on network I/O.
     */
    suspend fun pushText(
        owner: String,
        repo: String,
        path: String,
        token: String,
        text: String,
        commitMessage: String = "Add shared content via Android share target"
    ): PushResult = withContext(Dispatchers.IO) {
        try {
            val encodedContent = Base64.encodeToString(text.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            val request = CreateFileRequest(message = commitMessage, content = encodedContent)

            val response = api.putFile(
                authorization = "Bearer $token",
                owner = owner,
                repo = repo,
                path = path,
                body = request
            )

            if (response.isSuccessful) {
                PushResult.Success(response.body()?.content?.html_url)
            } else {
                PushResult.Failure(parseErrorBody(response.code(), response.errorBody()?.string()))
            }
        } catch (e: IOException) {
            // Network failure: no connectivity, DNS failure, timeout, etc.
            PushResult.Failure("Network error: ${e.message ?: "unable to reach GitHub"}")
        } catch (e: Exception) {
            // Anything unexpected (malformed response, serialization failure, etc.)
            PushResult.Failure("Unexpected error: ${e.message ?: e::class.simpleName}")
        }
    }

    /** Maps GitHub's HTTP error codes to human-readable messages. */
    private fun parseErrorBody(httpCode: Int, body: String?): String {
        val githubMessage = body?.let {
            runCatching { Gson().fromJson(it, GitHubErrorResponse::class.java)?.message }.getOrNull()
        }

        val reason = when (httpCode) {
            401 -> "Unauthorized: the Personal Access Token is invalid or expired."
            403 -> "Forbidden: the token lacks permission to write to this repository."
            404 -> "Not found: check the username/repository name and that the token has access."
            409 -> "Conflict: the file already exists; provide its current SHA to update it."
            422 -> "Validation failed: check the file path and content."
            else -> "GitHub API error (HTTP $httpCode)."
        }

        return if (githubMessage != null) "$reason $githubMessage" else reason
    }
}
