package com.sc2079.androidcontroller.features.bluetooth.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.R
import com.sc2079.androidcontroller.ui.components.FadeInAnimation
import com.sc2079.androidcontroller.ui.theme.SC2079AndroidControllerApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Data class representing a Bluetooth device
 */
data class BluetoothDevice(
    val name: String,
    val address: String,
    val rssi: Int // Signal strength in dBm
)

/**
 * Mock Bluetooth devices data
 */
private val mockDevices = listOf(
    BluetoothDevice(
        name = "Robot Controller 01",
        address = "00:11:22:33:44:55",
        rssi = -45
    ),
    BluetoothDevice(
        name = "Robot Controller 02",
        address = "AA:BB:CC:DD:EE:FF",
        rssi = -62
    ),
    BluetoothDevice(
        name = "Robot Controller 03",
        address = "12:34:56:78:90:AB",
        rssi = -78
    )
)

/**
 * Bluetooth connection screen
 */
@Composable
fun BluetoothScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var devices by remember { mutableStateOf<List<BluetoothDevice>>(emptyList()) }
    var visibleDevices by remember { mutableStateOf<Set<String>>(emptySet()) } // Track visible devices by address
    var isScanning by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.bluetooth_title),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = stringResource(R.string.bluetooth_connect_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Scan button - centered, not full width
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (devices.isNotEmpty()) {
                        // Animate out existing devices first
                        visibleDevices = emptySet()
                        isScanning = true
                        scope.launch {
                            delay(300) // Wait for fade-out animation to complete
                            devices = emptyList()
                            delay(500) // Simulate scan delay
                            devices = mockDevices
                            visibleDevices = mockDevices.map { it.address }.toSet()
                            isScanning = false
                        }
                    } else {
                        // No devices to animate out, start scanning immediately
                        isScanning = true
                        scope.launch {
                            delay(500) // Simulate scan delay
                            devices = mockDevices
                            visibleDevices = mockDevices.map { it.address }.toSet()
                            isScanning = false
                        }
                    }
                },
                shape = RoundedCornerShape(18.dp),
                enabled = !isScanning
            ) {
                Text(
                    text = if (isScanning) {
                        stringResource(R.string.loading)
                    } else {
                        stringResource(R.string.bluetooth_scan)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Device list
        if (devices.isEmpty() && !isScanning) {
            Text(
                text = stringResource(R.string.bluetooth_no_devices),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(devices) { index, device ->
                    val isVisible = visibleDevices.contains(device.address)
                    
                    if (isVisible) {
                        FadeInAnimation(
                            durationMillis = 300,
                            delayMillis = index * 100 // Stagger animation for each device
                        ) {
                            BluetoothDeviceCard(
                                device = device,
                                onConnectClick = {
                                    // Handle connect action
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bluetooth device card component
 */
@Composable
private fun BluetoothDeviceCard(
    device: BluetoothDevice,
    onConnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.bluetooth_device_address, device.address),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = stringResource(R.string.bluetooth_device_rssi, device.rssi),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            Button(
                onClick = onConnectClick,
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(text = stringResource(R.string.bluetooth_connect))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BluetoothScreenPreview() {
    SC2079AndroidControllerApplicationTheme {
        BluetoothScreen()
    }
}
