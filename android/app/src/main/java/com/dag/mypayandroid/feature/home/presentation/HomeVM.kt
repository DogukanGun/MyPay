package com.dag.mypayandroid.feature.home.presentation

import com.dag.mypayandroid.base.helper.security.NFCHelper
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
import com.dag.mypayandroid.base.helper.system.ActivityHolder
import com.dag.mypayandroid.base.helper.blockchain.WalletManager
import com.dag.mypayandroid.base.navigation.DefaultNavigator
import com.dag.mypayandroid.base.navigation.Destination
import com.dag.mypayandroid.base.solanapay.SolanaPayURLEncoder
import com.dag.mypayandroid.base.solanapay.TransferRequestURLFields
import com.web3auth.core.Web3Auth
import com.web3auth.core.types.UserInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import org.sol4k.Connection
import org.sol4k.Keypair
import org.sol4k.PublicKey
import org.sol4k.RpcUrl
import java.math.BigDecimal
import com.dag.mypayandroid.base.solanapay.SolanaPayURLParser

@HiltViewModel
class HomeVM @Inject constructor(
    private val activityHolder: ActivityHolder,
    private val walletManager: WalletManager,
    private val defaultNavigator: DefaultNavigator,
    private val notificationStateManager: NotificationStateManager,
    private val nfcHelper: NFCHelper
) : BaseVM<HomeVS>(initialValue = HomeVS.Companion.initial()) {

    private var _askForPermission = MutableStateFlow(false)
    val askForPermission:StateFlow<Boolean> = _askForPermission

    lateinit var solanaKeyPair: Keypair
    lateinit var balance: String
    private var userInfoData: UserInfo? = null

    private val _nfcPaymentState = MutableStateFlow<NFCPaymentState>(NFCPaymentState.Idle)
    val nfcPaymentState: StateFlow<NFCPaymentState> = _nfcPaymentState

    init {
        switchToSuccessState()
        checkPermission()
        observeNotificationState()
        setupNFCListener()
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

    fun sendPayment(amount: Int, recipient: PublicKey) {
        if (nfcHelper.isNFCAvailable()) {
            val url = SolanaPayURLEncoder.encodeURL(
                fields = TransferRequestURLFields(
                    recipient = recipient,
                    amount = BigDecimal.valueOf(amount.toLong()),
                    tokenDecimal = 9
                )
            )
            nfcHelper.sendPaymentRequest(url.toString())
        } else {
            //TODO trigger settings to turn on nfc
        }
    }

    fun getBalance() {
        viewModelScope.launch {
            updateSuccessState(isLoadingBalance = true)
            try {
                walletManager.getPublicKey()?.let {
                    withContext(Dispatchers.IO) {
                        val connection = Connection(RpcUrl.DEVNET)
                        val balanceResponse = connection.getBalance(PublicKey(it)).toBigDecimal()
                        balance = balanceResponse.divide(BigDecimal.TEN.pow(9)).toString()
                        updateSuccessState(balance = balance, isLoadingBalance = false)
                    }
                }
            } catch (e: Exception) {
                updateSuccessState(isLoadingBalance = false)
                Log.e("HomeVM", "Error getting balance: ${e.message}", e)
            }
        }
    }

    fun signAndSendTransaction(onSign: (hash: String?, error: String?) -> Unit) {
        // Use biometric authentication for secure key access when signing
        if (walletManager.walletState.value == WalletManager.WalletState.Locked) {
            walletManager.getPrivateKey(
                onSuccess = { privateKey ->
                    val keypair = Keypair.fromSecretKey(privateKey.hexToByteArray())
                    viewModelScope.launch {
                        try {
                            //val signedTransaction = solanaHelper.signAndSendSol(keypair)
                            //onSign(signedTransaction, null)
                        } catch (e: Exception) {
                            e.localizedMessage?.let { onSign(null, it) }
                        }
                    }
                },
                onError = { errorMessage ->
                    Log.e("HomeVM", "Biometric authentication failed for signing: $errorMessage")
                    onSign(null, "Authentication failed: $errorMessage")
                }
            )
        } else {
            // Fall back to Web3Auth if biometric isn't available
            viewModelScope.launch {
                try {
                    //val signedTransaction = solanaHelper.signAndSendSol(solanaKeyPair)
                    //onSign(signedTransaction, null)
                } catch (e: Exception) {
                    e.localizedMessage?.let { onSign(null, it) }
                }
            }
        }
    }

    fun signTransaction(onSign: (signedTransaction: String?, error: String?) -> Unit) {
        // Use biometric authentication for secure key access when signing
        if (walletManager.walletState.value == WalletManager.WalletState.Locked) {
            walletManager.getPrivateKey(
                onSuccess = { privateKey ->
                    val keypair = Keypair.fromSecretKey(privateKey.hexToByteArray())
                    viewModelScope.launch {
                        try {
                            //val signedTransaction = solanaHelper.signSendSol(keypair)
                            //onSign(signedTransaction, null)
                        } catch (e: Exception) {
                            e.localizedMessage?.let { onSign(null, it) }
                        }
                    }
                },
                onError = { errorMessage ->
                    Log.e("HomeVM", "Biometric authentication failed for signing: $errorMessage")
                    onSign(null, "Authentication failed: $errorMessage")
                }
            )
        } else {
            // Fall back to Web3Auth if biometric isn't available
            viewModelScope.launch {
                try {
                    //val signedTransaction = solanaHelper.signSendSol(solanaKeyPair)
                    //onSign(signedTransaction, null)
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

    // NFC payment methods
    fun initiateNFCPayment(amount: Int, recipient: PublicKey) {
        if (nfcHelper.isNFCAvailable()) {
            val url = SolanaPayURLEncoder.encodeURL(
                fields = TransferRequestURLFields(
                    recipient = recipient,
                    amount = BigDecimal.valueOf(amount.toLong()),
                    tokenDecimal = 9
                )
            )
            
            nfcHelper.initiatePaymentRequest(url.toString(), BigDecimal.valueOf(amount.toLong())) { paymentState ->
                when (paymentState) {
                    NFCHelper.PaymentState.WAITING_FOR_REQUEST -> {
                        _nfcPaymentState.value = NFCPaymentState.Sending
                    }
                    NFCHelper.PaymentState.ERROR -> {
                        _nfcPaymentState.value = NFCPaymentState.Error("NFC Payment request failed")
                    }
                    else -> {}
                }
            }
        } else {
            _nfcPaymentState.value = NFCPaymentState.Error("NFC is not available")
        }
    }

    fun processNFCPaymentRequest(paymentUrl: String, amount: BigDecimal) {
        _nfcPaymentState.value = NFCPaymentState.RequestReceived(paymentUrl, amount)
    }

    fun confirmNFCPayment() {
        viewModelScope.launch {
            try {
                // Here you would typically validate the transaction and sign it
                val transactionId = nfcHelper.generateTransactionId()
                nfcHelper.sendPaymentResponse(transactionId)
                _nfcPaymentState.value = NFCPaymentState.Completed(transactionId)
            } catch (e: Exception) {
                _nfcPaymentState.value = NFCPaymentState.Error("Payment confirmation failed")
            }
        }
    }

    fun resetNFCPaymentState() {
        nfcHelper.resetPaymentState()
        _nfcPaymentState.value = NFCPaymentState.Idle
    }

    // Update NFC listener setup
    private fun setupNFCListener() {
        nfcHelper.setNFCListener(object : NFCHelper.NFCPaymentListener {
            override fun onPaymentRequestReceived(paymentUrl: String) {
                // Parse the payment URL to get details
                try {
                    val parsedRequest = SolanaPayURLParser.parseURL(paymentUrl)
                    if (parsedRequest is TransferRequestURLFields) {
                        processNFCPaymentRequest(paymentUrl, parsedRequest.amount)
                    }
                } catch (e: Exception) {
                    _nfcPaymentState.value = NFCPaymentState.Error("Invalid payment request")
                }
            }

            override fun onPaymentResponseReceived(transactionId: String) {
                _nfcPaymentState.value = NFCPaymentState.Completed(transactionId)
            }

            override fun onNFCError(error: String) {
                _nfcPaymentState.value = NFCPaymentState.Error(error)
            }

            override fun onNFCMessageSent() {
                _nfcPaymentState.value = NFCPaymentState.Sending
            }

            override fun onDeviceConnected() {
                // Optional: Handle device connection
            }

            override fun onDeviceDisconnected() {
                // Optional: Handle device disconnection
            }

            override fun onPaymentStateChanged(state: NFCHelper.PaymentState) {
                // Optional: Additional state tracking
            }
        })
    }
}