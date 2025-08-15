package com.dag.mypayandroid.base.helper

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager.*
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import java.security.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import androidx.core.content.edit
import androidx.core.content.ContextCompat
import java.nio.charset.Charset
import java.util.concurrent.Executor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricHelper @Inject constructor() {

    companion object {
        private const val PREFERENCES_BIOMETRIC = "PREFERENCES_BIOMETRIC"
        private const val LAST_USED_CIPHER_KEY_NAME = "LAST_USED_CIPHER_KEY_NAME"
        private const val DEFAULT_KEY_NAME = "WALLET_KEY"

        /**
         * Check if the device has biometric capabilities.
         */
        @JvmStatic
        fun isHardwareSupported(context: Context?): Boolean {
            context?.let {
                val biometricManager = from(it)
                val canAuthenticateResult = biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                return canAuthenticateResult != BIOMETRIC_ERROR_HW_UNAVAILABLE && canAuthenticateResult != BIOMETRIC_ERROR_NO_HARDWARE
            }
            return false
        }

        /**
         * Check if the user has enrolled biometrics on the device
         */
        @JvmStatic
        fun isBiometricAvailable(context: Context?): Boolean {
            context?.let {
                val biometricManager = from(it)
                val canAuthenticateResult = biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
                return canAuthenticateResult == BIOMETRIC_SUCCESS
            }
            return false
        }

        @JvmStatic
        fun saveLastUsedCipherKeyName(context: Context?, keyName: String) {
            context?.let {
                val settings = it.getSharedPreferences(PREFERENCES_BIOMETRIC, Context.MODE_PRIVATE)
                settings.edit {
                    putString(LAST_USED_CIPHER_KEY_NAME, keyName)
                }
            }
        }

        @JvmStatic
        fun getLastUsedCipherKeyName(context: Context?): String {
            context?.let {
                val settings = it.getSharedPreferences(PREFERENCES_BIOMETRIC, Context.MODE_PRIVATE)
                return settings.getString(LAST_USED_CIPHER_KEY_NAME, DEFAULT_KEY_NAME) ?: DEFAULT_KEY_NAME
            }
            return DEFAULT_KEY_NAME
        }
    }

    private val mProvider = "AndroidKeyStore"
    private var mKeyStore: KeyStore? = null
    private var mCipher: Cipher? = null
    private var mCipherKey: SecretKey? = null
    private var mKeyName: String = DEFAULT_KEY_NAME
    
    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    fun initialize(keyName: String = DEFAULT_KEY_NAME) {
        this.mKeyName = keyName
        loadKeyStore()
    }

    private fun loadKeyStore() {
        try {
            mKeyStore = KeyStore.getInstance(mProvider)
            mKeyStore?.load(null)
        } catch (e: Exception) {
            throw RuntimeException("Failed to get keystore", e)
        }
    }

    private fun deleteKey() {
        try {
            mKeyStore?.deleteEntry(mKeyName)
        } catch (_: Exception) {
            // ignore
        } finally {
            mCipherKey = null
        }
    }

    private fun hasKey(): Boolean {
        return try {
            mCipherKey = mKeyStore?.getKey(mKeyName, null) as? SecretKey
            mCipherKey != null
        } catch (e: UnrecoverableKeyException) {
            // User changed/removed device credentials or biometrics
            deleteKey()
            false
        } catch (e: KeyStoreException) {
            // Treat as "no key"
            mCipherKey = null
            false
        } catch (e: NoSuchAlgorithmException) {
            mCipherKey = null
            false
        }
    }

    private fun createKey(): Boolean {
        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, 
                mProvider
            )

            val keyGenSpec = KeyGenParameterSpec.Builder(
                mKeyName,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setInvalidatedByBiometricEnrollment(false)
                .setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL)
                .build()

            keyGenerator.init(keyGenSpec)
            mCipherKey = keyGenerator.generateKey()
            
            loadKeyStore() // Refresh keystore after key creation
            return mCipherKey != null
        } catch (e: Exception) {
            return false
        }
    }

    private fun getCipher(): Cipher {
        return mCipher ?: Cipher.getInstance(
            KeyProperties.KEY_ALGORITHM_AES + "/" +
                    KeyProperties.BLOCK_MODE_CBC + "/" +
                    KeyProperties.ENCRYPTION_PADDING_PKCS7
        ).also { mCipher = it }
    }

    private fun initEncryptionCipher(): Boolean {
        return try {
            getCipher().init(Cipher.ENCRYPT_MODE, mCipherKey)
            true
        } catch (e: KeyPermanentlyInvalidatedException) {
            // Key got invalidated: recreate it
            deleteKey()
            if (createKey()) {
                try {
                    getCipher().init(Cipher.ENCRYPT_MODE, mCipherKey)
                    true
                } catch (_: Exception) {
                    false
                }
            } else {
                false
            }
        } catch (e: InvalidKeyException) {
            deleteKey()
            false
        }
    }

    private fun initDecryptionCipher(iv: ByteArray): Boolean {
        return try {
            val cipher = getCipher()
            val spec = javax.crypto.spec.IvParameterSpec(iv)
            cipher.init(Cipher.DECRYPT_MODE, mCipherKey, spec)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getEncryptionCryptoObject(): BiometricPrompt.CryptoObject? {
        loadKeyStore()
        
        val keyIsGenerated = if (!hasKey()) {
            createKey()
        } else {
            true
        }
        
        if (!keyIsGenerated) {
            return null
        }

        return if (initEncryptionCipher()) {
            BiometricPrompt.CryptoObject(getCipher())
        } else {
            null
        }
    }

    fun getDecryptionCryptoObject(iv: ByteArray): BiometricPrompt.CryptoObject? {
        loadKeyStore()
        
        if (!hasKey()) {
            return null
        }

        return if (initDecryptionCipher(iv)) {
            BiometricPrompt.CryptoObject(getCipher())
        } else {
            null
        }
    }

    fun setupBiometricPrompt(
        activity: FragmentActivity,
        title: String,
        subtitle: String,
        negativeButtonText: String,
        onSuccess: (BiometricPrompt.CryptoObject?) -> Unit,
        onError: (Int, CharSequence) -> Unit
    ) {
        executor = ContextCompat.getMainExecutor(activity)
        
        biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(result.cryptoObject)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString)
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()
    }

    fun showBiometricPrompt() {
        val cryptoObject = getEncryptionCryptoObject()
        if (cryptoObject != null) {
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        } else {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    fun showDecryptionBiometricPrompt(iv: ByteArray) {
        val cryptoObject = getDecryptionCryptoObject(iv)
        if (cryptoObject != null) {
            biometricPrompt.authenticate(promptInfo, cryptoObject)
        } else {
            biometricPrompt.authenticate(promptInfo)
        }
    }

    fun encryptData(data: String, cryptoObject: BiometricPrompt.CryptoObject): EncryptedData {
        val cipher = cryptoObject.cipher!!
        val encryptedBytes = cipher.doFinal(data.toByteArray(Charset.defaultCharset()))
        return EncryptedData(encryptedBytes, cipher.iv)
    }

    fun decryptData(encryptedData: EncryptedData, cryptoObject: BiometricPrompt.CryptoObject): String {
        val cipher = cryptoObject.cipher!!
        val decryptedBytes = cipher.doFinal(encryptedData.encryptedData)
        return String(decryptedBytes, Charset.defaultCharset())
    }

    data class EncryptedData(val encryptedData: ByteArray, val iv: ByteArray) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EncryptedData

            if (!encryptedData.contentEquals(other.encryptedData)) return false
            if (!iv.contentEquals(other.iv)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = encryptedData.contentHashCode()
            result = 31 * result + iv.contentHashCode()
            return result
        }
    }
}