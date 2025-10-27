package com.dag.mypayandroid.base.data.repository


import com.dag.mypayandroid.base.data.repository.SecureStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(
    private val secureStorage: SecureStorage
){
    fun savePublicKey(publicKey: String): Result<Unit> {
        return try {
            with(secureStorage) {
                saveString(SecureStorage.KEY_WALLET, publicKey)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPublicKey(): Result<String?> {
        return try {
            with(secureStorage) {
                val wallet = getString(SecureStorage.KEY_WALLET)
                Result.success(wallet)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun savePrivateKey(privateKey: String): Result<Unit> {
        return try {
            with(secureStorage) {
                saveString(SecureStorage.KEY_WALLET_PRIVATE_KEY, privateKey)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getPrivateKey(): Result<String?> {
        return try {
            with(secureStorage) {
                val privateKey = getString(SecureStorage.KEY_WALLET_PRIVATE_KEY)
                Result.success(privateKey)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun clearWallet(): Result<Unit> {
        return try {
            with(secureStorage) {
                saveString(SecureStorage.KEY_WALLET, null)
                saveString(SecureStorage.KEY_WALLET_PRIVATE_KEY, null)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun hasWalletData(): Boolean {
        return try {
            with(secureStorage) {
                val hasPrivateKey = getString(SecureStorage.KEY_WALLET_PRIVATE_KEY) != null
                val hasPublicKey = getString(SecureStorage.KEY_WALLET) != null
                val hasEthWallet = getString(SecureStorage.KEY_ETH_WALLET_PRIVATE_KEY) != null
                val hasSolanaWallet = getString(SecureStorage.KEY_SOLANA_WALLET_PRIVATE_KEY) != null
                (hasPrivateKey && hasPublicKey) || hasEthWallet || hasSolanaWallet
            }
        } catch (e: Exception) {
            false
        }
    }

    // ETH Wallet methods
    fun saveEthPrivateKey(privateKey: String): Result<Unit> {
        return try {
            secureStorage.saveString(SecureStorage.KEY_ETH_WALLET_PRIVATE_KEY, privateKey)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveEthPublicKey(publicKey: String): Result<Unit> {
        return try {
            secureStorage.saveString(SecureStorage.KEY_ETH_WALLET_PUBLIC_KEY, publicKey)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getEthPrivateKey(): Result<String?> {
        return try {
            val privateKey = secureStorage.getString(SecureStorage.KEY_ETH_WALLET_PRIVATE_KEY)
            Result.success(privateKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getEthPublicKey(): Result<String?> {
        return try {
            val publicKey = secureStorage.getString(SecureStorage.KEY_ETH_WALLET_PUBLIC_KEY)
            Result.success(publicKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Solana Wallet methods
    fun saveSolanaPrivateKey(privateKey: String): Result<Unit> {
        return try {
            secureStorage.saveString(SecureStorage.KEY_SOLANA_WALLET_PRIVATE_KEY, privateKey)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveSolanaPublicKey(publicKey: String): Result<Unit> {
        return try {
            secureStorage.saveString(SecureStorage.KEY_SOLANA_WALLET_PUBLIC_KEY, publicKey)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSolanaPrivateKey(): Result<String?> {
        return try {
            val privateKey = secureStorage.getString(SecureStorage.KEY_SOLANA_WALLET_PRIVATE_KEY)
            Result.success(privateKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getSolanaPublicKey(): Result<String?> {
        return try {
            val publicKey = secureStorage.getString(SecureStorage.KEY_SOLANA_WALLET_PUBLIC_KEY)
            Result.success(publicKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}