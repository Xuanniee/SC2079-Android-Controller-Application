package com.sc2079.androidcontroller.features.bluetooth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sc2079.androidcontroller.features.bluetooth.data.BluetoothClassicManager

class BluetoothVmFactory(
    private val manager: BluetoothClassicManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return BluetoothViewModel(manager) as T
    }
}
