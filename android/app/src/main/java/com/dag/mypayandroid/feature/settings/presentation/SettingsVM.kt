package com.dag.mypayandroid.feature.settings.presentation

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.dag.mypayandroid.base.BaseVM
import com.dag.mypayandroid.base.helper.blockchain.WalletManager
import com.dag.mypayandroid.feature.settings.data.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsVM @Inject constructor(
    val walletManager: WalletManager
) : BaseVM<SettingsVS>(SettingsVS.ShowSettings) {

    fun executeSetting(selectedSetting: Settings) {
        when(selectedSetting) {
            Settings.FEEDBACK_FORM -> {

            }
            Settings.LEGAL -> {

            }
            Settings.PRIVATE_KEY -> {
                walletManager.getPrivateKey(
                    onSuccess = { privateKey ->
                        viewModelScope.launch {
                            try {
                                _viewState.value = SettingsVS.ShowPrivateKey(privateKey)
                            } catch (e: Exception) {
                                e.localizedMessage?.let {
                                    _viewState.value = SettingsVS.ShowError(it)
                                }
                            }
                        }
                    },
                    onError = { errorMessage ->
                        Log.e("SettingsVM", "Biometric authentication failed for signing: $errorMessage")
                        _viewState.value = SettingsVS.ShowError(errorMessage)
                    }
                )
            }
        }
    }

}