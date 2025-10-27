package com.dag.mypayandroid.base.data.repository

import android.util.Log
import com.dag.mypayandroid.base.data.repository.SecureStorage
import com.dag.mypayandroid.base.network.JarvisApi
import com.dag.mypayandroid.base.network.JarvisAskRequest
import com.dag.mypayandroid.base.network.JarvisAskResponse
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JarvisRepository @Inject constructor(
    private val jarvisApi: JarvisApi,
    private val secureStorage: SecureStorage
) {
    
    private val gson = Gson()
    
    companion object {
        private const val TAG = "JarvisRepository"
    }
    
    suspend fun ask(input: String): Result<JarvisAskResponse> {
        return try {
            val token = getAuthToken()
            val request = JarvisAskRequest(
                input = input,
                mentioned_ids = emptyList(),
                reply_to = null
            )
            
            // Debug logging
            Log.d(TAG, "Sending Jarvis request")
            Log.d(TAG, "Input: $input")
            Log.d(TAG, "Token exists: ${token.isNotEmpty()}")
            Log.d(TAG, "Token preview: ${token.take(20)}...")
            
            val response = jarvisApi.ask("Bearer $token", request)
            
            Log.d(TAG, "Response code: ${response.code()}")
            
            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "Success - output: ${response.body()!!.output}")
                Result.success(response.body()!!)
            } else {
                // Log error response body
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "Error response: $errorBody")
                Log.e(TAG, "Error code: ${response.code()}")
                Log.e(TAG, "Error message: ${response.message()}")
                Result.failure(Exception("Failed to process request: ${response.message()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during ask", e)
            Result.failure(e)
        }
    }
    
    private fun getAuthToken(): String {
        return secureStorage.getString(SecureStorage.KEY_USER_TOKEN)
            ?: throw Exception("No auth token found")
    }
}

