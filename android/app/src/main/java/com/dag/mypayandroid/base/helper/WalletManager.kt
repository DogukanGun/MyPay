package com.dag.mypayandroid.base.helper

import android.net.Uri
import android.util.Log
import com.web3auth.core.types.LoginParams
import com.web3auth.core.types.Provider
import com.web3auth.core.types.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.future.await
import org.sol4k.Keypair
import javax.inject.Inject

class WalletManager @Inject constructor(
    private val web3AuthHelper: Web3AuthHelper,
    private val solanaHelper: SolanaHelper
) {
    private val _isLoggedIn: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isAccountLoaded: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isAccountLoaded: StateFlow<Boolean> = _isAccountLoaded

    lateinit var solanaKeyPair: Keypair
    lateinit var balance: String

    fun solanaPrivateKey(): String {
        return web3AuthHelper.getSolanaPrivateKey()
    }

    private fun prepareKeyPair() {
        solanaKeyPair = Keypair.fromSecretKey(solanaPrivateKey().hexToByteArray())
    }

    suspend fun login() {
        val loginParams = LoginParams(loginProvider = Provider.GOOGLE)
        try {
            web3AuthHelper.login(loginParams = loginParams).await()
            prepareKeyPair()
            _isLoggedIn.emit(true)
        } catch (error: Exception) {
            _isLoggedIn.emit(false)
            throw error
        }
    }

    suspend fun initialise() {
        try {
            web3AuthHelper.initialize().await()
        } catch (e: Exception) {
            Log.e("Initialization", e.toString())
        }
        isUserLoggedIn()
    }

    private suspend fun isUserLoggedIn() {
        try {
            val isLoggedIn = web3AuthHelper.isUserAuthenticated()
            if (isLoggedIn) {
                prepareKeyPair()
            }
            _isLoggedIn.emit(isLoggedIn)
        } catch (e: Exception) {
            _isLoggedIn.emit(false)
        }
    }

    suspend fun getBalance() {
        _isAccountLoaded.emit(false)
        try {
            balance = solanaHelper.getBalance(solanaKeyPair.publicKey)
            _isAccountLoaded.emit(true)
        } catch (e: Exception) {
            _isAccountLoaded.emit(false)
            throw e
        }
    }

    suspend fun logOut() {
        try {
            web3AuthHelper.logOut().await()
            _isLoggedIn.emit(false)
        } catch (e: Exception) {
            Log.e("Logout", e.toString())
            _isLoggedIn.emit(true)
        }
    }

    suspend fun setResultUrl(uri: Uri?) {
        web3AuthHelper.setResultUrl(uri)
    }

    suspend fun signAndSendTransaction(onSign: (hash: String?, error: String?) -> Unit) {
        try {
            val signedTransaction = solanaHelper.signAndSendSol(solanaKeyPair)
            onSign(signedTransaction, null)
        } catch (e: Exception) {
            e.localizedMessage?.let { onSign(null, it) }
        }
    }

    suspend fun signTransaction(onSign: (signedTransaction: String?, error: String?) -> Unit) {
        try {
            val signedTransaction = solanaHelper.signSendSol(solanaKeyPair)
            onSign(signedTransaction, null)
        } catch (e: Exception) {
            e.localizedMessage?.let { onSign(null, it) }
        }
    }

    fun userInfo(onAvailable: (userInfo: UserInfo?, error: String?) -> Unit) {
        try {
            val info = web3AuthHelper.getUserInfo()
            onAvailable(info, null)
        } catch (e: Exception) {
            e.localizedMessage?.let { onAvailable(null, it) }
        }
    }
}