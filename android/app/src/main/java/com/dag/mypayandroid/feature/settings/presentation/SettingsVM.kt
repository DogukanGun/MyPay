package com.dag.mypayandroid.feature.settings.presentation

import android.content.Context
import android.util.Log
import com.dag.mypayandroid.R
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import com.dag.mypayandroid.base.BaseVM
import com.dag.mypayandroid.base.data.AlertDialogButton
import com.dag.mypayandroid.base.data.AlertDialogButtonType
import com.dag.mypayandroid.base.data.AlertDialogModel
import com.dag.mypayandroid.base.helper.blockchain.WalletManager
import com.dag.mypayandroid.base.helper.system.AlertDialogManager
import com.dag.mypayandroid.feature.settings.data.Settings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

@HiltViewModel
class SettingsVM @Inject constructor(
    val walletManager: WalletManager,
    val alertDialogManager: AlertDialogManager
) : BaseVM<SettingsVS>(SettingsVS.ShowSettings) {

    fun showList() {
        _viewState.value = SettingsVS.ShowSettings
    }
    fun executeSetting(selectedSetting: Settings,context: Context) {
        when(selectedSetting) {
            Settings.FEEDBACK_FORM -> {
                viewModelScope.launch {
                    alertDialogManager.showAlert(
                        AlertDialogModel(
                            ContextCompat.getString(context,R.string.feedback_form_alert_modal_title),
                            ContextCompat.getString(context,R.string.feedback_form_alert_modal_message),
                            positiveButton = AlertDialogButton(
                                text = ContextCompat.getString(context,R.string.send),
                                onClick = {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                                        data = "mailto:".toUri()
                                        putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf("dogukangundogan5@gmail.com"))
                                        putExtra(android.content.Intent.EXTRA_SUBJECT, "App Feedback")
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Log.e("SettingsVM", "No email app found: ${e.localizedMessage}")
                                    }
                                },
                                type = AlertDialogButtonType.CUSTOM
                            ),
                            negativeButton = AlertDialogButton(
                                text = ContextCompat.getString(context,R.string.cancel),
                                type = AlertDialogButtonType.CLOSE
                            )
                        )
                    )
                }
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