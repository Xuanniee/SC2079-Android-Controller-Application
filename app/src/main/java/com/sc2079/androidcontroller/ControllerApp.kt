package com.sc2079.androidcontroller

import android.app.Application
import com.sc2079.androidcontroller.features.bluetooth.data.BluetoothClassicManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class ControllerApp : Application() {
    // App-Wide Scope that is created once when App is initialised
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Single BT manager instance for whole app process
    val bluetoothManager by lazy {
        BluetoothClassicManager(
            controllerAppContext = applicationContext,
            bluetoothScope = appScope
        )
    }

    override fun onCreate() {
        super.onCreate()
        android.util.Log.w(
            "BT_LIFECYCLE",
            "BT_TEST: ControllerApp onCreate hash=${System.identityHashCode(this)}"
        )
    }

}
