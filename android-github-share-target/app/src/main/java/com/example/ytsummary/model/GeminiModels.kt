package com.example.ytsummary.model

/**
 * Request body for POST /v1beta/models/{model}:generateContent.
 * Gemini accepts a YouTube URL directly via fileData.fileUri, so no transcript
 * fetching is needed: Gemini watches/reads the video itself.
 */
data class GenerateContentRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    val fileData: FileData? = null
)

data class FileData(
    val fileUri: String
)

data class GenerateContentResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)

data class GeminiErrorResponse(
    val error: GeminiError?
)

data class GeminiError(
    val code: Int?,
    val message: String?,
    val status: String?
)
