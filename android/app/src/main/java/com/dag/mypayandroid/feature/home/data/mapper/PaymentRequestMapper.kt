package com.dag.mypayandroid.feature.home.data.mapper

import com.dag.mypayandroid.base.solanapay.TransferRequestURLFields
import com.dag.mypayandroid.feature.home.domain.model.PaymentRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRequestMapper @Inject constructor() {
    
    fun mapToPaymentRequest(parsed: TransferRequestURLFields, originalUrl: String): PaymentRequest {
        return PaymentRequest(
            recipient = parsed.recipient.toString(),
            amount = parsed.amount,
            url = originalUrl
        )
    }
}