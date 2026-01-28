package com.sc2079.androidcontroller.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothScreen
import com.sc2079.androidcontroller.ui.screens.HomeScreen

/**
 * Main navigation graph for the app
 * Only Home (Map view) and Bluetooth screens
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }

        composable(Screen.Bluetooth.route) {
            BluetoothScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
