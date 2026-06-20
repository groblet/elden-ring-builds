package com.example.githubshare.model

/**
 * Request body for PUT /repos/{owner}/{repo}/contents/{path}.
 * GitHub requires file content to be Base64-encoded.
 */
data class CreateFileRequest(
    val message: String,
    val content: String,
    val sha: String? = null // required only when overwriting an existing file
)

/** Minimal subset of the response GitHub returns on success. */
data class CreateFileResponse(
    val content: ContentInfo?
)

data class ContentInfo(
    val path: String?,
    val sha: String?,
    val html_url: String?
)

/** Shape of the response body returned by GitHub on most error conditions. */
data class GitHubErrorResponse(
    val message: String?,
    val documentation_url: String?
)
