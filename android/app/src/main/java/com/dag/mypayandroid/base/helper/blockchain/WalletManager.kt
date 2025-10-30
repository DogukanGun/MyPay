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
     * Get private key with biometric authentication (prioritizes Solana, falls back to ETH)
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

        // Check for Solana wallet first, then ETH wallet, then legacy wallet
        val solanaPrivateKeyResult = walletRepository.getSolanaPrivateKey()
        val ethPrivateKeyResult = walletRepository.getEthPrivateKey()
        val legacyPrivateKeyResult = walletRepository.getPrivateKey()

        val privateKeyFromRepo = when {
            solanaPrivateKeyResult.isSuccess && solanaPrivateKeyResult.getOrNull() != null -> {
                solanaPrivateKeyResult.getOrNull()
            }
            ethPrivateKeyResult.isSuccess && ethPrivateKeyResult.getOrNull() != null -> {
                ethPrivateKeyResult.getOrNull()
            }
            legacyPrivateKeyResult.isSuccess && legacyPrivateKeyResult.getOrNull() != null -> {
                legacyPrivateKeyResult.getOrNull()
            }
            else -> null
        }

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
     * Get public key without biometric authentication (prioritizes Solana, falls back to ETH)
     */
    fun getPublicKey(): String? {
        // Check for Solana wallet first, then ETH wallet, then legacy wallet
        val solanaPublicKeyResult = walletRepository.getSolanaPublicKey()
        val ethPublicKeyResult = walletRepository.getEthPublicKey()
        val legacyPublicKeyResult = walletRepository.getPublicKey()

        return when {
            solanaPublicKeyResult.isSuccess && solanaPublicKeyResult.getOrNull() != null -> {
                solanaPublicKeyResult.getOrNull()
            }
            ethPublicKeyResult.isSuccess && ethPublicKeyResult.getOrNull() != null -> {
                ethPublicKeyResult.getOrNull()
            }
            legacyPublicKeyResult.isSuccess && legacyPublicKeyResult.getOrNull() != null -> {
                legacyPublicKeyResult.getOrNull()
            }
            else -> null
        }
    }

    /**
     * Clear wallet data (for logout)
     */
    fun clearWallet() {
        // Clear legacy wallet
        walletRepository.clearWallet()
        
        // Clear new wallet storage
        try {
            walletRepository.saveEthPrivateKey("")
            walletRepository.saveEthPublicKey("")
            walletRepository.saveSolanaPrivateKey("")
            walletRepository.saveSolanaPublicKey("")
        } catch (e: Exception) {
            // Ignore errors during cleanup
        }
        
        _walletState.value = WalletState.NotCreated
    }
    
    fun clearAllWallets() {
        clearWallet()
    }

    /**
     * Get public key for specific chain
     */
    fun getPublicKeyForChain(chain: com.dag.mypayandroid.feature.home.presentation.components.BlockchainChain): String? {
        return when (chain) {
            com.dag.mypayandroid.feature.home.presentation.components.BlockchainChain.SOLANA -> {
                walletRepository.getSolanaPublicKey().getOrNull()
            }
            com.dag.mypayandroid.feature.home.presentation.components.BlockchainChain.ETHEREUM -> {
                walletRepository.getEthPublicKey().getOrNull()
            }
        }
    }

    /**
     * Get private key for specific chain with biometric authentication
     */
    fun getPrivateKeyForChain(
        chain: com.dag.mypayandroid.feature.home.presentation.components.BlockchainChain,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_walletState.value != WalletState.Locked) {
            onError("Wallet is not available")
            return
        }

        val privateKeyResult = when (chain) {
            com.dag.mypayandroid.feature.home.presentation.components.BlockchainChain.SOLANA -> {
                walletRepository.getSolanaPrivateKey()
            }
            com.dag.mypayandroid.feature.home.presentation.components.BlockchainChain.ETHEREUM -> {
                walletRepository.getEthPrivateKey()
            }
        }

        if (privateKeyResult.isFailure || privateKeyResult.getOrNull() == null) {
            onError("${chain.displayName} wallet not found")
            return
        }

        val privateKeyFromRepo = privateKeyResult.getOrNull()!!
        onSuccess(privateKeyFromRepo)
    }

    /**
     * Get all available wallets with biometric authentication
     */
    fun getAllWallets(
        activity: FragmentActivity? = activityHolder.getActivity() as? FragmentActivity,
        onSuccess: (Map<String, String>) -> Unit,
        onError: (String) -> Unit
    ) {
        if (_walletState.value != WalletState.Locked) {
            onError("Wallet is not available")
            return
        }

        activity?.let { fragmentActivity ->
            biometricHelper.setupBiometricPrompt(
                activity = fragmentActivity,
                title = DEFAULT_TITLE,
                subtitle = "Authenticate to access your wallets",
                negativeButtonText = DEFAULT_NEGATIVE_BUTTON,
                onSuccess = { cryptoObject ->
                    if (cryptoObject != null) {
                        try {
                            val wallets = mutableMapOf<String, String>()
                            
                            // Check for Solana wallet
                            val solanaPrivateKeyResult = walletRepository.getSolanaPrivateKey()
                            if (solanaPrivateKeyResult.isSuccess && solanaPrivateKeyResult.getOrNull() != null) {
                                wallets["Solana"] = solanaPrivateKeyResult.getOrNull()!!
                            }
                            
                            // Check for ETH wallet
                            val ethPrivateKeyResult = walletRepository.getEthPrivateKey()
                            if (ethPrivateKeyResult.isSuccess && ethPrivateKeyResult.getOrNull() != null) {
                                wallets["Ethereum"] = ethPrivateKeyResult.getOrNull()!!
                            }
                            
                            // Check for legacy wallet
                            val legacyPrivateKeyResult = walletRepository.getPrivateKey()
                            if (legacyPrivateKeyResult.isSuccess && legacyPrivateKeyResult.getOrNull() != null) {
                                wallets["Legacy"] = legacyPrivateKeyResult.getOrNull()!!
                            }
                            
                            if (wallets.isNotEmpty()) {
                                onSuccess(wallets)
                            } else {
                                onError("No wallets found")
                            }
                        } catch (e: Exception) {
                            onError("Failed to access wallets: ${e.message}")
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
                            var allSuccessful = true
                            
                            // Store ETH wallet if provided
                            ethWallet?.let { wallet ->
                                val ethPrivateResult = walletRepository.saveEthPrivateKey(wallet.private_key)
                                val ethPublicResult = walletRepository.saveEthPublicKey(wallet.public_address)
                                if (ethPrivateResult.isFailure || ethPublicResult.isFailure) {
                                    allSuccessful = false
                                }
                            }
                            
                            // Store Solana wallet if provided  
                            solanaWallet?.let { wallet ->
                                val solanaPrivateResult = walletRepository.saveSolanaPrivateKey(wallet.private_key)
                                val solanaPublicResult = walletRepository.saveSolanaPublicKey(wallet.public_address)
                                if (solanaPrivateResult.isFailure || solanaPublicResult.isFailure) {
                                    allSuccessful = false
                                }
                            }
                            
                            if (allSuccessful) {
                                _walletState.value = WalletState.Locked
                                onResult(true)
                            } else {
                                onResult(false)
                            }
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


    sealed class WalletState {
        object Uninitialized : WalletState()
        object NotCreated : WalletState() // Wallet doesn't exist
        object Locked : WalletState() // Wallet exists but needs authentication
    }
}