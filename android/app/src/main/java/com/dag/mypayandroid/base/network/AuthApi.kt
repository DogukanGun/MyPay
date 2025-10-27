package com.dag.mypayandroid.base.network

import org.web3j.crypto.Wallet
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthApi {
    
    @POST("/api/user/check")
    suspend fun checkIfUserExists(
        @Header("Authorization") token: String,
        @Body request: CheckIfUserRequest
    ): Response<CheckIfUserResponse>
    
    @POST("/api/user/register")
    suspend fun registerUser(
        @Header("Authorization") token: String,
        @Body request: RegisterUserRequest
    ): Response<RegisterUserResponse>
    
    @POST("/api/user/session")
    suspend fun newSession(
        @Header("Authorization") token: String,
        @Body request: NewSessionRequest
    ): Response<NewSessionResponse>
    
    @GET("/api/user/profile")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<UserProfileResponse>
}

data class CheckIfUserRequest(
    var device_identifier: String
)

data class CheckIfUserResponse(
    val register: Boolean,
    val device_changed: Boolean
)

data class RegisterUserRequest(
    val device_identifier: String,
    val twitter_id: String,
    val username: String
)

data class NewSessionRequest(
    val device_identifier: String
)

data class Wallets(
    val eth_wallet: WalletInfo?,
    val solana_wallet: WalletInfo?
)

data class NewSessionResponse(
    val uid: String,
    val username: String,
    val twitter_id: String,
    val wallets: Wallets
)

data class WalletInfo(
    val private_key: String,
    val public_address: String
)

data class RegisterUserResponse(
    val uid: String,
    val username: String,
    val twitter_id: String,
    val wallets: Wallets?
)

data class UserProfileResponse(
    val uid: String,
    val username: String,
    val twitter_id: String
)