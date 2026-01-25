package com.sc2079.androidcontroller.features.bluetooth.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.sc2079.androidcontroller.features.bluetooth.domain.BluetoothConnState
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel

/**
 * Simple Map Function to translate curr Bluetooth State to strings describing btstate
 */
private fun formatBluetoothConnState(
    btState: BluetoothConnState
): String = when (btState) {
    is BluetoothConnState.Disconnected -> "Disconnected"
    is BluetoothConnState.Listening -> "Listening (${btState.serviceName})"
    is BluetoothConnState.Connecting -> "Connecting to ${btState.name}"
    is BluetoothConnState.Connected -> "Connected to ${btState.name}"
    is BluetoothConnState.Error -> "Error: ${btState.msg}"
}

/**
 * Default Screen for Connecting to Robot via Bluetooth
 */
@Composable
fun BluetoothSetupScreen(
    bluetoothViewModel: BluetoothViewModel,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    // This screen should not hold data, but collect the uiState data from the ViewModel
    // ViewModel would inform the screen of any uistate changes to recompute the UI
    val bluetoothUiState by bluetoothViewModel.bluetoothUiState.collectAsState()

    /**
     * Previously, Tablet could not be discovered by the AMD Tool even though Tablet was server
     * because it was not discoverable.
     *
     * Asks user to make device visible to other apps via a sys dialog
     */
    val deviceDiscoverableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    // UI Elements of Setup Page from Top to Bottom
    Column(
        modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Page Name
        Text("Bluetooth Setup", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(12.dp))

        // Connection Status
        Text("Status: ${formatBluetoothConnState(bluetoothUiState.bluetoothConnState)}")

        bluetoothUiState.lastError?.let {
            Spacer(Modifier.height(8.dp))
            Text("Error: $it", color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { bluetoothViewModel.retrievePairedDevices() }
            ) {
                Text("Load Paired")
            }
            if (!bluetoothUiState.isScanning) {
                Button(
                    onClick = { bluetoothViewModel.startBluetoothScan() }
                ) {
                    Text("Scan")
                }
            } else {
                OutlinedButton(
                    onClick = { bluetoothViewModel.stopBluetoothScan() }
                ) {
                    Text("Stop Scan")
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    bluetoothViewModel.hostBluetoothServer() }
            ) {
                Text("Host (Server)")
            }

            OutlinedButton(
                onClick = {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                    }
                    deviceDiscoverableLauncher.launch(intent)
                }
            ) {
                Text("Make Discoverable")
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { bluetoothViewModel.connectSelectedDevice() },
                enabled = bluetoothUiState.selectedDeviceAddress != null
            ) {
                Text("Connect")
            }

            OutlinedButton(
                onClick = {
                    bluetoothViewModel.disconnect()
                }
            ) {
                Text("Disconnect")
            }
        }

        Spacer(Modifier.height(12.dp))

        // No direct BluetoothDevice.name access in UI (avoids MissingPermission lint)
        Text("Selected: ${bluetoothUiState.selectedDeviceName}")
        bluetoothUiState.selectedDeviceAddress?.let { addr ->
            Text(addr, style = MaterialTheme.typography.bodySmall)
        }

        Spacer(Modifier.height(8.dp))

        Text("Paired Devices", style = MaterialTheme.typography.titleMedium)
        DeviceList(
            devices = bluetoothUiState.pairedBtDevices,
            selectedAddress = bluetoothUiState.selectedDeviceAddress,
            onSelect = bluetoothViewModel::selectDevice
        )

        Spacer(Modifier.height(12.dp))

        Text("Discovered Devices", style = MaterialTheme.typography.titleMedium)
        DeviceList(
            devices = bluetoothUiState.discoveredBtDevices,
            selectedAddress = bluetoothUiState.selectedDeviceAddress,
            onSelect = bluetoothViewModel::selectDevice
        )

        Spacer(Modifier.height(12.dp))

        val connectedChat = bluetoothUiState.bluetoothConnState is BluetoothConnState.Connected
        Button(
            onClick = onOpenChat,
            enabled = connectedChat,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Open Chat")
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<BluetoothDevice>,
    selectedAddress: String?,
    onSelect: (BluetoothDevice) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(devices, key = { it.address }) { d ->
            val isSel = selectedAddress == d.address

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(d) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    // Avoid d.name entirely in UI to remove MissingPermission lint.
                    Text(d.address)
                    Text(
                        text = if (isSel) "Selected" else "Tap to select",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}