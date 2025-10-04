package com.dag.mypayandroid.feature.home.domain.usecase

import com.dag.mypayandroid.feature.home.domain.model.PaymentRequest
import com.dag.mypayandroid.feature.home.domain.repository.NFCPaymentRepository
import javax.inject.Inject

class ParsePaymentURLUseCase @Inject constructor(
    private val nfcPaymentRepository: NFCPaymentRepository
) {
    operator fun invoke(paymentUrl: String): Result<PaymentRequest> {
        return nfcPaymentRepository.parsePaymentURL(paymentUrl)
    }
}