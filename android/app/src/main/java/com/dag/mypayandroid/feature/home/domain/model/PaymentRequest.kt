package com.dag.mypayandroid.feature.home.domain.model

import java.math.BigDecimal

data class PaymentRequest(
    val recipient: String,
    val amount: BigDecimal,
    val url: String
)

data class PaymentResult(
    val transactionSignature: String,
    val amount: BigDecimal,
    val recipient: String
)