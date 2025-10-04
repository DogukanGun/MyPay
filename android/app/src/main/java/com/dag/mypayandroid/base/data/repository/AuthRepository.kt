package com.dag.mypayandroid.base.data.repository

import com.dag.mypayandroid.base.data.repository.SecureStorage
import com.dag.mypayandroid.base.network.AuthApi
import com.dag.mypayandroid.base.network.CheckIfUserRequest
import com.dag.mypayandroid.base.network.CheckIfUserResponse
import com.dag.mypayandroid.base.network.NewSessionRequest
import com.dag.mypayandroid.base.network.NewSessionResponse
import com.dag.mypayandroid.base.network.RegisterUserRequest
import com.dag.mypayandroid.base.network.RegisterUserResponse
import com.dag.mypayandroid.base.network.UserProfileResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val secureStorage: SecureStorage
) {
    
    suspend fun checkIfUserExists(): Result<CheckIfUserResponse> {
        return try {
            val token = getAuthToken()
            val deviceId = secureStorage.getDeviceIdentifier()
            val request = CheckIfUserRequest(deviceId)
            val response = authApi.checkIfUserExists("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to check user: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun registerUser(twitterId: String, username: String): Result<RegisterUserResponse> {
        return try {
            val token = getAuthToken()
            val deviceId = secureStorage.getDeviceIdentifier()
            val request = RegisterUserRequest(deviceId, twitterId, username)
            val response = authApi.registerUser("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to register user: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getUserProfile(): Result<UserProfileResponse> {
        return try {
            val token = getAuthToken()
            val response = authApi.getUserProfile("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to get profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun newSession(): Result<NewSessionResponse> {
        return try {
            val token = getAuthToken()
            val deviceId = secureStorage.getDeviceIdentifier()
            val request = NewSessionRequest(deviceId)
            val response = authApi.newSession("Bearer $token", request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to create new session: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun saveAuthToken(token: String) {
        secureStorage.saveString(SecureStorage.KEY_USER_TOKEN, token)
    }
    
    private fun getAuthToken(): String {
        return secureStorage.getString(SecureStorage.KEY_USER_TOKEN) 
            ?: throw Exception("No auth token found")
    }
    
    fun hasAuthToken(): Boolean {
        return secureStorage.getString(SecureStorage.KEY_USER_TOKEN) != null
    }
    
    fun clearAuthToken() {
        secureStorage.removeString(SecureStorage.KEY_USER_TOKEN)
    }
    
    fun saveUserInfo(twitterId: String, displayName: String) {
        secureStorage.saveString(SecureStorage.KEY_TWITTER_ID, twitterId)
        secureStorage.saveString(SecureStorage.KEY_DISPLAY_NAME, displayName)
    }
    
    fun getUserInfo(): Pair<String?, String?> {
        val twitterId = secureStorage.getString(SecureStorage.KEY_TWITTER_ID)
        val displayName = secureStorage.getString(SecureStorage.KEY_DISPLAY_NAME)
        return Pair(twitterId, displayName)
    }
    
    fun clearUserInfo() {
        secureStorage.removeString(SecureStorage.KEY_TWITTER_ID)
        secureStorage.removeString(SecureStorage.KEY_DISPLAY_NAME)
    }
}