package com.dag.mypayandroid.feature.home.data.repository

import com.dag.mypayandroid.base.helper.security.NFCHelper
import com.dag.mypayandroid.base.solanapay.SolanaPayURLEncoder
import com.dag.mypayandroid.base.solanapay.SolanaPayURLParser
import com.dag.mypayandroid.base.solanapay.TransferRequestURLFields
import com.dag.mypayandroid.feature.home.data.mapper.PaymentRequestMapper
import com.dag.mypayandroid.feature.home.domain.model.PaymentRequest
import com.dag.mypayandroid.feature.home.domain.repository.NFCPaymentRepository
import org.sol4k.PublicKey
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NFCPaymentRepositoryImpl @Inject constructor(
    private val nfcHelper: NFCHelper,
    private val paymentRequestMapper: PaymentRequestMapper
) : NFCPaymentRepository {

    override fun createPaymentRequest(amount: BigDecimal, recipientAddress: String): Result<String> {
        return try {
            val url = SolanaPayURLEncoder.encodeURL(
                fields = TransferRequestURLFields(
                    recipient = PublicKey(recipientAddress),
                    amount = amount,
                    tokenDecimal = 9
                )
            )
            Result.success(url.toString())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun parsePaymentURL(paymentUrl: String): Result<PaymentRequest> {
        return try {
            val parsed = SolanaPayURLParser.parseURL(paymentUrl)
            val paymentRequest = paymentRequestMapper.mapToPaymentRequest(parsed, paymentUrl)
            Result.success(paymentRequest)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isNFCEnabled(): Boolean {
        return nfcHelper.isNfcEnabled()
    }

    override fun sendNFCMessage(message: String): Result<Unit> {
        return try {
            nfcHelper.sendMessage(message)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}