package com.dag.mypayandroid.feature.home.domain.repository

import com.dag.mypayandroid.feature.home.domain.model.UserProfile

interface UserRepository {
    fun getUserInfo(): Result<UserProfile>
}