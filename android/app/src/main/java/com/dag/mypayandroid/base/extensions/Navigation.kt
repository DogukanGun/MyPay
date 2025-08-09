package com.dag.mypayandroid.base.extensions

import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.dag.mypayandroid.base.navigation.Destination


fun NavController.startAsTopComposable(destination: Destination){
    this.navigate(destination) {
        launchSingleTop = true
        popUpTo(0) { inclusive = true }
    }
}