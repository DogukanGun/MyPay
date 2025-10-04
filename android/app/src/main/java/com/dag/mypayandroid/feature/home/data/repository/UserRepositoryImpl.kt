package com.dag.mypayandroid.feature.home.data.repository

import com.dag.mypayandroid.base.data.repository.AuthRepository
import com.dag.mypayandroid.base.helper.blockchain.WalletManager
import com.dag.mypayandroid.feature.home.domain.model.UserProfile
import com.dag.mypayandroid.feature.home.domain.repository.UserRepository
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val authRepository: AuthRepository,
    private val walletManager: WalletManager
) : UserRepository {

    override fun getUserInfo(): Result<UserProfile> {
        return try {
            // Get locally stored user info first
            val (twitterId, displayName) = authRepository.getUserInfo()
            val walletAddress = walletManager.getPublicKey()
            
            if (displayName != null && twitterId != null) {
                // Use local data if available
                Result.success(
                    UserProfile(
                        name = displayName,
                        email = twitterId,
                        profileImage = null,
                        typeOfLogin = "Twitter"
                    )
                )
            } else {
                // Fallback to API call if local data is missing
                runBlocking {
                    val profileResult = authRepository.getUserProfile()
                    profileResult.fold(
                        onSuccess = { response ->
                            // Save the retrieved info locally for future use
                            authRepository.saveUserInfo(response.twitter_id ?: "", response.username ?: "")
                            Result.success(
                                UserProfile(
                                    name = response.username ?: "Unknown",
                                    email = response.twitter_id,
                                    profileImage = null,
                                    typeOfLogin = "Twitter"
                                )
                            )
                        },
                        onFailure = { error ->
                            Result.failure(error)
                        }
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}