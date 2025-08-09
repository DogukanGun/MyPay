package com.dag.mypayandroid.base.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dag.mypayandroid.R
import com.dag.mypayandroid.base.navigation.Destination

@Composable
fun getDestinationTitle(destination: String): String{
    return when(destination) {
        Destination.HomeScreen.toString() -> {
            stringResource(R.string.app_name)
        }
        else -> {
            stringResource(R.string.app_name)
        }
    }
}