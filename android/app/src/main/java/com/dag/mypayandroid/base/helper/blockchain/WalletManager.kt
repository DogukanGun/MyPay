package com.dag.mypayandroid.base.helper.blockchain

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dag.mypayandroid.base.network.WalletInfo
import com.dag.mypayandroid.base.helper.security.BiometricHelper
import com.dag.mypayandroid.base.helper.system.ActivityHolder
import com.dag.mypayandroid.base.data.repository.WalletRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletManager @Inject constructor(
    private val biometricHelper: BiometricHelper,
    private val activityHolder: ActivityHolder,
    private val context: Context,
    private val walletRepository: WalletRepository
) {
    companion object {
        private const val WALLET_BIOMETRIC_KEY_NAME = "WALLET_SECURE_KEY"

        // Default messages
        private const val DEFAULT_TITLE = "Authentication Required"
        private const val DEFAULT_SUBTITLE = "Authenticate to access your wallet"
        private const val DEFAULT_NEGATIVE_BUTTON = "Cancel"
    }

    private val _walletState = MutableLiveData<WalletState>(WalletState.Uninitialized)
    val walletState: LiveData<WalletState> = _walletState

    init {
        biometricHelper.initialize(WALLET_BIOMETRIC_KEY_NAME)
        checkWalletState()
    }

    /**
     * Check if wallet exists and update state
     */
    private fun checkWalletState() {
        _walletState.value = when {
            walletRepository.hasWalletData() -> WalletState.Locked
            else -> WalletState.NotCreated
        }
    }

    /**
     * Store wallet credentials securely with biometric authentication
     */
    @RequiresApi(Build.VERSION_CODES.R)
    fun storeWalletCredentials(
        privateKey: String,
        publicKey: String,
        activity: FragmentActivity? = activityHolder.getActivity() as? FragmentActivity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        activity?.let { fragmentActivity ->
            biometricHelper.setupBiometricPrompt(
                activity = fragmentActivity,
                title = DEFAULT_TITLE,
                subtitle = "Authenticate to secure your wallet",
                negativeButtonText = DEFAULT_NEGATIVE_BUTTON,
                onSuccess = { cryptoObject ->
                    if (cryptoObject != null) {
                        try {
                            val encryptedData = biometricHelper.encryptData(privateKey, cryptoObject)
                            
                            // Store private key and public key using WalletRepository
                            walletRepository.savePrivateKey(privateKey)
                            walletRepository.savePublicKey(publicKey)
                            
                            _walletState.value = WalletState.Locked
                            onSuccess()
                        } catch (e: Exception) {
                            onError("Failed to secure wallet: ${e.message}")
                        }
                    } else {
                        onError("Encryption failed: Crypto object is null")
                    }
                },
                onError = { errorCode, errorMessage ->
                    onError("Authentication failed: $errorMessage")
                }
            )
            
            biometricHelper.showBiometricPrompt()
        } ?: onError("Activity not available")
    }

    /**
     * Get private key with biometric authentication
     */
    fun getPrivateKey(
        activity: FragmentActivity? = activityHolder.getActivity() as? FragmentActivity,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_walletState.value != WalletState.Locked) {
            onError("Wallet is not available")
            return
        }

        val privateKeyResult = walletRepository.getPrivateKey()

        if (privateKeyResult.isFailure) {
            onError("Failed to access wallet data")
            return
        }

        val privateKeyFromRepo = privateKeyResult.getOrNull()

        if (privateKeyFromRepo == null) {
            onError("Wallet data is corrupted")
            return
        }

            activity?.let { fragmentActivity ->
                biometricHelper.setupBiometricPrompt(
                    activity = fragmentActivity,
                    title = DEFAULT_TITLE,
                    subtitle = DEFAULT_SUBTITLE,
                    negativeButtonText = DEFAULT_NEGATIVE_BUTTON,
                    onSuccess = { cryptoObject ->
                        if (cryptoObject != null) {
                            try {
                                onSuccess(privateKeyFromRepo)
                            } catch (e: Exception) {
                                onError("Failed to access wallet: ${e.message}")
                            }
                        } else {
                            onError("Authentication failed: Crypto object is null")
                        }
                    },
                    onError = { errorCode, errorMessage ->
                        onError("Authentication failed: $errorMessage")
                    }
                )
                
                biometricHelper.showBiometricPrompt()
            } ?: onError("Activity not available")
    }

    /**
     * Get public key without biometric authentication
     */
    fun getPublicKey(): String? {
        return walletRepository.getPublicKey().getOrNull()
    }

    /**
     * Clear wallet data (for logout)
     */
    fun clearWallet() {
        walletRepository.clearWallet()
        _walletState.value = WalletState.NotCreated
    }
    
    fun clearAllWallets() {
        clearWallet()
    }

    /**
     * Store both ETH and Solana wallets securely with biometric authentication
     */
    fun storeWallets(
        ethWallet: WalletInfo?,
        solanaWallet: WalletInfo?,
        onResult: (Boolean) -> Unit
    ) {
        if (ethWallet == null && solanaWallet == null) {
            onResult(false)
            return
        }

        val activity = activityHolder.getActivity() as? FragmentActivity
        activity?.let { fragmentActivity ->
            biometricHelper.setupBiometricPrompt(
                activity = fragmentActivity,
                title = DEFAULT_TITLE,
                subtitle = "Authenticate to secure your wallets",
                negativeButtonText = DEFAULT_NEGATIVE_BUTTON,
                onSuccess = { cryptoObject ->
                    if (cryptoObject != null) {
                        try {
                            // Store ETH wallet if provided
                            ethWallet?.let { wallet ->
                                walletRepository.saveEthPrivateKey(wallet.private_key)
                                walletRepository.saveEthPublicKey(wallet.public_address)
                            }
                            
                            // Store Solana wallet if provided  
                            solanaWallet?.let { wallet ->
                                walletRepository.saveSolanaPrivateKey(wallet.private_key)
                                walletRepository.saveSolanaPublicKey(wallet.public_address)
                            }
                            
                            _walletState.value = WalletState.Locked
                            onResult(true)
                        } catch (e: Exception) {
                            onResult(false)
                        }
                    } else {
                        onResult(false)
                    }
                },
                onError = { errorCode, errorMessage ->
                    onResult(false)
                }
            )
            
            biometricHelper.showBiometricPrompt()
        } ?: onResult(false)
    }

    /**
     * Check if biometric authentication is available on the device
     */
    fun isBiometricAvailable(): Boolean {
        return BiometricHelper.Companion.isBiometricAvailable(context)
    }

    /**
     * Check if the device has biometric hardware
     */
    fun isBiometricHardwareSupported(): Boolean {
        return BiometricHelper.Companion.isHardwareSupported(context)
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
        }
        return data
    }

    sealed class WalletState {
        object Uninitialized : WalletState()
        object NotCreated : WalletState() // Wallet doesn't exist
        object Locked : WalletState() // Wallet exists but needs authentication
        data class Unlocked(val publicKey: String) : WalletState() // Temporarily authenticated
    }
}