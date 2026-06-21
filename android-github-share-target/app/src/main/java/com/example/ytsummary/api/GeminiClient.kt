package com.example.ytsummary.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object GeminiClient {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    val api: GeminiApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            // BASIC avoids logging the request body, which contains the API key's adjacent prompt content
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS) // video understanding can take a while
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(httpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }
}
