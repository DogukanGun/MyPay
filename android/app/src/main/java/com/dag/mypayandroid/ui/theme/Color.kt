package com.dag.mypayandroid.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color


val PrimaryColor = Color(0xFFEBFE06) // #EBFE06
val Background  = Color(0xFFD1D5DB) // #D1D5DB
val DarkBackground   = Color(0xFF16161B) // #16161B
val SecondaryColor = Color(0xFFA39EFF) // #A39EFF

val gradientStart = Color(0xFF8EC5FC) // Gradient soft blue
val gradientEnd = Color(0xFFE0C3FC)   // Gradient soft purple

val primaryText = Color.Black
val secondaryText = Color.LightGray
val disabledText = Color(0xFF64748B)

// Navigation Colors
val bottomNavBarColor = DarkBackground
val bottomNavSelectedColor = PrimaryColor
val bottomNavUnselectedColor = Background

val iconGradient = Brush.linearGradient(
    colors = listOf(
        gradientStart,
        gradientEnd
    )
)
