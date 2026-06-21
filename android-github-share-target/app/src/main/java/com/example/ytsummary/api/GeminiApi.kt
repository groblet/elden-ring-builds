package com.example.ytsummary.api

import com.example.ytsummary.model.GenerateContentRequest
import com.example.ytsummary.model.GenerateContentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit definition for the Gemini generateContent endpoint.
 * https://ai.google.dev/gemini-api/docs/video-understanding#youtube
 */
interface GeminiApi {

    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContent(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body body: GenerateContentRequest
    ): Response<GenerateContentResponse>
}
