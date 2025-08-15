package com.dag.mypayandroid.feature.home.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri
import com.dag.mypayandroid.base.BaseVM
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.util.PatternsCompat.EMAIL_ADDRESS
import com.dag.mypayandroid.base.notification.NotificationStateManager
import com.dag.mypayandroid.base.helper.ActivityHolder
import com.dag.mypayandroid.base.helper.SolanaHelper
import com.dag.mypayandroid.base.helper.WalletManager
import com.dag.mypayandroid.base.helper.Web3AuthHelper
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.ExtraLoginOptions
import com.web3auth.core.types.LoginParams
import com.web3auth.core.types.Provider
import com.web3auth.core.types.UserInfo
import com.web3auth.core.types.Web3AuthResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.future.await
import org.sol4k.Keypair
import org.web3j.crypto.Credentials
import java.util.concurrent.CompletableFuture
@HiltViewModel
class HomeVM @Inject constructor(
    private val activityHolder: ActivityHolder,
    private val web3AuthHelper: Web3AuthHelper,
    private val solanaHelper: SolanaHelper,
    private val walletManager: WalletManager,
    private val notificationStateManager: NotificationStateManager
) : BaseVM<HomeVS>(initialValue = HomeVS.Companion.initial()) {

    private var _askForPermission = MutableStateFlow(false)
    val askForPermission:StateFlow<Boolean> = _askForPermission

    private val _shouldShowPopup = MutableStateFlow(false)
    val shouldShowPopup: StateFlow<Boolean> = _shouldShowPopup

    private val _isLoggedIn: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _isAccountLoaded: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isAccountLoaded: StateFlow<Boolean> = _isAccountLoaded

    private val _isBiometricAuthInProgress = MutableStateFlow(false)
    val isBiometricAuthInProgress: StateFlow<Boolean> = _isBiometricAuthInProgress

    lateinit var solanaKeyPair: Keypair
    lateinit var balance: String
    private var userInfoData: UserInfo? = null

    init {
        switchToSuccessState()
        checkPermission()
        observeNotificationState()
    }

    private fun switchToSuccessState() {
        _viewState.value = HomeVS.Success()
    }

    fun solanaPrivateKey(): String {
        return web3AuthHelper.getSolanaPrivateKey()
    }

    private fun prepareKeyPair() {
        // Try to get private key from WalletManager first with biometric authentication
        if (walletManager.walletState.value == WalletManager.WalletState.Locked) {
            _isBiometricAuthInProgress.value = true
            walletManager.getPrivateKey(
                onSuccess = { privateKey ->
                    solanaKeyPair = Keypair.fromSecretKey(privateKey.hexToByteArray())
                    _isBiometricAuthInProgress.value = false
                    viewModelScope.launch {
                        updateSuccessState(walletAddress = solanaKeyPair.publicKey.toBase58())
                        fetchUserDataAfterAuth()
                    }
                },
                onError = { errorMessage ->
                    Log.e("HomeVM", "Biometric authentication failed: $errorMessage")
                    _isBiometricAuthInProgress.value = false
                    // Fall back to Web3Auth if biometric authentication fails
                    solanaKeyPair = Keypair.fromSecretKey(solanaPrivateKey().hexToByteArray())
                    viewModelScope.launch {
                        updateSuccessState(walletAddress = solanaKeyPair.publicKey.toBase58())
                        fetchUserDataAfterAuth()
                    }
                }
            )
        } else {
            // If no securely stored key, use Web3Auth
            solanaKeyPair = Keypair.fromSecretKey(solanaPrivateKey().hexToByteArray())
            viewModelScope.launch {
                updateSuccessState(walletAddress = solanaKeyPair.publicKey.toBase58())
                fetchUserDataAfterAuth()
            }
        }
    }

    private fun fetchUserDataAfterAuth() {
        fetchUserInfo()
        getBalance()
    }

    private fun updateSuccessState(
        walletAddress: String? = null,
        shouldShowPopup: Boolean? = null,
        balance: String? = null,
        userInfo: UserInfo? = null,
        isLoadingBalance: Boolean? = null
    ) {
        val currentState = _viewState.value
        if (currentState is HomeVS.Success) {
            _viewState.value = currentState.copy(
                walletAddress = walletAddress ?: currentState.walletAddress,
                shouldShowPopup = shouldShowPopup ?: currentState.shouldShowPopup,
                balance = balance ?: currentState.balance,
                userInfo = userInfo ?: currentState.userInfo,
                isLoadingBalance = isLoadingBalance ?: currentState.isLoadingBalance
            )
        } else {
            _viewState.value = HomeVS.Success(
                walletAddress = walletAddress,
                shouldShowPopup = shouldShowPopup ?: false,
                balance = balance,
                userInfo = userInfo,
                isLoadingBalance = isLoadingBalance ?: false
            )
        }
    }

    private fun fetchUserInfo() {
        try {
            val info = web3AuthHelper.getUserInfo()
            userInfoData = info
            updateSuccessState(userInfo = info)
        } catch (e: Exception) {
            Log.e("HomeVM", "Error fetching user info: ${e.message}", e)
        }
    }

    fun login(web3Auth: Web3Auth, email: String = ""){
        // First validate email format
        if (email.isEmpty()) {
            _viewState.value = HomeVS.LoginRequired(emailError = "Email is required")
            return
        }
        
        // Update UI to loading state
        _viewState.value = HomeVS.LoginRequired(isLoading = true)
        
        viewModelScope.launch {
            try {
                val selectedLoginProvider = Provider.EMAIL_PASSWORDLESS
                val loginParams = LoginParams(selectedLoginProvider, extraLoginOptions = ExtraLoginOptions(login_hint = email))
                val loginCompletableFuture: CompletableFuture<Web3AuthResponse> =
                    web3Auth.login(loginParams)
                // IMP END - Login

                loginCompletableFuture.whenComplete { response, error ->
                    if (error == null) {
                        val credentials = Credentials.create(web3Auth.getPrivkey())
                        Log.d("Web3Auth", "Login successful, credentials address: ${credentials.address}")
                        
                        viewModelScope.launch {
                            updateSuccessState(walletAddress = credentials.address, isLoadingBalance = true)
                            _isLoggedIn.emit(true)
                            
                            // Store wallet credentials securely after successful login
                            storeWalletCredentials(web3Auth.getPrivkey(), credentials.address)
                            
                            // Initialize the Solana keypair
                            solanaKeyPair = Keypair.fromSecretKey(solanaPrivateKey().hexToByteArray())
                            
                            // Fetch user data after authentication
                            fetchUserDataAfterAuth()
                        }
                    } else {
                        Log.e("Web3Auth", error.message ?: "Something went wrong")
                        viewModelScope.launch {
                            _viewState.value = HomeVS.LoginRequired(
                                emailError = "Login failed: ${error.message ?: "Unknown error"}"
                            )
                            _isLoggedIn.emit(false)
                        }
                    }
                }
            } catch (error: Exception){
                _viewState.value = HomeVS.LoginRequired(
                    emailError = "Login failed: ${error.message ?: "Unknown error"}"
                )
                _isLoggedIn.emit(false)
            }
        }
    }
    
    private fun storeWalletCredentials(privateKey: String, publicKey: String) {
        if (walletManager.isBiometricAvailable()) {
            walletManager.storeWalletCredentials(
                privateKey = privateKey,
                publicKey = publicKey,
                onSuccess = {
                    Log.d("HomeVM", "Wallet credentials stored securely")
                },
                onError = { errorMessage ->
                    Log.e("HomeVM", "Failed to store wallet credentials: $errorMessage")
                }
            )
        } else {
            Log.d("HomeVM", "Biometric authentication not available, skipping secure storage")
        }
    }
    
    fun resetToLoginState() {
        _viewState.value = HomeVS.LoginRequired()
    }

    fun initialise() {
        viewModelScope.launch {
            val isUserLoggedIn = isUserLoggedIn()

            if (isUserLoggedIn) {
                try {
                    prepareKeyPair()
                    _isLoggedIn.emit(true)
                } catch (e: Exception) {
                    Log.e("HomeVM", "Error preparing keypair: ${e.message}", e)
                    _viewState.value = HomeVS.LoginRequired()
                    _isLoggedIn.emit(false)
                }
            } else {

                _viewState.value = HomeVS.LoginRequired()
                _isLoggedIn.emit(false)
            }
        }
    }

    private suspend fun isUserLoggedIn(): Boolean {
        return try {
            val isLoggedIn = web3AuthHelper.isUserAuthenticated()
            return isLoggedIn
        }  catch (e: Exception) {
            return false
        }
    }

    fun getBalance() {
        viewModelScope.launch {
            updateSuccessState(isLoadingBalance = true)
            _isAccountLoaded.emit(false)
            try {
                balance = solanaHelper.getBalance(solanaKeyPair.publicKey)
                _isAccountLoaded.emit(true)
                updateSuccessState(balance = balance, isLoadingBalance = false)
            } catch (e: Exception) {
                _isAccountLoaded.emit(false)
                updateSuccessState(isLoadingBalance = false)
                Log.e("HomeVM", "Error getting balance: ${e.message}", e)
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            try {
                web3AuthHelper.logOut().await()
                walletManager.clearWallet() // Clear securely stored wallet data
                _isLoggedIn.emit(false)
                _viewState.value = HomeVS.LoggedOut
            } catch (e: Exception) {
                Log.e("Logout", e.toString())
                _isLoggedIn.emit(true)
            }
        }
    }

    fun setResultUrl(uri: Uri?) {
        viewModelScope.launch {
            web3AuthHelper.setResultUrl(uri)
        }
    }

    fun signAndSendTransaction(onSign: (hash: String?, error: String?) -> Unit) {
        // Use biometric authentication for secure key access when signing
        if (walletManager.walletState.value == WalletManager.WalletState.Locked) {
            _isBiometricAuthInProgress.value = true
            walletManager.getPrivateKey(
                onSuccess = { privateKey ->
                    val keypair = Keypair.fromSecretKey(privateKey.hexToByteArray())
                    _isBiometricAuthInProgress.value = false
                    viewModelScope.launch {
                        try {
                            val signedTransaction = solanaHelper.signAndSendSol(keypair)
                            onSign(signedTransaction, null)
                        } catch (e: Exception) {
                            e.localizedMessage?.let { onSign(null, it) }
                        }
                    }
                },
                onError = { errorMessage ->
                    _isBiometricAuthInProgress.value = false
                    Log.e("HomeVM", "Biometric authentication failed for signing: $errorMessage")
                    onSign(null, "Authentication failed: $errorMessage")
                }
            )
        } else {
            // Fall back to Web3Auth if biometric isn't available
            viewModelScope.launch {
                try {
                    val signedTransaction = solanaHelper.signAndSendSol(solanaKeyPair)
                    onSign(signedTransaction, null)
                } catch (e: Exception) {
                    e.localizedMessage?.let { onSign(null, it) }
                }
            }
        }
    }

    fun signTransaction(onSign: (signedTransaction: String?, error: String?) -> Unit) {
        // Use biometric authentication for secure key access when signing
        if (walletManager.walletState.value == WalletManager.WalletState.Locked) {
            _isBiometricAuthInProgress.value = true
            walletManager.getPrivateKey(
                onSuccess = { privateKey ->
                    val keypair = Keypair.fromSecretKey(privateKey.hexToByteArray())
                    _isBiometricAuthInProgress.value = false
                    viewModelScope.launch {
                        try {
                            val signedTransaction = solanaHelper.signSendSol(keypair)
                            onSign(signedTransaction, null)
                        } catch (e: Exception) {
                            e.localizedMessage?.let { onSign(null, it) }
                        }
                    }
                },
                onError = { errorMessage ->
                    _isBiometricAuthInProgress.value = false
                    Log.e("HomeVM", "Biometric authentication failed for signing: $errorMessage")
                    onSign(null, "Authentication failed: $errorMessage")
                }
            )
        } else {
            // Fall back to Web3Auth if biometric isn't available
            viewModelScope.launch {
                try {
                    val signedTransaction = solanaHelper.signSendSol(solanaKeyPair)
                    onSign(signedTransaction, null)
                } catch (e: Exception) {
                    e.localizedMessage?.let { onSign(null, it) }
                }
            }
        }
    }

    fun userInfo(onAvailable:(userInfo: UserInfo?, error: String?) -> Unit) {
        if (userInfoData != null) {
            onAvailable(userInfoData, null)
            return
        }
        
        try {
            val info = web3AuthHelper.getUserInfo()
            userInfoData = info
            updateSuccessState(userInfo = info)
            onAvailable(info, null)
        } catch (e: Exception) {
            e.localizedMessage?.let { onAvailable(null, it) }
        }
    }

    private fun observeNotificationState() {
        viewModelScope.launch {
            notificationStateManager.shouldShowPopup.collect { shouldShow ->
                _shouldShowPopup.value = shouldShow
                updateSuccessState(shouldShowPopup = shouldShow)
            }
        }
    }

    fun navigateToX(
        packageManager: PackageManager,
        startActivity:(intent:Intent)-> Unit
    ){
        val twitterUsername = "NexArb_"
        val uri = "twitter://user?screen_name=$twitterUsername".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val packageManager = packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage("com.twitter.android")

        if (launchIntent != null) {
            // Twitter app is installed
            intent.setPackage("com.twitter.android")
            startActivity(intent)
        } else {
            // Fallback to browser
            val browserIntent = Intent(Intent.ACTION_VIEW,
                "https://twitter.com/$twitterUsername".toUri())
            startActivity(browserIntent)
        }
    }
    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                activityHolder.getActivity() as Context,
                Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            _askForPermission.value = true
        }
    }
}