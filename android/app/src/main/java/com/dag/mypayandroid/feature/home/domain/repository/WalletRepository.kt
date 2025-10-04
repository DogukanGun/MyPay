package com.dag.mypayandroid.feature.home.domain.repository

import java.math.BigDecimal

interface WalletRepository {
    suspend fun getBalance(publicKey: String): Result<BigDecimal>
    fun getPublicKey(): String?
    suspend fun getPrivateKey(): Result<String>
}