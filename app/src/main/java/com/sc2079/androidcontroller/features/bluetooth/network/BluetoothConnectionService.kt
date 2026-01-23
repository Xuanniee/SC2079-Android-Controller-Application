package com.sc2079.androidcontroller.features.bluetooth.network
//
//import android.Manifest
//import android.bluetooth.BluetoothDevice
//import android.bluetooth.BluetoothManager
//import android.bluetooth.BluetoothSocket
//import android.content.Context
//import android.content.pm.PackageManager
//import android.os.Build
//import androidx.annotation.RequiresPermission
//import androidx.core.content.ContextCompat
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.Job
//import kotlinx.coroutines.flow.MutableSharedFlow
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asSharedFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.launch
//import java.io.BufferedReader
//import java.io.InputStreamReader
//import java.io.OutputStreamWriter
//import java.util.UUID
//
//import com.sc2079.androidcontroller.features.bluetooth.domain.BluetoothConnState
//
//class BluetoothConnectionService (
//    private val scope: CoroutineScope
//) {
//    /**
//     * Track the various possible Bluetooth Connection States
//     */
//    // Variables & StateFlows to track various states
//    private val _bluetoothState = MutableStateFlow<BluetoothConnState>(
//        BluetoothConnState.Disconnected
//    )
//    val bluetoothState = _bluetoothState.asStateFlow()
//
//    // SharedFlow useful for streaming messages
//    private val _incomingMessages = MutableSharedFlow<String>(
//        extraBufferCapacity = 64
//    )
//    val incomingMessages = _incomingMessages.asSharedFlow()
//
//    // Sockets & I/O Vars
//    private var bluetoothSocket: BluetoothSocket? = null
//    private var bluetoothWriter: OutputStreamWriter? = null
//    private var bluetoothReader: Job? = null
//
//    // Standard Serial Port Profile (SPP) UUID for Bluetooth
//    // TODO Check this first if fails to connect via BT
//    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
//
//    // Simple BT Checker Function - Returns True if we have permission
//    fun checkBluetoothConnectPermission(context: Context): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            ContextCompat.checkSelfPermission(
//                context,
//                Manifest.permission.BLUETOOTH_CONNECT
//            ) == PackageManager.PERMISSION_GRANTED
//        } else {
//            true
//        }
//    }
//
//    // Retrieves a list of Android devices previously connected via BT
//    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
//    fun getBondedDevices(context: Context): List<BluetoothDevice> {
//        // Check if the User has provided permissions to access the devices' bonded list
//        if (!checkBluetoothConnectPermission(context)) return emptyList()
//
//        // Retrieve the local bluetooth adapter on the device
//        val bluetoothManager = context.getSystemService(BluetoothManager::class.java) ?: return emptyList()
//        val localBluetoothAdapter = bluetoothManager.adapter ?: return emptyList()
//
//        return try {
//            localBluetoothAdapter.bondedDevices?.toList().orEmpty()
//        } catch (se: SecurityException) {
//            emptyList()
//        }
//    }
//
//    // Attempt to connect to a particular BT device
//    fun connect(device: BluetoothDevice, context: Context) {
//        // Ensure no old sockets are open
//        disconnect()
//
//        // Retrieve the local bluetooth adapter on the device
//        val bluetoothManager = context.getSystemService(BluetoothManager::class.java)
//        val localBluetoothAdapter = bluetoothManager.adapter
//
//        // Check if Bluetooth is supported and enabled
//        if (localBluetoothAdapter == null) {
//            _bluetoothState.value =
//                BluetoothConnState.Error("Bluetooth not supported on this device")
//            return
//        }
//        if (!localBluetoothAdapter.isEnabled) {
//            _bluetoothState.value =
//                BluetoothConnState.Error("Bluetooth is disabled. Please enable " +
//                        "Bluetooth.")
//            return
//        }
//        // Ensure that users are still allowing us to use BT during runtime
//        if (!checkBluetoothConnectPermission(context)) {
//            // Does not have permission
//            _bluetoothState.value =
//                BluetoothConnState.Error("User did not provide runtime " +
//                        "permission to use Bluetooth..")
//            return
//        }
//
//        // Resolve device name
//        val deviceName = device.name ?: device.address
//        _bluetoothState.value = BluetoothConnState.Connecting(deviceName)
//
//        // Use a coroutine to run the BT work in the background
//        scope.launch(Dispatchers.IO) {
//            try {
//                // Cancel BT discovery because it slows down connection
//                localBluetoothAdapter.cancelDiscovery()
//
//                // Create a SPP Socket that is blocking
//                val sock = device.createRfcommSocketToServiceRecord(sppUuid)
//                sock.connect()
//
//                bluetoothSocket = sock
//                bluetoothWriter = OutputStreamWriter(sock.outputStream)
//
//
//                // Successful -> Open I/O Stream
//                _bluetoothState.value = BluetoothConnState.Connected(deviceName)
//                startReaderLoop(sock)
//            } catch (e: Exception) {
//                _bluetoothState.value =
//                    BluetoothConnState.Error("Connect failed: ${e.message}")
//                disconnect()
//            }
//        }
//    }
//
//    /**
//     * Rece
//     */
//    private fun startReaderLoop(sock: BluetoothSocket) {
//        bluetoothReader?.cancel()
//        bluetoothReader = scope.launch(Dispatchers.IO) {
//            try {
//                val br = BufferedReader(InputStreamReader(sock.inputStream))
//                while (true) {
//                    val line = br.readLine() ?: break // null means disconnected
//                    _incomingMessages.tryEmit(line)
//                }
//                _bluetoothState.value = BluetoothConnState.Disconnected
//            } catch (e: Exception) {
//                _bluetoothState.value =
//                    BluetoothConnState.Error("Disconnected: ${e.message}")
//            } finally {
//                disconnect()
//            }
//        }
//    }
//
//    fun sendLine(line: String) {
//        scope.launch(Dispatchers.IO) {
//            try {
//                val w = bluetoothWriter ?: throw IllegalStateException("Not connected")
//                w.write(line)
//                w.write("\n")
//                w.flush()
//            } catch (e: Exception) {
//                _bluetoothState.value =
//                    BluetoothConnState.Error("Send failed: ${e.message}")
//                disconnect()
//            }
//        }
//    }
//
//    fun disconnect() {
//        try { bluetoothReader?.cancel() } catch (_: Exception) {}
//        bluetoothReader = null
//
//        try { bluetoothWriter?.close() } catch (_: Exception) {}
//        bluetoothWriter = null
//
//        try { bluetoothSocket?.close() } catch (_: Exception) {}
//        bluetoothSocket = null
//
//        if (_bluetoothState.value is BluetoothConnState.Connecting ||
//            _bluetoothState.value is BluetoothConnState.Connected) {
//            _bluetoothState.value = BluetoothConnState.Disconnected
//        }
//    }
//
//}