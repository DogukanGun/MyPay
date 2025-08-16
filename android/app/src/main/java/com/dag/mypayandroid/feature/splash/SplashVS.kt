package com.dag.mypayandroid.feature.splash

import com.dag.mypayandroid.base.BaseVS
import com.dag.mypayandroid.base.navigation.Destination


sealed class SplashVS: BaseVS {
    data class StartApp(val destination: Destination): SplashVS()
    data object CloseApp: SplashVS()
}