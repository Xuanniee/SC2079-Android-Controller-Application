package com.sc2079.androidcontroller.features.bluetooth.domain

import android.bluetooth.BluetoothDevice

/**
 * Domain Layer of Android App
 *
 * Defines how our data would look like.
 */
data class Message(
    // Flag to determine if Robot or App sent the Command
    val fromRobot: Boolean,
    // Command/Message from either the App or Robot
    val messageBody: String
)

// SSOT for BT UI State. Each var is explained in detail in the ViewModel
data class BluetoothUiState(
    val bluetoothConnState: BluetoothConnState = BluetoothConnState.Disconnected,
    val pairedBtDevices: List<BluetoothDevice> = emptyList(),
    val discoveredBtDevices: List<BluetoothDevice> = emptyList(),
    val selectedDeviceName: String = "-",
    val selectedDeviceAddress: String? = null,
    val isScanning: Boolean = false,
    val messages: List<Message> = emptyList(),
    val lastError: String? = null
)

// Enum Class to indicate the possible BT Conn States
sealed class BluetoothConnState {
    data object Disconnected : BluetoothConnState()
    data class Listening(val serviceName: String) : BluetoothConnState()
    data class Connecting(val name: String) : BluetoothConnState()
    data class Connected(val name: String, val device: BluetoothDevice?) : BluetoothConnState()
    data class Error(val msg: String) : BluetoothConnState()
}