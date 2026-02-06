package com.sc2079.androidcontroller.features.bluetooth.ui

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
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
 * Bluetooth connection screen where tapping a device initiates connection.
 * Paired devices are auto-loaded on first render.
 */
@Composable
fun BluetoothConnectionScreen(
    bluetoothViewModel: BluetoothViewModel,
    onOpenChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bluetoothUiState by bluetoothViewModel.bluetoothUiState.collectAsState()

    // Auto-load paired devices on first render
    LaunchedEffect(Unit) {
        bluetoothViewModel.retrievePairedDevices()
    }

    val deviceDiscoverableLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize()
    ) {
        val minDimension = minOf(maxWidth, maxHeight)
        val isMobile = minDimension < 600.dp

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
            // Page Name
            Text("Bluetooth Connection", style = MaterialTheme.typography.titleLarge)

            Spacer(Modifier.height(12.dp))

            // Connection Status
            Text("Status: ${formatBluetoothConnState(bluetoothUiState.bluetoothConnState)}")

            bluetoothUiState.lastError?.let {
                Spacer(Modifier.height(8.dp))
                Text("Error: $it", color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(12.dp))

            // Bluetooth Animation Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        BluetoothLottieAnimation(
                            isScanning = bluetoothUiState.isScanning
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Scanning status text
                    Text(
                        text = if (bluetoothUiState.isScanning) {
                            "Scanning for devices"
                        } else {
                            "Press the scan button to scan for devices"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (isMobile) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Row: Scan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                    }

                    // Row: Host, Discoverable
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val isHostingActive = bluetoothUiState.bluetoothConnState is BluetoothConnState.Listening
                        BluetoothActionButton(
                            onClick = { bluetoothViewModel.hostBluetoothServer() },
                            icon = Icons.Default.Settings,
                            label = "Host",
                            backgroundColor = if (isHostingActive) MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.weight(1f)
                        )

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
                    }

                    // Row: Disconnect only (connect handled by tapping device)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
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
                }
            } else {
                // Tablet layout: single row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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

                    val isHostingActive = bluetoothUiState.bluetoothConnState is BluetoothConnState.Listening
                    BluetoothActionButton(
                        onClick = { bluetoothViewModel.hostBluetoothServer() },
                        icon = Icons.Default.Settings,
                        label = "Host",
                        backgroundColor = if (isHostingActive) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.weight(1f)
                    )

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
            }

            Spacer(Modifier.height(12.dp))

            // Only show Paired Devices section if there are paired devices
            if (bluetoothUiState.pairedBtDevices.isNotEmpty()) {
                Text("Paired Devices", style = MaterialTheme.typography.titleMedium)
                DeviceListConnect(
                    devices = bluetoothUiState.pairedBtDevices,
                    selectedAddress = bluetoothUiState.selectedDeviceAddress,
                    onSelectAndConnect = { device ->
                        bluetoothViewModel.selectDevice(device)
                        bluetoothViewModel.connectSelectedDevice()
                    }
                )

                Spacer(Modifier.height(12.dp))
            }

            Text("Discovered Devices", style = MaterialTheme.typography.titleMedium)
            DeviceListConnect(
                devices = bluetoothUiState.discoveredBtDevices,
                selectedAddress = bluetoothUiState.selectedDeviceAddress,
                onSelectAndConnect = { device ->
                    bluetoothViewModel.selectDevice(device)
                    bluetoothViewModel.connectSelectedDevice()
                }
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
    }
}

@Composable
private fun DeviceListConnect(
    devices: List<BluetoothDevice>,
    selectedAddress: String?,
    onSelectAndConnect: (BluetoothDevice) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 220.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(devices, key = { it.address }) { d ->
            val isSel = selectedAddress == d.address

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectAndConnect(d) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer
                    else MaterialTheme.colorScheme.surface
                )
            ) {
                Column(Modifier.padding(12.dp)) {
                    val primaryName = d.name ?: d.address
                    Text(primaryName)
                    Text(
                        text = d.address,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun BluetoothActionButton(
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    enabled: Boolean = true,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    modifier: Modifier = Modifier
) {
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

    val animatedContentColor by animateColorAsState(
        targetValue = if (enabled) targetContentColor else targetContentColor.copy(alpha = 0.5f),
        animationSpec = tween(durationMillis = 300),
        label = "contentColor"
    )

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

/**
 * Bluetooth Lottie Animation Component
 * Shows animation when scanning is enabled, static when not scanning
 * When scanning stops, plays reverse until start (progress = 0), then stops
 */
@Composable
private fun BluetoothLottieAnimation(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    // Load the Lottie composition from assets folder
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("Bluetooth circles.lottie")
    )

    // Track previous scanning state to detect transition
    var previousScanning by remember { mutableStateOf(isScanning) }
    var isReversing by remember { mutableStateOf(false) }
    var shouldStop by remember { mutableStateOf(false) }

    // Detect when scanning transitions from true to false
    LaunchedEffect(isScanning) {
        if (previousScanning && !isScanning) {
            // Scanning just stopped - start reverse playback
            isReversing = true
            shouldStop = false
        } else if (isScanning) {
            // Scanning started - reset states
            isReversing = false
            shouldStop = false
        }
        previousScanning = isScanning
    }

    // Animate the composition with proper state management
    val animationState = animateLottieCompositionAsState(
        composition = composition,
        iterations = if (isScanning) Int.MAX_VALUE else 1,
        isPlaying = isScanning || (isReversing && !shouldStop),
        speed = if (isReversing && !isScanning) -1f else 1f, // Reverse when stopping
        restartOnPlay = false
    )

    // Monitor progress when reversing - stop when it reaches 0 (start)
    LaunchedEffect(isReversing, animationState.progress) {
        if (isReversing && !isScanning) {
            // Check if we've reached the start (progress = 0 or very close)
            if (animationState.progress <= 0f || animationState.progress < 0.01f) {
                shouldStop = true
                isReversing = false
            }
        }
    }

    // Background color is always bg secondary (doesn't change when scanning)
    val backgroundColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { if (shouldStop && !isScanning) 0f else animationState.progress },
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
