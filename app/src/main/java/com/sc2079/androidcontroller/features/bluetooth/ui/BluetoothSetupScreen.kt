package com.sc2079.androidcontroller.features.bluetooth.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

        // All buttons in one row with squarish shape, rounded 16.dp borders, secondary background, and icons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Load Paired button
            BluetoothActionButton(
                onClick = { bluetoothViewModel.retrievePairedDevices() },
                icon = Icons.Default.Refresh,
                label = "Load Paired",
                modifier = Modifier.weight(1f)
            )

            // Scan / Stop Scan button with smooth transitions
            val isScanningActive = bluetoothUiState.isScanning
            BluetoothActionButton(
                onClick = {
                    if (isScanningActive) {
                        bluetoothViewModel.stopBluetoothScan()
                    } else {
                        bluetoothViewModel.startBluetoothScan()
                    }
                },
                icon = if (isScanningActive) Icons.Default.Cancel else Icons.Default.Search,
                label = if (isScanningActive) "Stop Scan" else "Scan",
                backgroundColor = if (isScanningActive) MaterialTheme.colorScheme.tertiaryContainer 
                                  else MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            )

            // Host (Server) button
            val isHostingActive = bluetoothUiState.bluetoothConnState is BluetoothConnState.Listening
            BluetoothActionButton(
                onClick = { bluetoothViewModel.hostBluetoothServer() },
                icon = Icons.Default.Settings,
                label = "Host",
                backgroundColor = if (isHostingActive) MaterialTheme.colorScheme.tertiaryContainer 
                                  else MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.weight(1f)
            )

            // Make Discoverable button
            BluetoothActionButton(
                onClick = {
                    val intent = Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                        putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
                    }
                    deviceDiscoverableLauncher.launch(intent)
                },
                icon = Icons.Default.Visibility,
                label = "Discoverable",
                modifier = Modifier.weight(1f)
            )

            // Connect button
            BluetoothActionButton(
                onClick = { bluetoothViewModel.connectSelectedDevice() },
                icon = Icons.Default.Link,
                label = "Connect",
                enabled = bluetoothUiState.selectedDeviceAddress != null,
                modifier = Modifier.weight(1f)
            )

            // Disconnect button (red background when not disconnected, disabled when disconnected)
            val isDisconnected = bluetoothUiState.bluetoothConnState is BluetoothConnState.Disconnected
            BluetoothActionButton(
                onClick = { bluetoothViewModel.disconnect() },
                icon = Icons.Default.Close,
                label = "Disconnect",
                enabled = !isDisconnected,
                backgroundColor = if (isDisconnected) MaterialTheme.colorScheme.secondaryContainer 
                                  else MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f)
            )
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

/**
 * Custom Bluetooth action button with squarish shape, rounded corners, secondary background, and icon
 * Includes fade-in animations for UI changes
 */
@Composable
private fun BluetoothActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    modifier: Modifier = Modifier
) {
    // Animate background color changes with fade-in effect
    val animatedBackgroundColor by animateColorAsState(
        targetValue = if (enabled) backgroundColor else backgroundColor.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 300),
        label = "backgroundColor"
    )
    
    val isErrorColor = backgroundColor == MaterialTheme.colorScheme.error
    val isTertiaryColor = backgroundColor == MaterialTheme.colorScheme.tertiaryContainer
    val targetContentColor = when {
        isErrorColor -> MaterialTheme.colorScheme.onError
        isTertiaryColor -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSecondaryContainer
    }
    
    // Animate content color changes with fade-in effect
    val animatedContentColor by animateColorAsState(
        targetValue = if (enabled) targetContentColor else targetContentColor.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 300),
        label = "contentColor"
    )
    
    // Animate opacity for fade-in effect
    val animatedAlpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.5f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )
    
    Box(
        modifier = modifier
            .height(64.dp)
            .alpha(animatedAlpha)
            .clip(RoundedCornerShape(16.dp))
            .background(
                animatedBackgroundColor,
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = animatedContentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = animatedContentColor
            )
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