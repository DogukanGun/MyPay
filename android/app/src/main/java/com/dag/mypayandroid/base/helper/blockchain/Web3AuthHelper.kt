package com.dag.mypayandroid.base.helper.blockchain

import android.net.Uri
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.LoginParams
import com.web3auth.core.types.UserInfo
import com.web3auth.core.types.Web3AuthResponse
import java.util.concurrent.CompletableFuture

interface Web3AuthHelper {
    suspend fun login(loginParams: LoginParams): CompletableFuture<Web3AuthResponse>
    suspend fun logOut(): CompletableFuture<Void>
    fun getSolanaPrivateKey(): String

    fun getUserInfo(): UserInfo
    fun initialize(): CompletableFuture<Void>

    suspend fun setResultUrl(uri: Uri?): Unit
    suspend fun isUserAuthenticated(): Boolean

    fun getWeb3Auth(): Web3Auth
}