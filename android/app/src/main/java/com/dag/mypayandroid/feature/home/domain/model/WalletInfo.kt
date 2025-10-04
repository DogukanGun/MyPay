package com.dag.mypayandroid.feature.home.domain.model

import java.math.BigDecimal

data class WalletInfo(
    val publicKey: String,
    val balance: BigDecimal,
    val isLoadingBalance: Boolean = false
)