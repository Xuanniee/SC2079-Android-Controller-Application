package com.sc2079.androidcontroller.features.bluetooth.domain

import android.bluetooth.BluetoothDevice

sealed class BluetoothConnState {
    data object Disconnected : BluetoothConnState()
    data class Connecting(val deviceName: String) : BluetoothConnState()
    data class Connected(val deviceName: String, val device: BluetoothDevice) : BluetoothConnState()
    data class Error(val message: String) : BluetoothConnState()
}

data class ChatMessage(
    val text: String,
    val isIncoming: Boolean
)
