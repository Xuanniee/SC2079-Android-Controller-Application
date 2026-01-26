package com.sc2079.androidcontroller.navigation

/**
 * Sealed class representing all navigation destinations in the app
 */
sealed class Screen(val route: String) {
    data object Loading : Screen("loading")
    data object Error : Screen("error")
    data object Home : Screen("home")
    data object Bluetooth : Screen("bluetooth")
    data object Controller : Screen("controller")
    data object Map : Screen("map")
}

