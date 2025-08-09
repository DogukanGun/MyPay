package com.dag.mypayandroid.feature.home.presentation

import com.dag.mypayandroid.base.BaseVS

sealed class HomeVS : BaseVS {
    data object Loading : HomeVS()
    data object LoggedOut: HomeVS()
    data class Error(val message: String) : HomeVS()
    data class Success(
        var walletAddress: String? = null,
        val shouldShowPopup: Boolean = false
    ) : HomeVS()

    companion object {
        fun initial() = Loading
    }
}