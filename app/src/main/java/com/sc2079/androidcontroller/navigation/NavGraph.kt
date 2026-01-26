package com.sc2079.androidcontroller.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothScreen
import com.sc2079.androidcontroller.features.controller.presentation.ControllerScreen
import com.sc2079.androidcontroller.features.map.presentation.MapScreen
import com.sc2079.androidcontroller.ui.screens.ErrorScreen
import com.sc2079.androidcontroller.ui.screens.HomeScreen
import com.sc2079.androidcontroller.ui.screens.LoadingScreen

/**
 * Main navigation graph for the app
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
        composable(Screen.Loading.route) {
            LoadingScreen()
        }

        composable(Screen.Error.route) {
            ErrorScreen(
                onRetry = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Error.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToBluetooth = {
                    navController.navigate(Screen.Bluetooth.route)
                },
                onNavigateToController = {
                    navController.navigate(Screen.Controller.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.Map.route)
                }
            )
        }

        composable(Screen.Bluetooth.route) {
            BluetoothScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Controller.route) {
            ControllerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Map.route) {
            MapScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

