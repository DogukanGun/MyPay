package com.dag.mypayandroid.feature.home.domain.usecase

import com.dag.mypayandroid.feature.home.domain.repository.WalletRepository
import java.math.BigDecimal
import javax.inject.Inject

class GetWalletBalanceUseCase @Inject constructor(
    private val walletRepository: WalletRepository
) {
    suspend operator fun invoke(publicKey: String): Result<BigDecimal> {
        return walletRepository.getBalance(publicKey)
    }
}