package com.dag.mypayandroid.feature.splash

import android.content.pm.PackageManager
import androidx.lifecycle.viewModelScope
import com.dag.mypayandroid.BuildConfig
import com.dag.mypayandroid.MainActivity
import com.dag.mypayandroid.base.helper.system.ActivityHolder
import com.dag.mypayandroid.base.helper.system.AlertDialogManager
import com.dag.mypayandroid.base.BaseVM
import com.dag.mypayandroid.base.data.AlertDialogButton
import com.dag.mypayandroid.base.data.AlertDialogButtonType
import com.dag.mypayandroid.base.data.AlertDialogModel
import com.dag.mypayandroid.base.data.repository.AuthRepository
import com.dag.mypayandroid.base.helper.blockchain.WalletManager
import com.dag.mypayandroid.base.navigation.Destination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class SplashVM @Inject constructor(
    private val packageManager: PackageManager,
    private val alertDialogManager: AlertDialogManager,
    private val activityHolder: ActivityHolder,
    private val authRepository: AuthRepository,
    private val walletManager: WalletManager
): BaseVM<SplashVS>(){

    init {
        checkWallets()
    }

    companion object {
        private const val METAMASK_PACKAGE_NAME = "io.metamask"
        private const val SPLASH_DELAY = 3000L // 3 seconds delay
    }

    private fun checkWallets(){
        if (BuildConfig.DEBUG){
            startApp()
            return
        }
        if (!isMetamaskWalletInstalled()){
            viewModelScope.launch {
                alertDialogManager.showAlert(
                    AlertDialogModel(
                        title = "Wallet Required",
                        message = "To use NexWallet, you need Metamask wallet installed on your device. Please install one of these wallets from your app store to continue.",
                        textInput = false,
                        positiveButton = AlertDialogButton(
                            text = "Got it",
                            onClick = {
                                _viewState.value = SplashVS.CloseApp
                            },
                            type = AlertDialogButtonType.CUSTOM
                        )
                    )
                )
            }
        }else{
            startApp()
        }
    }

    fun startApp() {
        viewModelScope.launch {
            delay(SPLASH_DELAY)
            
            // Check if user is already authenticated and has wallet
            val hasAuth = authRepository.hasAuthToken()
            val hasWallet = walletManager.getPublicKey() != null
            
            val destination = if (hasAuth && hasWallet) {
                Destination.HomeScreen // User is already logged in
            } else {
                Destination.LoginScreen // User needs to log in
            }
            
            _viewState.value = SplashVS.StartApp(destination)
        }
    }

    fun closeApp() {
        activityHolder.getActivity()?.finish()
    }

    private fun isMetamaskWalletInstalled(): Boolean {
        return isPackageInstalled(METAMASK_PACKAGE_NAME)
    }

    private fun isPackageInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}