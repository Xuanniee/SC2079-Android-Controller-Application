package com.sc2079.androidcontroller

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sc2079.androidcontroller.features.bluetooth.data.BluetoothClassicManager
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothChatScreen
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothSetupScreen
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothVmFactory
import com.sc2079.androidcontroller.ui.theme.SC2079AndroidControllerApplicationTheme

class MainActivity : ComponentActivity() {

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            // No-op: manager/vm will surface errors if permission denied
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Create Bluetooth manager once (applicationContext avoids leaking Activity)
        val manager = BluetoothClassicManager(
            appContext = applicationContext,
            scope = lifecycleScope
        )

        setContent {
            SC2079AndroidControllerApplicationTheme {
                // Request permissions once at launch
                LaunchedEffect(Unit) {
                    requestBluetoothPermissions()
                }

                val vm: BluetoothViewModel = viewModel(
                    factory = BluetoothVmFactory(manager)
                )

                var showChat by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val contentModifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)

                    if (!showChat) {
                        // Setup screen
                        // (Your SetupScreen already has its own padding; if not, pass modifier in your composable)
                        BluetoothSetupScreen(
                            vm = vm,
                            onOpenChat = { showChat = true }
                        )
                    } else {
                        // Chat screen
                        BluetoothChatScreen(
                            vm = vm,
                            onBack = { showChat = false }
                        )
                    }
                }
            }
        }
    }

    private fun requestBluetoothPermissions() {
        val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN
            )
        } else {
            // Pre-Android 12 scanning usually requires location
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        permissionLauncher.launch(perms)
    }
}
