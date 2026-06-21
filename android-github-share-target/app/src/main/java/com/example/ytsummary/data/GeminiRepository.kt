package com.example.ytsummary.data

import com.example.ytsummary.api.GeminiApi
import com.example.ytsummary.model.Content
import com.example.ytsummary.model.FileData
import com.example.ytsummary.model.GeminiErrorResponse
import com.example.ytsummary.model.GenerateContentRequest
import com.example.ytsummary.model.Part
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

sealed class SummaryResult {
    data class Success(val markdown: String) : SummaryResult()
    data class Failure(val message: String) : SummaryResult()
}

private const val MODEL = "gemini-2.5-pro"
private const val PROMPT = "Watch this YouTube video and write a concise summary of it " +
    "formatted as Markdown, with a short title, then key points as bullet lists."

/**
 * Sends the YouTube URL straight to Gemini via fileData.fileUri, so the
 * model watches the video itself instead of us fetching a transcript.
 */
class GeminiRepository(private val api: GeminiApi) {

    private val gson = Gson()

    suspend fun summarizeYoutubeVideo(apiKey: String, youtubeUrl: String): SummaryResult =
        withContext(Dispatchers.IO) {
            try {
                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(
                            parts = listOf(
                                Part(fileData = FileData(fileUri = youtubeUrl)),
                                Part(text = PROMPT)
                            )
                        )
                    )
                )

                val response = api.generateContent(MODEL, apiKey, request)

                if (response.isSuccessful) {
                    val text = response.body()
                        ?.candidates?.firstOrNull()
                        ?.content?.parts?.firstOrNull()
                        ?.text

                    if (text.isNullOrBlank()) {
                        SummaryResult.Failure("Gemini returned an empty summary.")
                    } else {
                        SummaryResult.Success(text)
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    val message = errorBody
                        ?.let { runCatching { gson.fromJson(it, GeminiErrorResponse::class.java) }.getOrNull() }
                        ?.error?.message
                        ?: "Request failed with HTTP ${response.code()}."
                    SummaryResult.Failure(message)
                }
            } catch (e: IOException) {
                SummaryResult.Failure("Network error: ${e.message}")
            } catch (e: Exception) {
                SummaryResult.Failure("Unexpected error: ${e.message}")
            }
        }
}
