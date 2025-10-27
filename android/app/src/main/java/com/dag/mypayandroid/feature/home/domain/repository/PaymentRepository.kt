package com.dag.mypayandroid.feature.home.domain.repository

import com.dag.mypayandroid.feature.home.domain.model.PaymentRequest
import com.dag.mypayandroid.feature.home.domain.model.PaymentResult

interface PaymentRepository {
    suspend fun executePayment(paymentRequest: PaymentRequest, privateKey: String): Result<PaymentResult>
}