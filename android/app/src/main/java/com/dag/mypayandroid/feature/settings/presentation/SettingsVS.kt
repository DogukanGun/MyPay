package com.dag.mypayandroid.feature.settings.presentation

import com.dag.mypayandroid.base.BaseVS

sealed class SettingsVS: BaseVS {
    data object ShowSettings: SettingsVS()
    data class ShowPrivateKey(val privateKey: String): SettingsVS()
    data class ExternalSource(val url: String): SettingsVS()
    data class ShowError(val errorMessage: String): SettingsVS()
}