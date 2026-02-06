package com.sc2079.androidcontroller

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.sc2079.androidcontroller.features.bluetooth.data.BluetoothClassicManager
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewmodelFactory
import com.sc2079.androidcontroller.features.map.data.local.MapPreferencesDataSource
import com.sc2079.androidcontroller.features.map.data.repository.MapRepositoryImpl
import com.sc2079.androidcontroller.features.map.presentation.MapViewModel
import com.sc2079.androidcontroller.features.map.presentation.MapViewModelFactory
import com.sc2079.androidcontroller.features.language.presentation.LocaleState
import com.sc2079.androidcontroller.ui.AppScaffold
import com.sc2079.androidcontroller.ui.screens.LoadingScreen
import com.sc2079.androidcontroller.ui.theme.SC2079AndroidControllerApplicationTheme

class MainActivity : AppCompatActivity() {
    // Generic Dialog to request Permissions from User
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ -> }

    // Request Bluetooth Permissions
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize locale state and bluetooth viewModel
        LocaleState.initFromCurrentLocale()

        // Ensure the bluetoothManager is singleton and will not be destroyed when activity dies
        val bluetoothManager = (application as ControllerApp).bluetoothManager

//        val bluetoothManager = BluetoothClassicManager(
//            controllerAppContext = applicationContext,
//            bluetoothScope = lifecycleScope
//        )

        enableEdgeToEdge()
        setContent {
            SC2079AndroidControllerApplicationTheme {
                // Start by requesting for BT Permissions
                LaunchedEffect(Unit) { requestBluetoothPermissions() }

                // Initialise ViewModels
                val bluetoothViewModel: BluetoothViewModel = viewModel(
                    factory = BluetoothViewmodelFactory(bluetoothManager)
                )

                // Initialise the Map Repo and Viewmodel at Activity level as well
                val context = LocalContext.current
                val repo = remember {
                    MapRepositoryImpl(MapPreferencesDataSource(context.applicationContext))
                }
                val mapViewModel: MapViewModel = viewModel(
                    factory = MapViewModelFactory(repo)
                )

                Surface(modifier = Modifier.fillMaxSize()) {
                    MainContent(
                        bluetoothViewModel = bluetoothViewModel,
                        mapViewModel = mapViewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.w("BT_LIFECYCLE", "BT_TEST: MainActivity onDestroy isFinishing=\$isFinishing activityHash=\${System.identityHashCode(this)}")
    }
}

/**
 * Composable for the App's Main Content
 */
@Composable
private fun MainContent(
    bluetoothViewModel: BluetoothViewModel,
    mapViewModel: MapViewModel
) {
    val isChangingLanguage by LocaleState.isChangingLanguage

    Box(modifier = Modifier.fillMaxSize()) {
        val navController = rememberNavController()

        /**
         * UI Of the Entire Controller
         *
         * Passes it the NavController and ViewModels
         */
        AppScaffold(
            navController = navController,
            bluetoothViewModel = bluetoothViewModel,
            mapViewModel = mapViewModel
        )

        if (isChangingLanguage) {
            LoadingScreen(
                message = stringResource(R.string.changing_language),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}


/* =====================================================================================
   BELOW IS XUAN YI ORIGINAL VERSION
   =====================================================================================

import android.Manifest
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sc2079.androidcontroller.features.bluetooth.data.BluetoothClassicManager
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewmodelFactory
import com.sc2079.androidcontroller.features.bluetooth.ui.BluetoothChatScreen
import com.sc2079.androidcontroller.features.bluetooth.ui.BluetoothSetupScreen
import com.sc2079.androidcontroller.features.map.ui.screens.MappingHomeScreen

/**
 * Entry Point for the Android Controller.
 *
 * Requests for BT Permissions before running the Setup for the Controller.
 *
class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ -> }

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
                LaunchedEffect(Unit) { requestBluetoothPermissions() }

                val bluetoothViewModel: BluetoothViewModel = viewModel(
                    factory = BluetoothViewmodelFactory(bluetoothManager)
                )

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

                    // KEY: apply scaffold padding (accounts for bottom bar) + IME padding
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .imePadding()
                    ) {
                        when (currentScreen) {
                            MainScreen.Setup -> BluetoothSetupScreen(
                                bluetoothViewModel = bluetoothViewModel,
                                onOpenChat = { currentScreen = MainScreen.Chat }
                            )

                            MainScreen.Chat -> BluetoothChatScreen(
                                bluetoothViewModel = bluetoothViewModel,
                                onBack = { currentScreen = MainScreen.Setup }
                            )

                            MainScreen.Map -> MappingHomeScreen(
                                bluetoothViewModel = bluetoothViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}

*/