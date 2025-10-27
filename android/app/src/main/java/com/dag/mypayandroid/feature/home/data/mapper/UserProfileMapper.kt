package com.dag.mypayandroid.feature.home.data.mapper

import com.dag.mypayandroid.feature.home.domain.model.UserProfile
import com.google.firebase.auth.UserInfo
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileMapper @Inject constructor() {
    
    fun mapToUserProfile(userInfo: UserInfo): UserProfile {
        return UserProfile(
            name = userInfo.displayName,
            email = userInfo.email,
        )
    }
}