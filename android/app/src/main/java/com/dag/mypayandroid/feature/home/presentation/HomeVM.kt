package com.dag.mypayandroid.feature.home.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri
import com.dag.mypayandroid.base.BaseVM
import android.content.Context
import androidx.core.app.ActivityCompat
import com.dag.mypayandroid.base.notification.NotificationStateManager
import com.dag.mypayandroid.base.helper.ActivityHolder
import com.dag.mypayandroid.base.helper.WalletManagement
import io.metamask.androidsdk.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@HiltViewModel
class HomeVM @Inject constructor(
    private val activityHolder: ActivityHolder,
    private val walletManagementImpl: WalletManagement,
    private val notificationStateManager: NotificationStateManager
) : BaseVM<HomeVS>(initialValue = HomeVS.Companion.initial()) {

    private var _askForPermission = MutableStateFlow(false)
    val askForPermission:StateFlow<Boolean> = _askForPermission

    private val _shouldShowPopup = MutableStateFlow(false)
    val shouldShowPopup: StateFlow<Boolean> = _shouldShowPopup

    init {
        switchToSuccessState()
        checkPermission()
//        checkMetamaskConnection()
        observeNotificationState()
    }

    private fun switchToSuccessState() {
        _viewState.value = HomeVS.Success()
    }

    fun startCharge() {

    }

    fun startPayment() {

    }

    private fun checkMetamaskConnection() {
        viewModelScope.launch {
            if (!walletManagementImpl.isConnected()) {
                val currentState =  _viewState.value as HomeVS.Success
                walletManagementImpl.connect {
                    if (it is Result.Success.Item) {
                        currentState.walletAddress = it.value
                    } else if (it is Result.Success.Items) {
                        currentState.walletAddress = it.value.first()
                    }
                }
            }
        }
    }

    private fun observeNotificationState() {
        viewModelScope.launch {
            notificationStateManager.shouldShowPopup.collect { shouldShow ->
                _shouldShowPopup.value = shouldShow
                if (_viewState.value is HomeVS.Success) {
                    _viewState.value = (_viewState.value as HomeVS.Success).copy(
                        shouldShowPopup = shouldShow
                    )
                }
            }
        }
    }

    fun navigateToX(
        packageManager: PackageManager,
        startActivity:(intent:Intent)-> Unit
    ){
        val twitterUsername = "NexArb_"
        val uri = "twitter://user?screen_name=$twitterUsername".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)

        val packageManager = packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage("com.twitter.android")

        if (launchIntent != null) {
            // Twitter app is installed
            intent.setPackage("com.twitter.android")
            startActivity(intent)
        } else {
            // Fallback to browser
            val browserIntent = Intent(Intent.ACTION_VIEW,
                "https://twitter.com/$twitterUsername".toUri())
            startActivity(browserIntent)
        }
    }
    private fun checkPermission() {
        if (ActivityCompat.checkSelfPermission(
                activityHolder.getActivity() as Context,
                Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            _askForPermission.value = true
        }
    }

}