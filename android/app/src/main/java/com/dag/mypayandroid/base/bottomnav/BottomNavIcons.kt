package com.dag.mypayandroid.base.bottomnav

import androidx.annotation.DrawableRes
import com.dag.mypayandroid.R
import com.dag.mypayandroid.base.navigation.Destination

enum class BottomNavIcon(
    @DrawableRes var icon: Int,
    var destination: Destination
) {
    Home(R.drawable.baseline_home_filled, Destination.HomeScreen),
    Settings(R.drawable.baseline_settings, Destination.SettingsScreen)
}