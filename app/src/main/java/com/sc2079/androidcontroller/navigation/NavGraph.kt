package com.sc2079.androidcontroller.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel
import com.sc2079.androidcontroller.features.bluetooth.ui.BluetoothChatScreen
import com.sc2079.androidcontroller.features.bluetooth.ui.BluetoothSetupScreen
import com.sc2079.androidcontroller.features.map.ui.MappingHomeScreen
import com.sc2079.androidcontroller.ui.screens.HomeScreen

/**
 * Main navigation graph for the app
 * Home (Map view) + Bluetooth (Setup/Chat)
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    bluetoothViewModel: BluetoothViewModel,
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            HomeScreen(bluetoothViewModel = bluetoothViewModel)
        }

        composable(Screen.Bluetooth.route) {
            BluetoothFlowScreen(bluetoothViewModel = bluetoothViewModel)
        }

        composable(Screen.Map.route) {
            MappingHomeScreen(bluetoothViewModel = bluetoothViewModel)
        }
    }
}

@Composable
private fun BluetoothFlowScreen(
    bluetoothViewModel: BluetoothViewModel
) {
    var showChat by remember { mutableStateOf(false) }

    if (!showChat) {
        BluetoothSetupScreen(
            bluetoothViewModel = bluetoothViewModel,
            onOpenChat = { showChat = true }
        )
    } else {
        BluetoothChatScreen(
            bluetoothViewModel = bluetoothViewModel,
            onBack = { showChat = false }
        )
    }
}
