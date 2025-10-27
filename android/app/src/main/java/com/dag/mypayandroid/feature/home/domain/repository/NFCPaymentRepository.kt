package com.dag.mypayandroid.feature.home.domain.repository

import com.dag.mypayandroid.feature.home.domain.model.PaymentRequest
import java.math.BigDecimal

interface NFCPaymentRepository {
    fun createPaymentRequest(amount: BigDecimal, recipientAddress: String): Result<String>
    fun parsePaymentURL(paymentUrl: String): Result<PaymentRequest>
    fun isNFCEnabled(): Boolean
    fun sendNFCMessage(message: String): Result<Unit>
}