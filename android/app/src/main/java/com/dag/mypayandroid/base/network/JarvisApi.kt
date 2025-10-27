package com.dag.mypayandroid.base.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface JarvisApi {
    
    @POST("/api/agent/ask")
    suspend fun ask(
        @Header("Authorization") token: String,
        @Body request: JarvisAskRequest
    ): Response<JarvisAskResponse>
}

data class JarvisAskRequest(
    val input: String,
    val mentioned_ids: List<String> = emptyList(),
    val reply_to: String? = null
)

data class JarvisAskResponse(
    val output: String
)

data class JarvisErrorResponse(
    val error: String
)

