package com.dag.mypayandroid.feature.splash

import com.dag.mypayandroid.base.BaseVS


sealed class SplashVS: BaseVS {
    data object StartApp: SplashVS()
    data object CloseApp: SplashVS()
}