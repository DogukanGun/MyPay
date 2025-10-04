package com.dag.mypayandroid.base.data.repository

import com.dag.mypayandroid.base.data.repository.SecureStorage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val secureStorage: SecureStorage
) {
    fun saveToken(token: String): Result<Unit> {
        return try {
            with(secureStorage) {
                saveString(SecureStorage.KEY_USER_TOKEN, token)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    fun getToken(): Result<String?> {
        return try {
            with(secureStorage) {
                val wallet = getString(SecureStorage.KEY_USER_TOKEN)
                if (wallet != null) {
                    Result.success(wallet)
                } else {
                    Result.success(null)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}