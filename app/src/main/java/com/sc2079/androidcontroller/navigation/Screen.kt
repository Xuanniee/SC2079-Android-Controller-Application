package com.sc2079.androidcontroller.navigation

/**
 * Sealed class representing all navigation destinations in the app
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Bluetooth : Screen("bluetooth")
}
