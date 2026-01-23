package com.sc2079.androidcontroller.features.bluetooth.presentation

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.features.bluetooth.domain.BluetoothConnState

@Composable
fun BluetoothSetupScreen(
    vm: BluetoothViewModel,
    onOpenChat: () -> Unit
) {
    val ui by vm.uiState.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = when (val s = ui.connState) {
                BluetoothConnState.Disconnected -> "Disconnected"
                is BluetoothConnState.Connecting -> "Connecting to ${s.deviceName}..."
                is BluetoothConnState.Connected -> "Connected: ${s.deviceName}"
                is BluetoothConnState.Error -> "Error: ${s.message}"
            }
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::refreshPaired) { Text("Load Paired") }
            if (ui.isScanning) {
                OutlinedButton(onClick = vm::stopScan) { Text("Stop Scan") }
            } else {
                OutlinedButton(onClick = vm::startScan) { Text("Scan") }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("Paired devices", style = MaterialTheme.typography.titleMedium)
        DeviceList(
            devices = ui.pairedDevices,
            selected = ui.selectedDevice,
            onSelect = vm::selectDevice
        )

        Spacer(Modifier.height(12.dp))

        Text("Discovered devices", style = MaterialTheme.typography.titleMedium)
        DeviceList(
            devices = ui.discoveredDevices,
            selected = ui.selectedDevice,
            onSelect = vm::selectDevice
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = vm::connectSelected,
                enabled = ui.selectedDevice != null
            ) { Text("Connect") }

            OutlinedButton(onClick = vm::disconnect) { Text("Disconnect") }

            Button(
                onClick = onOpenChat,
                enabled = ui.connState is BluetoothConnState.Connected
            ) { Text("Open Chat") }
        }
    }
}

@Composable
private fun DeviceList(
    devices: List<BluetoothDevice>,
    selected: BluetoothDevice?,
    onSelect: (BluetoothDevice) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(devices, key = { it.address }) { d ->
            val isSelected = selected?.address == d.address
            Surface(
                tonalElevation = if (isSelected) 6.dp else 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(d) }
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(d.name ?: "(no name)")
                    Text(d.address, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

//package com.sc2079.androidcontroller.features.bluetooth.ui
//
//import android.bluetooth.BluetoothDevice
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.heightIn
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewmodel
//
//@Composable
//fun BluetoothSetupScreen(
//    bluetoothViewmodel: BluetoothViewmodel,
//    modifier: Modifier = Modifier,
//) {
//    // Collect the Bluetooth UI State from the ViewModel
//    val bluetoothUiState by bluetoothViewmodel.bluetoothState.collect
//
//    Column(
//        modifier = modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        Text(
//            text = when ()
//        )
//
//        Spacer(modifier = modifier.height(12.dp))
//
//        Text("Paired Devices: ", style = MaterialTheme.typography.titleMedium)
//
//    }
//}
//
//@Composable
//private fun BluetoothDeviceList(
//    bluetoothDevices: List<BluetoothDevice>,
//    selectedDevice: BluetoothDevice?,
//    onSelect: (BluetoothDevice) -> Unit,
//    modifier: Modifier = Modifier,
//) {
//    LazyColumn(
//        modifier = modifier
//            .fillMaxWidth()
//            .heightIn(max = 220.dp),
//        verticalArrangement = Arrangement.spacedBy(6.dp)
//    ) {
//
//
//    }
//
//}