package com.dag.mypayandroid.feature.home.data.repository

import com.dag.mypayandroid.base.helper.blockchain.SolanaHelper
import com.dag.mypayandroid.feature.home.domain.model.PaymentRequest
import com.dag.mypayandroid.feature.home.domain.model.PaymentResult
import com.dag.mypayandroid.feature.home.domain.repository.PaymentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.sol4k.Connection
import org.sol4k.Keypair
import org.sol4k.RpcUrl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepositoryImpl @Inject constructor(
    private val solanaHelper: SolanaHelper
) : PaymentRepository {

    override suspend fun executePayment(
        paymentRequest: PaymentRequest,
        privateKey: String
    ): Result<PaymentResult> {
        return try {
            withContext(Dispatchers.IO) {
                // Create keypair from private key - try different formats
                val keypair = try {
                    // First try as Base58 string (Solana standard)
                    Keypair.fromSecretKey(privateKey.toByteArray())
                } catch (e: Exception) {
                    try {
                        // If that fails, try as hex string
                        val privateKeyBytes = hexStringToByteArray(privateKey)
                        Keypair.fromSecretKey(privateKeyBytes)
                    } catch (e2: Exception) {
                        // If both fail, try direct byte conversion
                        Keypair.fromSecretKey(privateKey.toByteArray())
                    }
                }
                
                var transactionSignature: String? = null
                var error: Exception? = null
                
                solanaHelper.receiveSolanaPayAndMakePayment(
                    keypair = keypair,
                    paymentUrl = paymentRequest.url,
                    onSigned = { transaction ->
                        try {
                            val connection = Connection(RpcUrl.DEVNET)
                            transactionSignature = connection.sendTransaction(transaction)
                        } catch (e: Exception) {
                            error = e
                        }
                    }
                )
                
                while (transactionSignature == null && error == null) {
                    kotlinx.coroutines.delay(100)
                }
                
                error?.let { throw it }
                
                val signature = transactionSignature ?: throw Exception("Transaction signature not received")
                
                Result.success(
                    PaymentResult(
                        transactionSignature = signature,
                        amount = paymentRequest.amount,
                        recipient = paymentRequest.recipient
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        for (i in 0 until len step 2) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
        }
        return data
    }
}