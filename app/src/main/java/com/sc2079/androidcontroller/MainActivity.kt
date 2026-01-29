package com.sc2079.androidcontroller

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel

import com.sc2079.androidcontroller.features.bluetooth.data.BluetoothClassicManager
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewmodelFactory
import com.sc2079.androidcontroller.features.bluetooth.ui.BluetoothChatScreen
import com.sc2079.androidcontroller.features.bluetooth.ui.BluetoothSetupScreen
import com.sc2079.androidcontroller.features.map.ui.MappingHomeScreen // <-- adjust if your file name differs
import com.sc2079.androidcontroller.ui.theme.SC2079AndroidControllerApplicationTheme

/**
 * Entry Point for the Android Controller.
 *
 * Requests for BT Permissions before running the Setup for the Controller.
 */
class MainActivity : ComponentActivity() {
    // Creates a Launcher Dialog to handle the results of Permission requests
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ -> }

    // Helper to request for BT in Android
    private fun requestBluetoothPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        permissionLauncher.launch(permissions)
    }

    private enum class MainScreen { Setup, Chat, Map }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bluetoothManager = BluetoothClassicManager(
            controllerAppContext = applicationContext,
            bluetoothScope = lifecycleScope
        )

        setContent {
            SC2079AndroidControllerApplicationTheme {
                LaunchedEffect(Unit) {
                    requestBluetoothPermissions()
                }

                val bluetoothViewModel: BluetoothViewModel = viewModel(
                    factory = BluetoothViewmodelFactory(bluetoothManager)
                )

                // Default to Setup screen
                var currentScreen by remember { mutableStateOf(MainScreen.Setup) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = currentScreen == MainScreen.Setup,
                                onClick = { currentScreen = MainScreen.Setup },
                                label = { Text("Setup") },
                                icon = {}
                            )
                            NavigationBarItem(
                                selected = currentScreen == MainScreen.Chat,
                                onClick = { currentScreen = MainScreen.Chat },
                                label = { Text("Chat") },
                                icon = {}
                            )
                            NavigationBarItem(
                                selected = currentScreen == MainScreen.Map,
                                onClick = { currentScreen = MainScreen.Map },
                                label = { Text("Map") },
                                icon = {}
                            )
                        }
                    }
                ) { innerPadding ->
                    val contentModifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)

                    when (currentScreen) {
                        MainScreen.Setup -> {
                            // BT Setup screen
                            BluetoothSetupScreen(
                                bluetoothViewModel = bluetoothViewModel,
                                onOpenChat = { currentScreen = MainScreen.Chat }
                            )
                        }

                        MainScreen.Chat -> {
                            // Chat screen
                            BluetoothChatScreen(
                                bluetoothViewModel = bluetoothViewModel,
                                onBack = { currentScreen = MainScreen.Setup }
                            )
                        }

                        MainScreen.Map -> {
                            // Mapping module home screen (C5/C6/C7/C9/C10)
                            // It needs the same bluetoothViewModel to receive/send messages.
                            // If you named it differently, update this call.
                            Surface(modifier = contentModifier) {
                                MappingHomeScreen(bluetoothViewModel = bluetoothViewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}


//package com.sc2079.androidcontroller
//
//import android.Manifest
//import android.os.Build
//import android.os.Bundle
//import androidx.activity.ComponentActivity
//import androidx.activity.compose.setContent
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.material3.Scaffold
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Modifier
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.sc2079.androidcontroller.features.bluetooth.data.BluetoothClassicManager
//import com.sc2079.androidcontroller.features.bluetooth.ui.BluetoothChatScreen
//import com.sc2079.androidcontroller.features.bluetooth.ui.BluetoothSetupScreen
//import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel
//import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewmodelFactory
//import com.sc2079.androidcontroller.ui.theme.SC2079AndroidControllerApplicationTheme
//
///**
// * Entry Point for the Android Controller.
// *
// * Requests for BT Permissions before running the Setup for the Controller.
// */
//class MainActivity : ComponentActivity() {
//    // Creates a Launcher Dialog to handle the results of Permission requests
//    private val permissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
//            _ ->
//        }
//
//    // Helper to request for BT in Android
//    private fun requestBluetoothPermissions() {
//        // Check if device is running Android 12 or higher
//        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            // Android 12 BT Scanning & Connect Permissions
//            arrayOf(
//                Manifest.permission.BLUETOOTH_CONNECT,
//                Manifest.permission.BLUETOOTH_SCAN
//            )
//        } else {
//            // Older Androids need Location Permission for BT
//            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
//        }
//        // Trigger the pop-up to request permission from User
//        permissionLauncher.launch(permissions)
//    }
//
//    // Override the onCreate Hook to set up the Controller
//    override fun onCreate(savedInstanceState: Bundle?) {
//        // Restores any UI States when Activity changes like screen rotations to prevent data loss
//        super.onCreate(savedInstanceState)
//
//        // Create Bluetooth manager with applicationContext to avoid crashes if Activity rotates
//        val bluetoothManager = BluetoothClassicManager(
//            // Holds system resources for application to use for a particular Activity/Screen
//            // Allows BT to run even when Activity is destroyed when scrreen is rotated
//            controllerAppContext = applicationContext,
//            // Coroutine scope to allow app to run background BT tasks not on the Main Thread for
//            // responsiveness
//            bluetoothScope = lifecycleScope
//        )
//
//        /**
//         * Main "Hook" to allow us to define the main UI for our users to use
//         */
//        setContent {
//            // Wrapper for Themes
//            SC2079AndroidControllerApplicationTheme {
//                // Ensures we only request for Permission once even though the setContent might run
//                // multiple times when Activity rotates
//                LaunchedEffect(Unit) {
//                    requestBluetoothPermissions()
//                }
//
//                /**
//                 * Creates my BluetoothViewmodel based on my Bluetooth Manager template
//                 * to store Bluetooth UI data to survive config changes
//                 */
//                val bluetoothViewModel: BluetoothViewModel = viewModel(
//                    factory = BluetoothViewmodelFactory(bluetoothManager)
//                )
//
//                // Variable to track if we should trigger recomposition of UI i.e. switch to a diff screen
//                var showChat by remember { mutableStateOf(false) }
//
//                // Provides a template to create a full-screen container
//                Scaffold(
//                    modifier = Modifier.fillMaxSize()
//                ) { innerPadding ->
//                        // Modifier that mak
//                        val contentModifier = Modifier
//                            .fillMaxSize()
//                            .padding(innerPadding)
//
//                        // Use the mutableState earlier to track which screen we should show first
//                        // Presently only 2 screens so is sufficient
//                        // TODO should shift the logic of deciding which screen to use to a
//                        //  different HomeScreen.kt file but only 1 module for now
//                        if (!showChat) {
//                            // BT Setup screen (Future HomeScreen)
//                            BluetoothSetupScreen(
//                                bluetoothViewModel = bluetoothViewModel,
//                                onOpenChat = { showChat = true }
//                            )
//                        } else {
//                            // Chat screen - For sending messages
//                            BluetoothChatScreen(
//                                bluetoothViewModel = bluetoothViewModel,
//                                onBack = { showChat = false }
//                            )
//                        }
//                }
//            }
//        }
//    }
//
//
//}
