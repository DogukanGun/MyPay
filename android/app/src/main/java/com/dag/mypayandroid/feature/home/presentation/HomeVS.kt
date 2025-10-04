package com.dag.mypayandroid.feature.home.presentation

import com.dag.mypayandroid.base.BaseVS
import com.dag.mypayandroid.feature.home.domain.model.UserProfile
import java.math.BigDecimal

sealed class HomeVS : BaseVS {
    data object Loading : HomeVS()
    data object LoggedOut: HomeVS()
    data class Error(val message: String) : HomeVS()
    data class Success(
        var walletAddress: String? = null,
        val shouldShowPopup: Boolean = false,
        val balance: String? = null,
        val userProfile: UserProfile? = null,
        val isLoadingBalance: Boolean = false
    ) : HomeVS()

    companion object {
        fun initial() = Loading
    }
}

sealed class NFCPaymentState {
    object Idle : NFCPaymentState()
    object Sending : NFCPaymentState()
    object Completed : NFCPaymentState()
    object Receiving : NFCPaymentState()
    data class RequestReceived(val paymentUrl: String, val amount: BigDecimal) : NFCPaymentState()
    data class PaymentSent(val transactionSignature: String) : NFCPaymentState()
    data class Error(val message: String) : NFCPaymentState()
    data class Success(val message: String) : NFCPaymentState()
}