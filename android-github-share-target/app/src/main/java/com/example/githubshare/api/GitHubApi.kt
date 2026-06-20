package com.example.githubshare.api

import com.example.githubshare.model.CreateFileRequest
import com.example.githubshare.model.CreateFileResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * Retrofit definition for the GitHub "Create or update file contents" endpoint.
 * https://docs.github.com/en/rest/repos/contents#create-or-update-file-contents
 */
interface GitHubApi {

    @PUT("repos/{owner}/{repo}/contents/{path}")
    suspend fun putFile(
        @Header("Authorization") authorization: String, // "Bearer <token>"
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("path", encoded = true) path: String,
        @Body body: CreateFileRequest
    ): Response<CreateFileResponse>
}
