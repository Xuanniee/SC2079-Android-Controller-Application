package com.sc2079.androidcontroller.features.bluetooth.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.sc2079.androidcontroller.features.bluetooth.domain.BluetoothConnState
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

class BluetoothClassicManager(
    private val appContext: Context,
    private val scope: CoroutineScope
) {
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        val manager = appContext.getSystemService(BluetoothManager::class.java)
        manager?.adapter
    }

    // Same UUID you used in Java
    private val sppUuid: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private val _connState = MutableStateFlow<BluetoothConnState>(BluetoothConnState.Disconnected)
    val connState: StateFlow<BluetoothConnState> = _connState.asStateFlow()

    private val _incomingBytes = MutableSharedFlow<ByteArray>(
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val incomingBytes = _incomingBytes.asSharedFlow()

    private var socket: BluetoothSocket? = null
    private var inStream: InputStream? = null
    private var outStream: OutputStream? = null
    private var readerJob: Job? = null

    fun hasConnectPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT) ==
                    PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun hasScanPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_SCAN) ==
                    PackageManager.PERMISSION_GRANTED
        } else true // pre-S used location in many cases; your old code requested location perms
    }

    fun isBluetoothEnabled(): Boolean = bluetoothAdapter?.isEnabled == true

    fun bondedDevices(): List<BluetoothDevice> {
        if (!hasConnectPermission()) return emptyList()
        val adapter = bluetoothAdapter ?: return emptyList()
        return try {
            adapter.bondedDevices?.toList().orEmpty()
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    /**
     * Refactor of BluetoothSetUp's discovery receiver (ACTION_FOUND).
     * Emits discovered devices as they are found.
     */
    @SuppressLint("MissingPermission")
    fun discoveryFlow(): Flow<BluetoothDevice> = callbackFlow {
        val adapter = bluetoothAdapter
        if (adapter == null) {
            close(IllegalStateException("Bluetooth not supported"))
            return@callbackFlow
        }
        if (!adapter.isEnabled) {
            close(IllegalStateException("Bluetooth disabled"))
            return@callbackFlow
        }
        if (!hasScanPermission()) {
            close(SecurityException("Missing BLUETOOTH_SCAN permission"))
            return@callbackFlow
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (BluetoothDevice.ACTION_FOUND == intent.action) {
                    val device: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    if (device != null) trySend(device).isSuccess
                }
            }
        }

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        appContext.registerReceiver(receiver, filter)

        // Equivalent to your "toggleButtonScan" startDiscovery
        adapter.cancelDiscovery()
        adapter.startDiscovery()

        awaitClose {
            try { appContext.unregisterReceiver(receiver) } catch (_: Exception) {}
            try { adapter.cancelDiscovery() } catch (_: Exception) {}
        }
    }

    /**
     * Refactor of startClientThread + ConnectThread + ConnectedThread.
     */
    @SuppressLint("MissingPermission")
    fun connect(device: BluetoothDevice) {
        disconnect()

        val adapter = bluetoothAdapter
        if (adapter == null) {
            _connState.value = BluetoothConnState.Error("Bluetooth not supported")
            return
        }
        if (!adapter.isEnabled) {
            _connState.value = BluetoothConnState.Error("Bluetooth is disabled")
            return
        }
        if (!hasConnectPermission()) {
            _connState.value = BluetoothConnState.Error("Missing BLUETOOTH_CONNECT permission")
            return
        }

        val name = device.name ?: device.address
        _connState.value = BluetoothConnState.Connecting(name)

        scope.launch(Dispatchers.IO) {
            try {
                adapter.cancelDiscovery()

                val sock = device.createRfcommSocketToServiceRecord(sppUuid)
                sock.connect()

                socket = sock
                inStream = sock.inputStream
                outStream = sock.outputStream

                _connState.value = BluetoothConnState.Connected(name, device)

                startReaderLoop()
            } catch (e: Exception) {
                _connState.value = BluetoothConnState.Error("Connect failed: ${e.message}")
                disconnect()
            }
        }
    }

    private fun startReaderLoop() {
        readerJob?.cancel()
        readerJob = scope.launch(Dispatchers.IO) {
            val input = inStream ?: return@launch
            val buffer = ByteArray(1024)

            while (isActive) {
                try {
                    val bytes = input.read(buffer)
                    if (bytes <= 0) break

                    // Java code noted “sometimes 1 char at a time”.
                    // We keep raw bytes and let VM decide how to assemble, but still emit what we have.
                    _incomingBytes.tryEmit(buffer.copyOfRange(0, bytes))
                } catch (e: IOException) {
                    break
                }
            }

            _connState.value = BluetoothConnState.Disconnected
            disconnect()
        }
    }

    /**
     * Refactor of ConnectedThread.write(bytes)
     */
    fun write(bytes: ByteArray) {
        scope.launch(Dispatchers.IO) {
            try {
                val output = outStream ?: throw IllegalStateException("Not connected")
                output.write(bytes)
                output.flush()
            } catch (e: Exception) {
                _connState.value = BluetoothConnState.Error("Write failed: ${e.message}")
                disconnect()
            }
        }
    }

    fun disconnect() {
        try { readerJob?.cancel() } catch (_: Exception) {}
        readerJob = null

        try { inStream?.close() } catch (_: Exception) {}
        inStream = null

        try { outStream?.close() } catch (_: Exception) {}
        outStream = null

        try { socket?.close() } catch (_: Exception) {}
        socket = null

        val cur = _connState.value
        if (cur is BluetoothConnState.Connected || cur is BluetoothConnState.Connecting) {
            _connState.value = BluetoothConnState.Disconnected
        }
    }
}
