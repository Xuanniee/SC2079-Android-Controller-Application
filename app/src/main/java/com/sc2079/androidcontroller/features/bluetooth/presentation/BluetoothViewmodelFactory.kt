package com.sc2079.androidcontroller.features.bluetooth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sc2079.androidcontroller.features.bluetooth.data.BluetoothClassicManager

/**
 * Factory Pattern to create a ViewModel by injecting our BT Manager by extending from VMProvider
 */
class BluetoothViewmodelFactory(
    private val bluetoothManager: BluetoothClassicManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Suppress the Type Casting and return the ViewModel after creating it
        @Suppress("UNCHECKED_CAST")
        return BluetoothViewModel(bluetoothManager) as T
    }
}
