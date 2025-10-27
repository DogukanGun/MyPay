package com.dag.mypayandroid.feature.login

import com.dag.mypayandroid.base.BaseVS

sealed class LoginVS: BaseVS {
    data class StartLogin(
        val emailError: String? = null,
        val isLoading: Boolean = false
    ) : LoginVS()

    object StartHomePage: LoginVS()

    data class Error(val errorMessage: String): LoginVS()
}