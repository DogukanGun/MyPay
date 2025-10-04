package com.dag.mypayandroid.feature.home.domain.model

data class UserProfile(
    val name: String?,
    val email: String?,
    val profileImage: String? = null,
    val typeOfLogin: String? = null
)