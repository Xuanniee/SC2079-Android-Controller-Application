package com.example.sc2079androidcontroller.features.bluetooth.network

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import com.example.sc2079androidcontroller.gui.hasBluetoothConnectPermission
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.util.UUID

class BluetoothService (

) {
    /**
     * Track the various possible Bluetooth Connection States
     */
    sealed class BluetoothConnState {
        // Disconnected uses a singleton as there is only one such state + Does not carry extra info
        data object Disconnected: BluetoothConnState()
        // Holds data as we can be connected to multiple devices
        data class Connecting(val deviceName: String): BluetoothConnState()
        data class Connected(val deviceName: String): BluetoothConnState()
        data class Error(val errorMessage: String): BluetoothConnState()
    }

    // Variables & StateFlows to track various states
    private val _bluetoothState = MutableStateFlow<BluetoothConnState>(
        BluetoothConnState.Disconnected
    )
    val bluetoothState = _bluetoothState.asStateFlow()

    // SharedFlow useful for streaming messages
    private val _incomingMessages = MutableSharedFlow<String>(
        extraBufferCapacity = 64
    )
    val incomingMessages = _incomingMessages.asSharedFlow()

    // Sockets & I/O Vars
    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothWriter: OutputStreamWriter? = null
    private var bluetoothReader: Job? = null

    // Standard Serial Port Profile (SPP) UUID for Bluetooth
    // TODO Check this first if fails to connect via BT
    private val sppUuid: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    // Retrieves a list of Android devices previously connected via BT
    fun getBondedDevices(context: Context): List<BluetoothDevice> {
        // Check if the User has provided permissions to access the devices' bonded list
        if (!hasBluetoothConnectPermission(context)) return emptyList()

        // Retrieve the local bluetooth adapter on the device
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java) ?: return emptyList()
        val localBluetoothAdapter = bluetoothManager.adapter ?: return emptyList()

        return try {
            localBluetoothAdapter.bondedDevices?.toList().orEmpty()
        } catch (se: SecurityException) {
            emptyList()
        }
    }

    // Attempt to connect to a particular BT device
    fun connect(device: BluetoothDevice) {
        // Ensure no old sockets are open
        disconnect()

        // Retrieve the local bluetooth adapter on the device
        val bluetoothManager = context.getSystemService(BluetoothManager::class.java) ?: return emptyList()
        val localBluetoothAdapter = bluetoothManager.adapter ?: return emptyList()

        if (adapter == null) {
            _bluetoothState.value = BluetoothConnState.Error("Bluetooth not supported on this device")
            return
        }
        if (!adapter.isEnabled) {
            _bluetoothState.value = BluetoothConnState.Error("Bluetooth is disabled. Please enable Bluetooth.")
            return
        }

        val name = device.name ?: device.address
        _bluetoothState.value = BluetoothConnState.Connecting(name)

        scope.launch(Dispatchers.IO) {
            try {
                // Cancel discovery because it slows down connection
                adapter.cancelDiscovery()

                val sock = device.createRfcommSocketToServiceRecord(sppUuid)
                sock.connect()

                socket = sock
                writer = OutputStreamWriter(sock.outputStream)

                _state.value = ConnState.Connected(name)

                startReaderLoop(sock)
            } catch (e: Exception) {
                _state.value = ConnState.Error("Connect failed: ${e.message}")
                disconnect()
            }
        }
    }

    /**
     * Rece
     */
    private fun startReaderLoop(sock: BluetoothSocket) {
        readerJob?.cancel()
        readerJob = scope.launch(Dispatchers.IO) {
            try {
                val br = BufferedReader(InputStreamReader(sock.inputStream))
                while (true) {
                    val line = br.readLine() ?: break // null means disconnected
                    _incomingLines.tryEmit(line)
                }
                _state.value = ConnState.Disconnected
            } catch (e: Exception) {
                _state.value = ConnState.Error("Disconnected: ${e.message}")
            } finally {
                disconnect()
            }
        }
    }

    fun sendLine(line: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val w = writer ?: throw IllegalStateException("Not connected")
                w.write(line)
                w.write("\n")
                w.flush()
            } catch (e: Exception) {
                _state.value = ConnState.Error("Send failed: ${e.message}")
                disconnect()
            }
        }
    }

    fun disconnect() {
        try { readerJob?.cancel() } catch (_: Exception) {}
        readerJob = null

        try { writer?.close() } catch (_: Exception) {}
        writer = null

        try { socket?.close() } catch (_: Exception) {}
        socket = null

        if (_state.value is ConnState.Connecting || _state.value is ConnState.Connected) {
            _state.value = ConnState.Disconnected
        }
    }

}