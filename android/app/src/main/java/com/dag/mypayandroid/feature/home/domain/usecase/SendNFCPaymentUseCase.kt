package com.dag.mypayandroid.feature.home.domain.usecase

import com.dag.mypayandroid.feature.home.domain.model.PaymentRequest
import com.dag.mypayandroid.feature.home.domain.repository.NFCPaymentRepository
import java.math.BigDecimal
import javax.inject.Inject

class SendNFCPaymentUseCase @Inject constructor(
    private val nfcPaymentRepository: NFCPaymentRepository
) {
    operator fun invoke(amount: BigDecimal, recipientAddress: String): Result<String> {
        return nfcPaymentRepository.createPaymentRequest(amount, recipientAddress)
    }
}