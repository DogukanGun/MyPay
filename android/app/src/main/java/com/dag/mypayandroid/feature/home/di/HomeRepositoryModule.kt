package com.dag.mypayandroid.feature.home.di

import com.dag.mypayandroid.feature.home.data.repository.NFCPaymentRepositoryImpl
import com.dag.mypayandroid.feature.home.data.repository.PaymentRepositoryImpl
import com.dag.mypayandroid.feature.home.data.repository.UserRepositoryImpl
import com.dag.mypayandroid.feature.home.data.repository.WalletRepositoryImpl
import com.dag.mypayandroid.feature.home.domain.repository.NFCPaymentRepository
import com.dag.mypayandroid.feature.home.domain.repository.PaymentRepository
import com.dag.mypayandroid.feature.home.domain.repository.UserRepository
import com.dag.mypayandroid.feature.home.domain.repository.WalletRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class HomeRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWalletRepository(
        walletRepositoryImpl: WalletRepositoryImpl
    ): WalletRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        userRepositoryImpl: UserRepositoryImpl
    ): UserRepository

    @Binds
    @Singleton
    abstract fun bindNFCPaymentRepository(
        nfcPaymentRepositoryImpl: NFCPaymentRepositoryImpl
    ): NFCPaymentRepository

    @Binds
    @Singleton
    abstract fun bindPaymentRepository(
        paymentRepositoryImpl: PaymentRepositoryImpl
    ): PaymentRepository
}