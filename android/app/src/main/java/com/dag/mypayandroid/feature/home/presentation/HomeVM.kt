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
import androidx.compose.ui.input.key.Key
import androidx.core.app.ActivityCompat
import com.dag.mypayandroid.base.data.AlertDialogButton
import com.dag.mypayandroid.base.data.AlertDialogButtonType
import com.dag.mypayandroid.base.data.AlertDialogModel
import com.dag.mypayandroid.base.notification.NotificationStateManager
import com.dag.mypayandroid.base.helper.system.ActivityHolder
import com.dag.mypayandroid.base.helper.blockchain.WalletManager
import com.dag.mypayandroid.base.helper.system.AlertDialogManager
import com.dag.mypayandroid.feature.home.domain.usecase.GetUserInfoUseCase
import com.dag.mypayandroid.base.navigation.DefaultNavigator
import com.dag.mypayandroid.base.navigation.Destination
import com.dag.mypayandroid.base.solanapay.SolanaPayURLEncoder
import com.dag.mypayandroid.base.solanapay.TransferRequestURLFields
import com.dag.mypayandroid.base.helper.blockchain.SolanaHelper
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
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import com.dag.mypayandroid.R
import com.dag.mypayandroid.feature.home.domain.model.UserProfile
import com.dag.mypayandroid.feature.home.presentation.components.BlockchainChain
import org.sol4k.Base58

