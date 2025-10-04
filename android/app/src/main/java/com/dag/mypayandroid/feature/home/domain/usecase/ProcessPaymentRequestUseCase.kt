package com.dag.mypayandroid.feature.home.domain.usecase

import com.dag.mypayandroid.feature.home.domain.model.PaymentRequest
import com.dag.mypayandroid.feature.home.domain.model.PaymentResult
import com.dag.mypayandroid.feature.home.domain.repository.PaymentRepository
import javax.inject.Inject

class ProcessPaymentRequestUseCase @Inject constructor(
    private val paymentRepository: PaymentRepository
) {
    suspend operator fun invoke(paymentRequest: PaymentRequest, privateKey: String): Result<PaymentResult> {
        return paymentRepository.executePayment(paymentRequest, privateKey)
    }
}