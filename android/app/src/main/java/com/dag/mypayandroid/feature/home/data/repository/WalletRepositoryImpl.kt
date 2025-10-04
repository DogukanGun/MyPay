package com.dag.mypayandroid.feature.home.data.repository

import com.dag.mypayandroid.base.helper.blockchain.WalletManager
import com.dag.mypayandroid.feature.home.domain.repository.WalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sol4k.Connection
import org.sol4k.PublicKey
import org.sol4k.RpcUrl
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepositoryImpl @Inject constructor(
    private val walletManager: WalletManager
) : WalletRepository {

    override suspend fun getBalance(publicKey: String): Result<BigDecimal> {
        return try {
            withContext(Dispatchers.IO) {
                val connection = Connection(RpcUrl.DEVNET)
                val balanceResponse = connection.getBalance(PublicKey(publicKey)).toBigDecimal()
                val balance = balanceResponse.divide(BigDecimal.TEN.pow(9))
                Result.success(balance)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getPublicKey(): String? {
        return walletManager.getPublicKey()
    }

    override suspend fun getPrivateKey(): Result<String> {
        return try {
            val privateKey = suspendingGetPrivateKey()
            Result.success(privateKey)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun suspendingGetPrivateKey(): String {
        return withContext(Dispatchers.IO) {
            var result: String? = null
            var error: String? = null
            
            walletManager.getPrivateKey(
                onSuccess = { privateKey -> result = privateKey },
                onError = { errorMsg -> error = errorMsg }
            )
            
            while (result == null && error == null) {
                kotlinx.coroutines.delay(100)
            }
            
            error?.let { throw Exception(it) }
            result ?: throw Exception("Failed to get private key")
        }
    }
}