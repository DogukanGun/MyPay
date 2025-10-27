package com.dag.mypayandroid.feature.home.domain.usecase

import com.dag.mypayandroid.feature.home.domain.model.UserProfile
import com.dag.mypayandroid.feature.home.domain.repository.UserRepository
import javax.inject.Inject

class GetUserInfoUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    operator fun invoke(): Result<UserProfile> {
        return userRepository.getUserInfo()
    }
}