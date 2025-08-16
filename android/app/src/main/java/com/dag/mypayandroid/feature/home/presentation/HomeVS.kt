package com.dag.mypayandroid.feature.home.presentation

import com.dag.mypayandroid.base.BaseVS
import com.web3auth.core.types.UserInfo

sealed class HomeVS : BaseVS {
    data object Loading : HomeVS()
    data object LoggedOut: HomeVS()
    data class Error(val message: String) : HomeVS()
    data class Success(
        var walletAddress: String? = null,
        val shouldShowPopup: Boolean = false,
        val balance: String? = null,
        val userInfo: UserInfo? = null,
        val isLoadingBalance: Boolean = false
    ) : HomeVS()

    companion object {
        fun initial() = Loading
    }
}