package com.dag.mypayandroid

import androidx.lifecycle.viewModelScope
import com.dag.mypayandroid.base.BaseVM
import com.dag.mypayandroid.base.data.repository.AuthRepository
import com.dag.mypayandroid.base.helper.blockchain.WalletManager
import com.dag.mypayandroid.base.navigation.DefaultNavigator
import com.dag.mypayandroid.base.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class MainVM @Inject constructor(
    private var defaultNavigator: DefaultNavigator,
    private val authRepository: AuthRepository,
    private val walletManager: WalletManager
): BaseVM<MainVS>() {

    fun navigate(destination: Destination){
        viewModelScope.launch {
            defaultNavigator.navigate(destination)
        }
    }

    fun isBottomNavActive(currentRoute:String?): Boolean {
        return currentRoute?.let {
            return Destination.NAV_WITHOUT_BOTTOM_NAVBAR
                .map { it.toString() }.contains(currentRoute).not()
        } ?: false
    }
    
    fun logout() {
        viewModelScope.launch {
            try {
                // Clear auth token
                authRepository.clearAuthToken()
                
                // Clear user info
                authRepository.clearUserInfo()
                
            } catch (e: Exception) {
                android.util.Log.e("MainVM", "Error during logout", e)
            }
        }
    }

}