package com.dag.mypayandroid.base.helper

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.dag.mypayandroid.base.extensions.toHexString
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletManager @Inject constructor(
    private val biometricHelper: BiometricHelper,
    private val activityHolder: ActivityHolder,
    private val context: Context
) {
    companion object {
        private const val WALLET_PREF = "WALLET_PREFERENCES"
        private const val KEY_ENCRYPTED_PRIVATE_KEY = "ENCRYPTED_PRIVATE_KEY"
        private const val KEY_ENCRYPTED_IV = "ENCRYPTED_IV"
        private const val KEY_PUBLIC_KEY = "PUBLIC_KEY"
        private const val WALLET_BIOMETRIC_KEY_NAME = "WALLET_SECURE_KEY"

        // Default messages
        private const val DEFAULT_TITLE = "Authentication Required"
        private const val DEFAULT_SUBTITLE = "Authenticate to access your wallet"
        private const val DEFAULT_NEGATIVE_BUTTON = "Cancel"
    }

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(WALLET_PREF, Context.MODE_PRIVATE)
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
        val hasEncryptedData = preferences.contains(KEY_ENCRYPTED_PRIVATE_KEY)
        val hasPublicKey = preferences.contains(KEY_PUBLIC_KEY)

        _walletState.value = when {
            hasEncryptedData && hasPublicKey -> WalletState.Locked
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
                            
                            // Store encrypted private key and IV
                            preferences.edit().apply {
                                putString(KEY_ENCRYPTED_PRIVATE_KEY, encryptedData.encryptedData.toHexString())
                                putString(KEY_ENCRYPTED_IV, encryptedData.iv.toHexString())
                                putString(KEY_PUBLIC_KEY, publicKey)
                                putString(KEY_PUBLIC_KEY, publicKey)
                                apply()
                            }
                            
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

        val encryptedPrivateKeyHex = preferences.getString(KEY_ENCRYPTED_PRIVATE_KEY, null)
        val ivHex = preferences.getString(KEY_ENCRYPTED_IV, null)

        if (encryptedPrivateKeyHex == null || ivHex == null) {
            onError("Wallet data is corrupted")
            return
        }

        try {
            val encryptedPrivateKey = hexStringToByteArray(encryptedPrivateKeyHex)
            val iv = hexStringToByteArray(ivHex)

            activity?.let { fragmentActivity ->
                biometricHelper.setupBiometricPrompt(
                    activity = fragmentActivity,
                    title = DEFAULT_TITLE,
                    subtitle = DEFAULT_SUBTITLE,
                    negativeButtonText = DEFAULT_NEGATIVE_BUTTON,
                    onSuccess = { cryptoObject ->
                        if (cryptoObject != null) {
                            try {
                                val encryptedData = BiometricHelper.EncryptedData(encryptedPrivateKey, iv)
                                val privateKey = biometricHelper.decryptData(encryptedData, cryptoObject)
                                onSuccess(privateKey)
                            } catch (e: Exception) {
                                onError("Failed to decrypt wallet: ${e.message}")
                            }
                        } else {
                            onError("Decryption failed: Crypto object is null")
                        }
                    },
                    onError = { errorCode, errorMessage ->
                        onError("Authentication failed: $errorMessage")
                    }
                )
                
                biometricHelper.showDecryptionBiometricPrompt(iv)
            } ?: onError("Activity not available")
        } catch (e: Exception) {
            onError("Failed to access wallet: ${e.message}")
        }
    }

    /**
     * Get public key without biometric authentication
     */
    fun getPublicKey(): String? {
        return preferences.getString(KEY_PUBLIC_KEY, null)
    }

    /**
     * Clear wallet data (for logout)
     */
    fun clearWallet() {
        preferences.edit().clear().apply()
        _walletState.value = WalletState.NotCreated
    }

    /**
     * Check if biometric authentication is available on the device
     */
    fun isBiometricAvailable(): Boolean {
        return BiometricHelper.isBiometricAvailable(context)
    }

    /**
     * Check if the device has biometric hardware
     */
    fun isBiometricHardwareSupported(): Boolean {
        return BiometricHelper.isHardwareSupported(context)
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