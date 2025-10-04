package com.dag.mypayandroid.base.data.repository

import android.content.Context
import android.provider.Settings
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

@Singleton
class SecureStorage @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePreferences = EncryptedSharedPreferences.create(
        context,
        "nexwallet_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveString(key: String, value: String?) {
        securePreferences.edit { putString(key, value) }
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return securePreferences.getString(key, defaultValue)
    }

    fun saveInt(key: String, value: Int) {
        securePreferences.edit { putInt(key, value) }
    }

    fun getInt(key: String, defaultValue: Int = 0): Int {
        return securePreferences.getInt(key, defaultValue)
    }

    fun saveBoolean(key: String, value: Boolean) {
        securePreferences.edit { putBoolean(key, value) }
    }

    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return securePreferences.getBoolean(key, defaultValue)
    }

    fun saveLong(key: String, value: Long) {
        securePreferences.edit { putLong(key, value) }
    }

    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return securePreferences.getLong(key, defaultValue)
    }

    fun saveStringList(key: String, value: List<String>){
        securePreferences.edit { putStringSet(key,value.toSet()) }
    }

    fun getStringSet(key: String): Set<String>? {
        return securePreferences.getStringSet(key,emptySet<String>())
    }

    fun removeString(key: String) {
        securePreferences.edit { remove(key) }
    }

    fun clear() {
        securePreferences.edit { clear() }
    }

    fun getDeviceIdentifier(): String {
        // First check if we have a stored device ID
        val storedDeviceId = getString("device_identifier")
        if (!storedDeviceId.isNullOrEmpty()) {
            return storedDeviceId
        }
        
        // Generate a new device identifier
        val deviceId = generateDeviceIdentifier()
        
        // Store it for future use
        saveString("device_identifier", deviceId)
        
        return deviceId
    }
    
    private fun generateDeviceIdentifier(): String {
        // Use Android ID as primary device identifier
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        
        // Create a more robust identifier using multiple device properties
        val deviceInfo = StringBuilder()
        
        // Android ID (if available and not default)
        if (!androidId.isNullOrEmpty() && androidId != "9774d56d682e549c") {
            deviceInfo.append(androidId)
        } else {
            // Fallback: Use combination of device properties
            deviceInfo.append(android.os.Build.BOARD)
            deviceInfo.append(android.os.Build.BRAND)
            deviceInfo.append(android.os.Build.DEVICE)
            deviceInfo.append(android.os.Build.HARDWARE)
            deviceInfo.append(android.os.Build.MANUFACTURER)
            deviceInfo.append(android.os.Build.MODEL)
            deviceInfo.append(android.os.Build.PRODUCT)
        }
        
        // Generate a hash to create a consistent, IMEI-like identifier
        val bytes = deviceInfo.toString().toByteArray()
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        
        // Convert to hex and take first 15 characters (similar to IMEI length)
        return hash.joinToString("") { "%02x".format(it) }.take(15)
    }

    companion object {
        //USER KEYS
        const val KEY_USER_TOKEN = "key_user_token"
        const val KEY_TWITTER_ID = "key_twitter_id"
        const val KEY_DISPLAY_NAME = "key_display_name"
        //WALLET KEYS
        const val KEY_WALLET = "key_wallet"
        const val KEY_WALLET_PRIVATE_KEY = "key_wallet_private_key"
        
        //ETH WALLET KEYS
        const val KEY_ETH_WALLET_PUBLIC_KEY = "key_eth_wallet_public_key"
        const val KEY_ETH_WALLET_PRIVATE_KEY = "key_eth_wallet_private_key"
        
        //SOLANA WALLET KEYS
        const val KEY_SOLANA_WALLET_PUBLIC_KEY = "key_solana_wallet_public_key"
        const val KEY_SOLANA_WALLET_PRIVATE_KEY = "key_solana_wallet_private_key"
    }
}