package com.dag.mypayandroid.feature.login

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.dag.mypayandroid.base.BaseVM
import com.dag.mypayandroid.base.helper.system.AlertDialogManager
import com.dag.mypayandroid.base.helper.blockchain.WalletManager
import com.dag.mypayandroid.base.helper.system.ActivityHolder
import com.dag.mypayandroid.base.data.repository.AuthRepository
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginVM @Inject constructor(
    private val walletManager: WalletManager,
    private val alertDialogManager: AlertDialogManager,
    private val activityHolder: ActivityHolder,
    private val authRepository: AuthRepository
): BaseVM<LoginVS>(LoginVS.StartLogin()) {
    private val _isLoggedIn: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private suspend fun startHomePage() {
        _isLoggedIn.emit(true)
        _viewState.value = LoginVS.StartHomePage
    }

    private suspend fun startLoginPage() {
        _viewState.value = LoginVS.StartLogin()
        _isLoggedIn.emit(false)
    }

    fun loginWithX() {
        val provider = OAuthProvider.newBuilder("twitter.com")
        provider.addCustomParameter("lang", "en")

        activityHolder.getActivity()?.let { activity ->
            Firebase.auth
                .startActivityForSignInWithProvider(
                    activity,
                    provider.build()
                )
                .addOnCanceledListener {
                    Log.i("LOGIN", "Login is cancelled")
                    _viewState.value = LoginVS.Error("Login cancelled")
                }
                .addOnFailureListener { exception ->
                    Log.e("LOGIN", "Login failed", exception)

                    _viewState.value = LoginVS.Error("Login failed")
                }
                .addOnCompleteListener { result ->
                    if (result.isSuccessful) {
                        val user = result.result.user
                        user?.getIdToken(true)?.addOnCompleteListener { tokenTask ->
                            if (tokenTask.isSuccessful) {
                                val token = tokenTask.result.token
                                if (token != null) {
                                    // Save token and user info
                                    authRepository.saveAuthToken(token)
                                    val twitterId = user.providerData.firstOrNull()?.uid
                                    val displayName = user.displayName
                                    if (twitterId != null && displayName != null) {
                                        authRepository.saveUserInfo(twitterId, displayName)
                                    }
                                    handleUserRegistration(displayName, twitterId)
                                } else {
                                    _viewState.value = LoginVS.Error("Failed to get authentication token")
                                }
                            } else {
                                _viewState.value = LoginVS.Error("Failed to get authentication token")
                            }
                        }
                    }
                }
        } ?: setActivityNotFoundError()
    }

    private fun setActivityNotFoundError() {
        _viewState.value = LoginVS.Error("Activity not found")
    }

    private fun setDataNotReceivedError() {
        _viewState.value = LoginVS.Error("No user data received")
    }

    private fun handleUserRegistration(username: String?, twitterId: String?) {
        if (username == null || twitterId == null) {
            _viewState.value = LoginVS.Error("Missing user information")
            return
        }

        viewModelScope.launch {
            try {
                // First check if user needs registration
                val checkResult = authRepository.checkIfUserExists()
                checkResult.fold(
                    onSuccess = { response ->
                        if (response.register) {
                            // User needs to register
                            registerUser(username, twitterId)
                        } else if (response.device_changed) {
                            changeDevice()
                        } else {
                            // User already exists, proceed to home
                            _viewState.value = LoginVS.StartHomePage
                        }
                    },
                    onFailure = { error ->
                        Log.e("LOGIN", "Failed to check user existence", error)
                        _viewState.value = LoginVS.Error("Failed to verify user: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("LOGIN", "Error during user registration flow", e)
                _viewState.value = LoginVS.Error("Registration failed: ${e.message}")
            }
        }
    }

    private fun changeDevice() {
        viewModelScope.launch {
            try {
                val result = authRepository.newSession()
                result.fold(
                    onSuccess = { response ->
                        Log.d("LOGIN", "New session created successfully!")
                        // Store wallet credentials from new session response
                        storeWalletsFromNewSession(response)
                    },
                    onFailure = { error ->
                        Log.e("LOGIN", "Failed to create new session", error)
                        _viewState.value = LoginVS.Error("Device change failed: ${error.message}")
                    }
                )
            } catch (e: Exception) {
                Log.e("LOGIN", "Error during device change", e)
                _viewState.value = LoginVS.Error("Device change failed: ${e.message}")
            }
        }
    }
    private suspend fun registerUser(username: String, twitterId: String) {
        try {
            val registerResult = authRepository.registerUser(twitterId, username)
            registerResult.fold(
                onSuccess = { response ->
                    Log.d("LOGIN", "User registered successfully!")
                    // Store wallet credentials from API response
                    storeWalletsFromApiResponse(response)
                },
                onFailure = { error ->
                    Log.e("LOGIN", "Failed to register user", error)
                    _viewState.value = LoginVS.Error("Registration failed: ${error.message}")
                }
            )
        } catch (e: Exception) {
            Log.e("LOGIN", "Error during user registration", e)
            _viewState.value = LoginVS.Error("Registration failed: ${e.message}")
        }
    }

    private fun storeWalletsFromApiResponse(response: com.dag.mypayandroid.base.network.RegisterUserResponse) {
        // Store both wallets using WalletManager (accessing from wallets object)
        walletManager.storeWallets(
            ethWallet = response.wallets?.eth_wallet,
            solanaWallet = response.wallets?.solana_wallet
        ) { success ->
            if (success) {
                _viewState.value = LoginVS.StartHomePage
            } else {
                _viewState.value = LoginVS.Error("Failed to secure wallets")
            }
        }
    }

    private fun storeWalletsFromNewSession(response: com.dag.mypayandroid.base.network.NewSessionResponse) {
        // Store both wallets using WalletManager (accessing from wallets object)
        walletManager.storeWallets(
            ethWallet = response.wallets.eth_wallet,
            solanaWallet = response.wallets.solana_wallet
        ) { success ->
            if (success) {
                _viewState.value = LoginVS.StartHomePage
            } else {
                _viewState.value = LoginVS.Error("Failed to secure wallets")
            }
        }
    }


    fun login(){
        // Update UI to loading state
        _viewState.value = LoginVS.StartLogin(isLoading = true)

        viewModelScope.launch {
            try {

            } catch (error: Exception){
                _viewState.value = LoginVS.StartLogin(
                    emailError = "Login failed: ${error.message ?: "Unknown error"}"
                )
                _isLoggedIn.emit(false)
            }
        }
    }


    private fun storeWalletCredentials(privateKey: String, publicKey: String,onSuccess:() -> Unit) {
        if (walletManager.isBiometricAvailable()) {
            walletManager.storeWalletCredentials(
                privateKey = privateKey,
                publicKey = publicKey,
                onSuccess = {
                    onSuccess()
                },
                onError = { errorMessage ->
                }
            )
        } else {
        }
    }

}