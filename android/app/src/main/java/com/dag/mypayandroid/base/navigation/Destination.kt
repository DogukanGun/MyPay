package com.dag.mypayandroid.base.navigation

import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    data object Splash: Destination

    @Serializable
    data object HomeScreen: Destination

    @Serializable
    data object LoginScreen: Destination

    @Serializable
    data object SettingsScreen: Destination

    companion object {
        val NAV_WITHOUT_BOTTOM_NAVBAR = listOf(Splash, LoginScreen)
    }

}
