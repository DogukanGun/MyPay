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
import android.util.Log
import androidx.core.app.ActivityCompat
import com.dag.mypayandroid.base.notification.NotificationStateManager
import com.dag.mypayandroid.base.helper.ActivityHolder
import com.dag.mypayandroid.base.helper.SolanaHelper
import com.dag.mypayandroid.base.helper.WalletManager
import com.dag.mypayandroid.base.navigation.DefaultNavigator
import com.dag.mypayandroid.base.navigation.Destination
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.UserInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.sol4k.Keypair
import org.sol4k.PublicKey

@HiltViewModel
class HomeVM @Inject constructor(
    private val activityHolder: ActivityHolder,
    private val solanaHelper: SolanaHelper,
    private val walletManager: WalletManager,
    private val defaultNavigator: DefaultNavigator,
    private val notificationStateManager: NotificationStateManager
) : BaseVM<HomeVS>(initialValue = HomeVS.Companion.initial()) {

    private var _askForPermission = MutableStateFlow(false)
    val askForPermission:StateFlow<Boolean> = _askForPermission

    private val _shouldShowPopup = MutableStateFlow(false)
    val shouldShowPopup: StateFlow<Boolean> = _shouldShowPopup

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

    fun fetchUserDataAfterAuth(web3Auth: Web3Auth) {
        fetchUserInfo(web3Auth)
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
                walletAddress = walletAddress ?: walletManager.getPublicKey(),
                shouldShowPopup = shouldShowPopup ?: currentState.shouldShowPopup,
                balance = balance ?: currentState.balance,
                userInfo = userInfo ?: currentState.userInfo,
                isLoadingBalance = isLoadingBalance ?: currentState.isLoadingBalance
            )
        } else {
            _viewState.value = HomeVS.Success(
                walletAddress = walletAddress ?: walletManager.getPublicKey(),
                shouldShowPopup = shouldShowPopup == true,
                balance = balance,
                userInfo = userInfo,
                isLoadingBalance = isLoadingBalance == true
            )
        }
    }

    private fun fetchUserInfo(web3Auth: Web3Auth) {
        try {
            val info = web3Auth.getUserInfo()
            userInfoData = info
            updateSuccessState(userInfo = info)
        } catch (e: Exception) {
            Log.e("HomeVM", "Error fetching user info: ${e.message}", e)
        }
    }

    fun resetToLoginState() {
        viewModelScope.launch {
            defaultNavigator.navigate(Destination.LoginScreen) {
                launchSingleTop = true
                popUpTo(0) { inclusive = true }
            }
        }
    }

    fun getBalance() {
        viewModelScope.launch {
            updateSuccessState(isLoadingBalance = true)
            _isAccountLoaded.emit(false)
            try {
                walletManager.getPublicKey()?.let {
                    balance = solanaHelper.getBalance(PublicKey(it))
                    _isAccountLoaded.emit(true)
                    updateSuccessState(balance = balance, isLoadingBalance = false)
                }
            } catch (e: Exception) {
                _isAccountLoaded.emit(false)
                updateSuccessState(isLoadingBalance = false)
                Log.e("HomeVM", "Error getting balance: ${e.message}", e)
            }
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

    fun userInfo(web3Auth: Web3Auth, onAvailable:(userInfo: UserInfo?, error: String?) -> Unit) {
        if (userInfoData != null) {
            onAvailable(userInfoData, null)
            return
        }
        
        try {
            val info = web3Auth.getUserInfo()
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