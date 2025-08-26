package com.dag.mypayandroid.feature.home.presentation

import com.dag.mypayandroid.base.helper.security.NFCHelper
import com.dag.mypayandroid.base.helper.security.NFCMode
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
import com.dag.mypayandroid.base.data.AlertDialogButton
import com.dag.mypayandroid.base.data.AlertDialogButtonType
import com.dag.mypayandroid.base.data.AlertDialogModel
import com.dag.mypayandroid.base.notification.NotificationStateManager
import com.dag.mypayandroid.base.helper.system.ActivityHolder
import com.dag.mypayandroid.base.helper.blockchain.WalletManager
import com.dag.mypayandroid.base.helper.system.AlertDialogManager
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
    private val alertDialogManager: AlertDialogManager,
    private val notificationStateManager: NotificationStateManager,
    private val nfcHelper: NFCHelper
) : BaseVM<HomeVS>(initialValue = HomeVS.Companion.initial()) {

    private var _askForPermission = MutableStateFlow(false)
    val askForPermission: StateFlow<Boolean> = _askForPermission

    lateinit var balance: String
    private var userInfoData: UserInfo? = null

    private val _nfcPaymentState = MutableStateFlow<NFCPaymentState>(NFCPaymentState.Idle)
    val nfcPaymentState: StateFlow<NFCPaymentState> = _nfcPaymentState

    companion object {
        private const val TAG = "HomeVM"
    }

    init {
        switchToSuccessState()
        checkPermission()
        observeNotificationState()
        setupNFCListener()
        // Start in reader mode by default (ready to receive)
        nfcHelper.setMode(NFCMode.READER)
        _nfcPaymentState.value = NFCPaymentState.Receiving
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
            Log.e(TAG, "Error fetching user info: ${e.message}", e)
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
                Log.e(TAG, "Error getting balance: ${e.message}", e)
            }
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
        startActivity: (intent: Intent) -> Unit
    ) {
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
            val browserIntent = Intent(
                Intent.ACTION_VIEW,
                "https://twitter.com/$twitterUsername".toUri()
            )
            startActivity(browserIntent)
        }
    }

    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                activityHolder.getActivity() as Context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            _askForPermission.value = true
        }
    }

    fun processNFCPaymentRequest(paymentUrl: String, amount: BigDecimal) {
        _nfcPaymentState.value = NFCPaymentState.RequestReceived(paymentUrl, amount)
    }

    /**
     * Sets up the listener to react to events from the NFCHelper.
     */
    private fun setupNFCListener() {
        nfcHelper.setListener(object : NFCHelper.NFCListener {
            override fun onMessageReceived(message: String) {
                // We received a payment URL from another device
                Log.d(TAG, "NFC message received: $message")
                try {
                    // Use a parser to extract details from the Solana Pay URL
                    val parsed = SolanaPayURLParser.parseURL(message)
                    viewModelScope.launch {
                        alertDialogManager.showAlert(
                            AlertDialogModel(
                                "Payment Request",
                                "A new payment request is received.",
                                positiveButton = AlertDialogButton(
                                    text = "Pay",
                                    onClick = {},
                                    type = AlertDialogButtonType.CUSTOM
                                ),
                                negativeButton = AlertDialogButton(
                                    text = "Reject",
                                    type = AlertDialogButtonType.CLOSE
                                )
                            )
                        )
                    }
                    _nfcPaymentState.value = NFCPaymentState.RequestReceived(message, parsed.amount)
                } catch (e: Exception) {
                    _nfcPaymentState.value = NFCPaymentState.Error("Invalid payment request received: ${e.message}")
                    Log.e(TAG, "Error parsing NFC payment URL", e)
                }
            }

            override fun onNFCError(error: String) {
                Log.e(TAG, "NFC Error: $error")
                _nfcPaymentState.value = NFCPaymentState.Error(error)
            }

            override fun onNFCStateChanged(mode: NFCMode) {
                Log.d(TAG, "NFC mode changed to: $mode")
                when (mode) {
                    NFCMode.TAG -> _nfcPaymentState.value = NFCPaymentState.Sending
                    NFCMode.READER -> _nfcPaymentState.value = NFCPaymentState.Receiving
                }
            }
        })
    }

    /**
     * Resets the NFC state and switches back to receiver mode.
     * Call this after completing a transaction or when cancelling.
     */
    fun resetNFCPaymentState() {
        Log.d(TAG, "Resetting NFC payment state")
        _nfcPaymentState.value = NFCPaymentState.Receiving
        // Switch back to reader mode after sending/completing transaction
        nfcHelper.setMode(NFCMode.READER)
    }

    /**
     * Prepares and sends a payment request URL via NFC.
     * This automatically switches to TAG mode and prepares the message for HCE transmission.
     */
    fun sendNFCPayment(amount: Int, recipient: PublicKey) {
        // Check if NFC is available first
        if (!nfcHelper.isNfcEnabled()) {
            _nfcPaymentState.value = NFCPaymentState.Error("Please enable NFC in your device settings.")
            return
        }

        try {
            // Create the Solana Pay URL
            val url = SolanaPayURLEncoder.encodeURL(
                fields = TransferRequestURLFields(
                    recipient = recipient,
                    amount = BigDecimal.valueOf(amount.toLong()),
                    tokenDecimal = 9 // Assuming SOL decimals
                )
            )

            Log.d(TAG, "Preparing to send payment URL via NFC: $url")

            // Switch to TAG mode to enable HCE
            nfcHelper.setMode(NFCMode.TAG)

            // Tell the helper to send the message (stores in SharedPreferences)
            nfcHelper.sendMessage(url.toString())

            _nfcPaymentState.value = NFCPaymentState.Sending
            Log.d(TAG, "NFC payment message prepared for sending via HCE - now tap devices together")

        } catch (e: Exception) {
            _nfcPaymentState.value = NFCPaymentState.Error("Failed to prepare payment: ${e.message}")
            Log.e(TAG, "Error preparing NFC payment", e)
        }
    }

    /**
     * Switch NFC mode manually - this is the key method for same-app P2P
     */
    fun switchNFCMode(mode: NFCMode) {
        if (!nfcHelper.isNfcEnabled()) {
            _nfcPaymentState.value = NFCPaymentState.Error("Please enable NFC in your device settings.")
            return
        }

        Log.d(TAG, "Switching NFC mode to: $mode")
        nfcHelper.setMode(mode)

        // Update state immediately when switching modes
        _nfcPaymentState.value = when (mode) {
            NFCMode.READER -> {
                Log.d(TAG, "Now in READER mode - ready to receive payments")
                NFCPaymentState.Receiving
            }
            NFCMode.TAG -> {
                Log.d(TAG, "Now in TAG mode - ready to send payments")
                NFCPaymentState.Idle // Will be updated to Sending when message is prepared
            }
        }
    }

    /**
     * Get current NFC mode
     */
    fun getCurrentNFCMode(): NFCMode = nfcHelper.getCurrentMode()
}