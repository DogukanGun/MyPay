package com.dag.mypayandroid.feature.login

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.viewModelScope
import com.dag.mypayandroid.BuildConfig
import com.dag.mypayandroid.base.BaseVM
import com.dag.mypayandroid.base.data.AlertDialogButton
import com.dag.mypayandroid.base.data.AlertDialogButtonType
import com.dag.mypayandroid.base.data.AlertDialogModel
import com.dag.mypayandroid.base.helper.system.AlertDialogManager
import com.dag.mypayandroid.base.helper.blockchain.WalletManager
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.ExtraLoginOptions
import com.web3auth.core.types.LoginParams
import com.web3auth.core.types.Provider
import com.web3auth.core.types.Web3AuthResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.sol4k.Keypair
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

@HiltViewModel
class LoginVM @Inject constructor(
    private val walletManager: WalletManager,
    private val alertDialogManager: AlertDialogManager
): BaseVM<LoginVS>(LoginVS.StartLogin()) {
    private val _isLoggedIn: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn
    lateinit var solanaKeyPair: Keypair

    private fun isUserLoggedIn(web3Auth: Web3Auth): Boolean {
        var flag = false
        try {
            flag = web3Auth.getPrivkey().isNotEmpty()
        }  catch (e: Exception) { }
        return flag
    }

    private suspend fun startHomePage() {
        _isLoggedIn.emit(true)
        _viewState.value = LoginVS.StartHomePage
    }

    private suspend fun startLoginPage() {
        _viewState.value = LoginVS.StartLogin()
        _isLoggedIn.emit(false)
    }

    fun initialise(web3Auth: Web3Auth) {
        viewModelScope.launch {
            val isUserLoggedIn = isUserLoggedIn(web3Auth)
            if (isUserLoggedIn) {
                startHomePage()
            } else {
                startLoginPage()
            }
        }
    }

    fun solanaPrivateKey(web3Auth: Web3Auth): String {
        return web3Auth.getEd25519PrivKey()
    }

    private fun prepareKeyPair(web3Auth: Web3Auth) {
        solanaKeyPair = Keypair.fromSecretKey(solanaPrivateKey(web3Auth).hexToByteArray())
    }

    @RequiresApi(Build.VERSION_CODES.R)
    fun login(web3Auth: Web3Auth, email: String = ""){
        // First validate email format
        if (email.isEmpty()) {
            _viewState.value = LoginVS.StartLogin(emailError = "Email is required")
            return
        }

        // Update UI to loading state
        _viewState.value = LoginVS.StartLogin(isLoading = true)

        viewModelScope.launch {
            try {
                val selectedLoginProvider = Provider.EMAIL_PASSWORDLESS
                val loginParams = LoginParams(selectedLoginProvider, extraLoginOptions = ExtraLoginOptions(login_hint = email))
                val loginCompletableFuture: CompletableFuture<Web3AuthResponse> =
                    web3Auth.login(loginParams)
                loginCompletableFuture.whenComplete { response, error ->
                    if (error == null) {
                        viewModelScope.launch {
                            _isLoggedIn.emit(true)
                            // Store wallet credentials securely after successful login
                            val publicKey = walletManager.getPublicKey()
                            if (publicKey == null) {
                                alertDialogManager.showAlert(
                                    AlertDialogModel(
                                        title = "Dont have wallet",
                                        message = "You dont have wallet, lets create one",
                                        positiveButton = AlertDialogButton(
                                            text = "Create Wallet",
                                            onClick = {
                                                prepareKeyPair(web3Auth)
                                                storeWalletCredentials(
                                                    solanaKeyPair.secret.toHexString(),
                                                    solanaKeyPair.publicKey.toBase58()
                                                ) {
                                                    _viewState.value = LoginVS.StartHomePage
                                                }
                                            },
                                            type = AlertDialogButtonType.CUSTOM
                                        )
                                    )
                                )
                            } else {
                                _viewState.value = LoginVS.StartHomePage
                            }
                        }
                    } else {
                        Log.e("Web3Auth", error.message ?: "Something went wrong")
                        viewModelScope.launch {
                            _viewState.value = LoginVS.StartLogin(
                                emailError = "Login failed: ${error.message ?: "Unknown error"}"
                            )
                            _isLoggedIn.emit(false)
                        }
                    }
                }
            } catch (error: Exception){
                _viewState.value = LoginVS.StartLogin(
                    emailError = "Login failed: ${error.message ?: "Unknown error"}"
                )
                _isLoggedIn.emit(false)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun storeWalletCredentials(privateKey: String, publicKey: String,onSuccess:() -> Unit) {
        if (walletManager.isBiometricAvailable()) {
            walletManager.storeWalletCredentials(
                privateKey = privateKey,
                publicKey = publicKey,
                onSuccess = {
                    Log.d("HomeVM", "Wallet credentials stored securely")
                    onSuccess()
                },
                onError = { errorMessage ->
                    Log.e("HomeVM", "Failed to store wallet credentials: $errorMessage")
                }
            )
        } else {
            Log.d("HomeVM", "Biometric authentication not available, skipping secure storage")
        }
    }

}