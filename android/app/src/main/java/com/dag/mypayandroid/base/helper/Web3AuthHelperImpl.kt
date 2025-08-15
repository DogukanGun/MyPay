package com.dag.mypayandroid.base.helper

import android.net.Uri
import android.util.Log
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.LoginParams
import com.web3auth.core.types.UserInfo
import com.web3auth.core.types.Web3AuthResponse
import java.util.concurrent.CompletableFuture

class Web3AuthHelperImpl(
    private val web3Auth: Web3Auth
): Web3AuthHelper {
    
    private val TAG = "Web3AuthHelper"
    private var loginAttempted = false
    
    override suspend fun login(loginParams: LoginParams): CompletableFuture<Web3AuthResponse> {
        Log.d(TAG, "Starting login with params: $loginParams")
        return web3Auth.login(loginParams)
    }

    override suspend fun logOut(): CompletableFuture<Void> {
        Log.d(TAG, "Logging out")
        loginAttempted = false
        return web3Auth.logout()
    }

    override fun getSolanaPrivateKey(): String {
        return try {
            val key = web3Auth.getEd25519PrivKey()
            Log.d(TAG, "Got Solana private key: ${key.take(5)}...")
            key
        } catch (e: Exception) {
            Log.e(TAG, "Error getting Solana private key: ${e.message}", e)
            ""
        }
    }

    override fun getUserInfo(): UserInfo {
        return try {
            val userInfo = web3Auth.getUserInfo()
            Log.d(TAG, "Got user info: ${userInfo?.email}")
            userInfo ?: throw Exception("User info is null")
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user info: ${e.message}", e)
            throw e
        }
    }

    override fun initialize(): CompletableFuture<Void> {
        try {
            return web3Auth.initialize()
        } catch(e: Exception) {
            // Something went wrong
            throw e
        }
    }

    override suspend fun setResultUrl(uri: Uri?) {
        try {
            Log.d(TAG, "Setting result URL: $uri")
            web3Auth.setResultUrl(uri)
        } catch (e: Exception) {
            Log.e(TAG, "Error setting result URL: ${e.message}", e)
        }
    }

    override suspend fun isUserAuthenticated(): Boolean {
        return try {
            val isAuthenticated = web3Auth.getPrivkey().isNotEmpty()
            Log.d(TAG, "User authenticated: $isAuthenticated")
            isAuthenticated
        } catch (e: Exception) {
            Log.e(TAG, "Error checking authentication: ${e.message}", e)
            false
        }
    }

    override fun getWeb3Auth(): Web3Auth {
        return web3Auth
    }

}