@HiltViewModel
class HomeVM @Inject constructor(
    private val activityHolder: ActivityHolder,
    private val walletManager: WalletManager,
    private val defaultNavigator: DefaultNavigator,
    private val alertDialogManager: AlertDialogManager,
    private val notificationStateManager: NotificationStateManager,
    private val nfcHelper: NFCHelper,
    private val solanaHelper: SolanaHelper,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    @ApplicationContext private val context: Context
) : BaseVM<HomeVS>(initialValue = HomeVS.Companion.initial()) {

    private var _askForPermission = MutableStateFlow(false)
    val askForPermission: StateFlow<Boolean> = _askForPermission

    lateinit var balance: String

    private val _nfcPaymentState = MutableStateFlow<NFCPaymentState>(NFCPaymentState.Idle)
    val nfcPaymentState: StateFlow<NFCPaymentState> = _nfcPaymentState
    
    private val _selectedChain = MutableStateFlow(BlockchainChain.SOLANA)
    val selectedChain: StateFlow<BlockchainChain> = _selectedChain

    companion object {
        private const val TAG = "HomeVM"
    }

    init {
        switchToSuccessState()
        checkPermission()
        observeNotificationState()
        setupNFCListener()
        fetchUserProfile()
        // Initialize with wallet address and get balance
        initializeWalletData()
        // Start in reader mode by default (ready to receive)
        nfcHelper.setMode(NFCMode.READER)
        _nfcPaymentState.value = NFCPaymentState.Receiving
    }
    
    private fun initializeWalletData() {
        // Set the wallet address for the currently selected chain
        val walletAddress = walletManager.getPublicKeyForChain(_selectedChain.value)
        updateSuccessState(walletAddress = walletAddress)
        
        // Get balance if wallet exists
        if (walletAddress != null) {
            getBalance()
        }
    }

    private fun switchToSuccessState() {
        _viewState.value = HomeVS.Success()
    }

    private fun updateSuccessState(
        walletAddress: String? = null,
        shouldShowPopup: Boolean? = null,
        balance: String? = null,
        userProfile: UserProfile? = null,
        isLoadingBalance: Boolean? = null
    ) {
        val currentState = _viewState.value
        if (currentState is HomeVS.Success) {
            _viewState.value = currentState.copy(
                walletAddress = walletAddress ?: walletManager.getPublicKeyForChain(_selectedChain.value),
                shouldShowPopup = shouldShowPopup ?: currentState.shouldShowPopup,
                balance = balance ?: currentState.balance,
                userProfile = userProfile ?: currentState.userProfile,
                isLoadingBalance = isLoadingBalance ?: currentState.isLoadingBalance
            )
        } else {
            _viewState.value = HomeVS.Success(
                walletAddress = walletAddress ?: walletManager.getPublicKeyForChain(_selectedChain.value),
                shouldShowPopup = shouldShowPopup == true,
                balance = balance,
                userProfile = userProfile,
                isLoadingBalance = isLoadingBalance == true
            )
        }
    }

    private fun fetchUserProfile() {
        viewModelScope.launch {
            try {
                val userProfileResult = getUserInfoUseCase()
                userProfileResult.fold(
                    onSuccess = { profile ->
                        updateSuccessState(userProfile = profile)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Error fetching user profile: ${error.message}", error)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching user profile: ${e.message}", e)
            }
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
                when (_selectedChain.value) {
                    BlockchainChain.SOLANA -> getSolanaBalance()
                    BlockchainChain.ETHEREUM -> getEthereumBalance()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting balance: ${e.message}", e)
                updateSuccessState(balance = "Error loading balance", isLoadingBalance = false)
            }
        }
    }
    
    private suspend fun getSolanaBalance() {
        walletManager.getPublicKeyForChain(BlockchainChain.SOLANA)?.let { publicKey ->
            Log.d(TAG, "Getting Solana balance for wallet: $publicKey")
            withContext(Dispatchers.IO) {
                val connection = Connection(RpcUrl.DEVNET)
                val balanceResponse = connection.getBalance(PublicKey(publicKey)).toBigDecimal()
                val balanceInSol = balanceResponse.divide(BigDecimal.TEN.pow(9))
                balance = "$balanceInSol SOL"
                Log.d(TAG, "Solana balance retrieved: $balance")
                withContext(Dispatchers.Main) {
                    updateSuccessState(balance = balance, isLoadingBalance = false)
                }
            }
        } ?: run {
            Log.e(TAG, "No Solana public key found")
            updateSuccessState(balance = "No wallet", isLoadingBalance = false)
        }
    }
    
    private suspend fun getEthereumBalance() {
        // For now, we'll get the same public key and show ETH balance as 0
        // In a real implementation, you would have separate ETH addresses
        walletManager.getPublicKeyForChain(BlockchainChain.ETHEREUM)?.let { publicKey ->
            Log.d(TAG, "Getting Ethereum balance for wallet: $publicKey")
            withContext(Dispatchers.IO) {
                try {
                    val ethBalance = getEthBalanceFromRPC(publicKey)
                    balance = "$ethBalance ETH"
                    Log.d(TAG, "Ethereum balance retrieved: $balance")
                    withContext(Dispatchers.Main) {
                        updateSuccessState(balance = balance, isLoadingBalance = false)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching ETH balance", e)
                    withContext(Dispatchers.Main) {
                        updateSuccessState(balance = "0.00 ETH", isLoadingBalance = false)
                    }
                }
            }
        } ?: run {
            Log.e(TAG, "No Ethereum public key found")
            updateSuccessState(balance = "No wallet", isLoadingBalance = false)
        }
    }
    
    private suspend fun getEthBalanceFromRPC(address: String): String {
        // Use Ethereum RPC call similar to iOS implementation
        val rpcUrl = "https://ethereum-sepolia.publicnode.com" // Testnet
        
        val requestBody = mapOf(
            "jsonrpc" to "2.0",
            "id" to 1,
            "method" to "eth_getBalance",
            "params" to listOf(address, "latest")
        )
        
        // For now, return mock data since we need proper ETH address conversion
        // In production, you would make actual HTTP request here
        return "0.00"
    }

    private fun observeNotificationState() {
        viewModelScope.launch {
            notificationStateManager.shouldShowPopup.collect { shouldShow ->
                updateSuccessState(shouldShowPopup = shouldShow)
            }
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
                                ContextCompat.getString(
                                    context,
                                    R.string.nfc_payment_request_title
                                ),
                                ContextCompat.getString(
                                    context,
                                    R.string.nfc_payment_request_message
                                ),
                                positiveButton = AlertDialogButton(
                                    text = ContextCompat.getString(
                                        context,
                                        R.string.nfc_payment_pay_button
                                    ),
                                    onClick = {
                                        // Get Solana private key specifically and execute the payment
                                        walletManager.getPrivateKeyForChain(
                                            BlockchainChain.SOLANA,
                                            onSuccess = { privateKeyString ->
                                                viewModelScope.launch {
                                                    try {
                                                        val privateKeyBytes = Base58.decode(privateKeyString)
                                                        val keypair = Keypair.fromSecretKey(privateKeyBytes)
                                                        viewModelScope.launch {
                                                            solanaHelper.receiveSolanaPayAndMakePayment(
                                                                keypair = keypair,
                                                                paymentUrl = message,
                                                                onSigned = { transaction ->
                                                                // Transaction signed successfully
                                                                viewModelScope.launch {
                                                                    try {
                                                                        // Send the transaction to the network on IO thread
                                                                        val signature = withContext(Dispatchers.IO) {
                                                                            val connection =
                                                                                Connection(RpcUrl.DEVNET)
                                                                            connection.sendTransaction(transaction)
                                                                        }

                                                                        // Update NFC state to indicate success
                                                                        _nfcPaymentState.value =
                                                                            NFCPaymentState.PaymentSent(
                                                                                signature
                                                                            )
                                                                        var successMessage = ContextCompat.getString(
                                                                            context,
                                                                            R.string.payment_sent_message,
                                                                        )
                                                                        successMessage = successMessage.replace("{1}",parsed.amount.toString())
                                                                        successMessage = successMessage.replace("{2}",signature)
                                                                        // Show success message
                                                                        alertDialogManager.showAlert(
                                                                            AlertDialogModel(
                                                                                ContextCompat.getString(
                                                                                    context,
                                                                                    R.string.payment_sent_title
                                                                                ),
                                                                                successMessage,
                                                                                positiveButton = AlertDialogButton(
                                                                                    text = ContextCompat.getString(
                                                                                        context,
                                                                                        R.string.okay
                                                                                    ),
                                                                                    type = AlertDialogButtonType.CLOSE
                                                                                )
                                                                            )
                                                                        )

                                                                        // Refresh balance after successful payment
                                                                        getBalance()
                                                                    } catch (e: Exception) {
                                                                        Log.e(
                                                                            TAG,
                                                                            "Error sending transaction: ${e.message}",
                                                                            e
                                                                        )
                                                                        _nfcPaymentState.value =
                                                                            NFCPaymentState.Error("Failed to send payment: ${e.message}")
                                                                        alertDialogManager.showAlert(
                                                                            AlertDialogModel(
                                                                                ContextCompat.getString(
                                                                                    context,
                                                                                    R.string.payment_failed_title
                                                                                ),
                                                                                ContextCompat.getString(
                                                                                    context,
                                                                                    R.string.payment_failed_send_message).replace("{1}","Unknown error"),
                                                                                positiveButton = AlertDialogButton(
                                                                                    text = ContextCompat.getString(
                                                                                        context,
                                                                                        R.string.okay
                                                                                    ),
                                                                                    type = AlertDialogButtonType.CLOSE
                                                                                )
                                                                            )
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        )
                                                        }
                                                    } catch (e: Exception) {
                                                        Log.e(
                                                            TAG,
                                                            "Error preparing payment: ${e.message}",
                                                            e
                                                        )
                                                        _nfcPaymentState.value =
                                                            NFCPaymentState.Error("Failed to prepare payment: ${e.message}")
                                                        alertDialogManager.showAlert(
                                                            AlertDialogModel(
                                                                ContextCompat.getString(
                                                                    context,
                                                                    R.string.payment_failed_title
                                                                ),
                                                                ContextCompat.getString(
                                                                    context,
                                                                    R.string.payment_failed_prepare_message).replace("{1}","Unknown error"),
                                                                positiveButton = AlertDialogButton(
                                                                    text = ContextCompat.getString(
                                                                        context,
                                                                        R.string.okay
                                                                    ),
                                                                    type = AlertDialogButtonType.CLOSE
                                                                )
                                                            )
                                                        )
                                                    }
                                                }
                                            },
                                            onError = { error ->
                                                Log.e(TAG, "Error getting private key: $error")
                                                _nfcPaymentState.value =
                                                    NFCPaymentState.Error("Authentication failed: $error")
                                                viewModelScope.launch {
                                                    alertDialogManager.showAlert(
                                                        AlertDialogModel(
                                                            ContextCompat.getString(
                                                                context,
                                                                R.string.authentication_failed_title
                                                            ),
                                                            ContextCompat.getString(
                                                                context,
                                                                R.string.authentication_failed_message).replace("{1}",error),
                                                            positiveButton = AlertDialogButton(
                                                                text = ContextCompat.getString(
                                                                    context,
                                                                    R.string.okay
                                                                ),
                                                                type = AlertDialogButtonType.CLOSE
                                                            )
                                                        )
                                                    )
                                                }
                                            }
                                        )
                                    },
                                    type = AlertDialogButtonType.CUSTOM
                                ),
                                negativeButton = AlertDialogButton(
                                    text = ContextCompat.getString(
                                        context,
                                        R.string.nfc_payment_reject_button
                                    ),
                                    type = AlertDialogButtonType.CLOSE
                                )
                            )
                        )
                    }
                    _nfcPaymentState.value = NFCPaymentState.RequestReceived(message, parsed.amount)
                } catch (e: Exception) {
                    _nfcPaymentState.value =
                        NFCPaymentState.Error("Invalid payment request received: ${e.message}")
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
            _nfcPaymentState.value =
                NFCPaymentState.Error("Please enable NFC in your device settings.")
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
            Log.d(
                TAG,
                "NFC payment message prepared for sending via HCE - now tap devices together"
            )

        } catch (e: Exception) {
            _nfcPaymentState.value =
                NFCPaymentState.Error("Failed to prepare payment: ${e.message}")
            Log.e(TAG, "Error preparing NFC payment", e)
        }
    }

    /**
     * Helper method to convert hex string to byte array
     */
    private fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] =
                ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
        }
        return data
    }
    
    fun switchChain(chain: BlockchainChain) {
        _selectedChain.value = chain
        initializeWalletData()
    }
}