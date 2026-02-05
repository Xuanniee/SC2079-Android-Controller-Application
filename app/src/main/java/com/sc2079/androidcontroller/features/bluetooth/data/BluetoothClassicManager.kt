package com.sc2079.androidcontroller.features.bluetooth.data

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.content.*
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

/**
 * Main Business Logic for Communicating via Bluetooth
 *
 * 1. AppContext allows us to access sys resources like BT Adapter because we are
 * it shows we have permissions.
 *
 * 2. Provides us with a coroutine scope to run bg tasks
 */

class BluetoothClassicManager(
    private val controllerAppContext: Context,
    private val bluetoothScope: CoroutineScope
) {
    init {
        // Ensure not recreating a new one when rotate screens
        android.util.Log.w(
            "BT_LIFECYCLE",
            "BT_TEST: BluetoothClassicManager CREATED hash=${System.identityHashCode(this)}"
        )
    }

    // Vibration manager for haptic feedback
    private val vibrationManager = com.sc2079.androidcontroller.features.vibration.VibrationManager(controllerAppContext)
    /**
     * Variables, Stateflows for the Bluetooth Module
     */
    // Actual BT Hardware on Device. Returns null if BT not avail
    private val bluetoothAdapter: BluetoothAdapter? by lazy {
        controllerAppContext
            .getSystemService(android.bluetooth.BluetoothManager::class.java)
            ?.adapter
    }

    // Serial Port Profile ID to open the BT Channel through handshake
    private val sppUuid: UUID =
        UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    // StateFlow to track the Bluetooth Connection Status. Starts being disconnected
    private val _bluetoothConnState = MutableStateFlow<BluetoothConnState>(
        BluetoothConnState.Disconnected
    )
    val bluetoothConnState: StateFlow<BluetoothConnState> = _bluetoothConnState.asStateFlow()
    // List of Incoming Bytes from BT Conn since they only understand Bytes
    private val _incomingBtBytes = MutableSharedFlow<ByteArray>(
        // Provide a buffer to buffer some messages if sender rate > receiver rate
        extraBufferCapacity = 64,
        // Throw away oldest if buffer overflow
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    // Expose as a shared flow as there might be dup messages and we don't just want the latest like
    // in stateflows
    val incomingBtBytes: SharedFlow<ByteArray> = _incomingBtBytes.asSharedFlow()
    // Client BT Socket - Socket on Robot that we are connecting to, to send data
    private var bluetoothSocket: BluetoothSocket? = null
    // Server BT Socket - Wait for robot to discover app
    private var bluetoothServerSocket: BluetoothServerSocket? = null
    // Watches input stream for data from Robot
    private var inputStream: InputStream? = null
    // Writes to this stream at Robot for them to receive
    private var outputStream: OutputStream? = null
    // Coroutine Jobs to check inputStream for messagfes
    private var readerJob: Job? = null
    // Coroutine job as server to wait for robot to connect
    private var acceptBluetoothJob: Job? = null
    // Flag to track if disconnect is user-initiated
    private var isUserInitiatedDisconnect = false

    /**
     * Permissions
     *
     * Helpers to check if we have permissions for various BT functionalities
     */
    // Checks if we can communicate with a paired device
    fun hasConnectPermission(): Boolean =
        // Checks if >= Android 12, which needs explicit permission from user
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(
                controllerAppContext,
                Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // BT on in older OS just means have permissions
            true
        }

    // Checks if App can use the BT hardware to scan for nearby devices
    fun hasScanPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Modern Android - Just check if we got bt scan permission
            ContextCompat.checkSelfPermission(
                controllerAppContext,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Older Android required Location permission as well, ensure we have it
            ContextCompat.checkSelfPermission(
                controllerAppContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        }

    // Check if Bluetooth is enabled by user
    fun isBluetoothEnabled(): Boolean {
        if (bluetoothAdapter == null) {
            // Device doesn't have Bluetooth hardware at all
            return false
        }
        // Check if the BT switch is on
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * Bluetooth Helper Functions
     */
    // Function to discover and stream BT devices nearby
    // Cast Android Events into Kotlins Flows
    fun startDiscovery(): Flow<BluetoothDevice> = callbackFlow {
        // Ensure that BT Hardware exists, has permissions and is enabled
        val btAdapter = bluetoothAdapter
        if (btAdapter == null) {
            close(IllegalStateException("Bluetooth not supported"))
            return@callbackFlow
        }
        if (!btAdapter.isEnabled) {
            close(IllegalStateException("Bluetooth disabled"))
            return@callbackFlow
        }
        if (!hasScanPermission()) {
            close(SecurityException("Missing scan permission"))
            return@callbackFlow
        }

        // Create a BC Listener to sys messages like BT Device Found or Batt low
        val broadcastReceiver = object : BroadcastReceiver() {
            // Filter for BT Device Found Broadcasts only
            @SuppressLint("MissingPermission")
            override fun onReceive(context: Context, intent: Intent) {
                if (BluetoothDevice.ACTION_FOUND == intent.action) {
                    // Extract device metadata and update that we found a device if not null
                    val newDevice = getBluetoothDeviceMetadata(intent)
                    if (newDevice != null) {
                        // Emit to our stream
                        trySend(newDevice).isSuccess
                    }
                }
            }
        }
        // Register the Broadcast receiver so that it can start filtering messages
        controllerAppContext.registerReceiver(
            broadcastReceiver,
            IntentFilter(BluetoothDevice.ACTION_FOUND)
        )

        // Tell BT Adapter to perform only 1 scan
        try {
            btAdapter.cancelDiscovery()
            btAdapter.startDiscovery()
        } catch (se: SecurityException) {
            // Close the Job if we dont have permissions after trying to run
            close(se)
        }

        // Runs only if the listening flow stream is stopped
        awaitClose {
            try {
                // Unregister the Broadcast listener
                controllerAppContext.unregisterReceiver(broadcastReceiver)
            } catch (_: Exception) {}

            try {
                // Tell BT Addapter to stop scanning
                if (hasScanPermission()) {
                    btAdapter.cancelDiscovery()
                }
            } catch (_: SecurityException) {
                // Permission revoked while closing, just ignore since cant comm with BT adapter
            }
        }
    }

    /**
     * BT Server
     *
     * Converts the App into a BT Server that can receive connections from Robot
     */
    fun startBluetoothServer(serviceName: String = "SC2079_BT") {
        // Ensure only 1 BT Server, kill off any old connections
        disconnectBluetooth(userInitiated = true) // User-initiated when starting server
        stopBluetoothServer()

        // Ensure BT Hardware exists, enabled and has permissions
        val btAdapter = bluetoothAdapter ?: run {
            _bluetoothConnState.value = BluetoothConnState.Error("Bluetooth not supported")
            return
        }
        if (!btAdapter.isEnabled) {
            _bluetoothConnState.value = BluetoothConnState.Error("Bluetooth disabled")
            return
        }
        if (!hasConnectPermission()) {
            _bluetoothConnState.value = BluetoothConnState.Error("Missing BLUETOOTH_CONNECT permission")
            return
        }

        // Update BT Status to be Listening for Incoming Conns since no issue
        _bluetoothConnState.value = BluetoothConnState.Listening(serviceName)

        // Wait for BT Connections via a background thread and pass it to the job var from earlier
        acceptBluetoothJob = bluetoothScope.launch(Dispatchers.IO) {
            try {
                // Server Socker for BT Connections to listen and pass it to the global var for cleanup
                val btServerSocket = btAdapter.listenUsingInsecureRfcommWithServiceRecord(serviceName, sppUuid)
                bluetoothServerSocket = btServerSocket

                // Wait indefinitely here until we receive actual connection and stop others from connecting
                val sockConn = btServerSocket.accept()
                stopBluetoothServer()

                // Update all the details about the socket connected to our global vars
                bluetoothSocket = sockConn
                inputStream = sockConn.inputStream
                outputStream = sockConn.outputStream

                // Identify the device that connected and stauts
                val connectedDevice = sockConn.remoteDevice
                val connectedDeviceName = getSafeDeviceName(connectedDevice)
                _bluetoothConnState.value = BluetoothConnState.Connected(connectedDeviceName, connectedDevice)

                // Start listening for incoming messages
                startReaderLoop()
            } catch (e: SecurityException) {
                // BT Permission error
                _bluetoothConnState.value = BluetoothConnState.Error("Permission revoked")
                disconnectBluetooth(userInitiated = false)
            } catch (e: Exception) {
                // Any Error
                _bluetoothConnState.value = BluetoothConnState.Error("Server failed: ${e.message}")
                disconnectBluetooth(userInitiated = false)
            }
        }
    }

    // Stops the App from continue acting as a BT Server
    fun stopBluetoothServer() {
        // Cancel any BT Jobs if they exists
        try {
            acceptBluetoothJob?.cancel()
        } catch (_: Exception) {}
        acceptBluetoothJob = null

        // Close any BT server sockets if open
        try {
            bluetoothServerSocket?.close()
        } catch (_: Exception) {}
        bluetoothServerSocket = null
    }

    /**
     * BT Client
     *
     * Converts the App into a BT Client that tries to send messages to the Robot
     */
    // Attempt to connect to a server via BT
    fun connectBluetooth(device: BluetoothDevice) {
        // Reset to Clean state
        disconnectBluetooth(userInitiated = true) // User-initiated when connecting to new device
        stopBluetoothServer()

        // Ensure BT Hardware exists, enabled and has permissions
        val btAdapter = bluetoothAdapter
        if (btAdapter == null) {
            _bluetoothConnState.value = BluetoothConnState.Error("Bluetooth not supported")
            return
        }
        if (!btAdapter.isEnabled) {
            _bluetoothConnState.value = BluetoothConnState.Error("Bluetooth disabled")
            return
        }
        if (!hasConnectPermission()) {
            _bluetoothConnState.value = BluetoothConnState.Error("Missing BLUETOOTH_CONNECT permission")
            return
        }

        // Update the BT UiState to be trying to connect to a device
        val connectingDeviceName = getSafeDeviceName(device)
        _bluetoothConnState.value = BluetoothConnState.Connecting(connectingDeviceName)

        // Use a background thread to start to connect to the BT Server
        bluetoothScope.launch(Dispatchers.IO) {
            try {
                // BT Handshake Process - Stops scanning
                btAdapter.cancelDiscovery()
                // Create a socket on the Server using the SPP ID and connect to it
                val sock = device.createRfcommSocketToServiceRecord(sppUuid)
                sock.connect()

                // Update the UI State with the Client Connection
                bluetoothSocket = sock
                inputStream = sock.inputStream
                outputStream = sock.outputStream

                // Update the UI State to be connected and start listening for messages
                _bluetoothConnState.value = BluetoothConnState.Connected(connectingDeviceName, device)
                // Start Listening
                startReaderLoop()
            } catch (e: SecurityException) {
                _bluetoothConnState.value = BluetoothConnState.Error("Permission revoked")
                disconnectBluetooth(userInitiated = false)
            } catch (e: Exception) {
                _bluetoothConnState.value = BluetoothConnState.Error("Connect failed: ${e.message}")
                disconnectBluetooth(userInitiated = false)
            }
        }
    }

    // Attempt to disconnect to a server via BT
    // @param userInitiated Set to true if user manually disconnected, false if unexpected disconnection
    fun disconnectBluetooth(userInitiated: Boolean = false) {
        android.util.Log.w(
            "BT_LIFECYCLE",
            "BT_TEST: disconnectBluetooth(userInitiated=$userInitiated) hash=${
                System.identityHashCode(
                    this
                )
            }"
        )
        // Store whether this is user-initiated before disconnecting
        val wasConnected = _bluetoothConnState.value is BluetoothConnState.Connected
        isUserInitiatedDisconnect = userInitiated

        // Stop listening for any server if we are
        stopBluetoothServer()

        // Kill any BT Job that are open and reset the UI State for all conn uistate vars
        try {
            readerJob?.cancel()
        } catch (_: Exception) {}
        readerJob = null

        try {
            inputStream?.close()
        } catch (_: Exception) {}
        inputStream = null

        try {
            outputStream?.close()
        } catch (_: Exception) {}
        outputStream = null

        try {
            bluetoothSocket?.close()
        } catch (_: Exception) {}
        bluetoothSocket = null

        // Update the UiState to be disconnceted
        val previousState = _bluetoothConnState.value
        _bluetoothConnState.value = BluetoothConnState.Disconnected

        // Trigger vibration if this was an unexpected disconnection (was connected but not user-initiated)
        if (wasConnected && !userInitiated) {
            onUnexpectedDisconnection()
        }
    }

    /**
     * Callback for unexpected disconnection - can be overridden or used with a listener
     */
    private var unexpectedDisconnectionCallback: (() -> Unit)? = null

    /**
     * Set callback for unexpected disconnection events
     */
    fun setUnexpectedDisconnectionCallback(callback: (() -> Unit)?) {
        unexpectedDisconnectionCallback = callback
    }

    /**
     * Handle unexpected disconnection
     */
    private fun onUnexpectedDisconnection() {
        // Trigger vibration for unexpected disconnection
        vibrationManager.vibrateForDisconnection()
        // Call any registered callback
        unexpectedDisconnectionCallback?.invoke()
    }

    /**
     * Generic BT Helper Functions
     */
    // Starts listening for messages on BT Conn
    private fun startReaderLoop() {
        android.util.Log.w(
            "BT_LIFECYCLE",
            "BT_TEST: startReaderLoop hash=${System.identityHashCode(this)}"
        )
        // Cancel any old existing BT Jobs
        readerJob?.cancel()

        // Start a new listening BT Job witb coroutine
        readerJob = bluetoothScope.launch(Dispatchers.IO) {
            // Retrieve a reference to the inputStream from Robot
            val btInputStream = inputStream ?: return@launch
            // Create a buffer
            val buffer = ByteArray(1024)

            // Run until connection dies
            while (isActive) {
                try {
                    // Read the number of bytes from stream
                    val numBytes = btInputStream.read(buffer)
                    if (numBytes <= 0) {
                        // Robot has broken conn from their end
                        break
                    }
                    // Emit to the SharedFlow the number of bytes we got
                    _incomingBtBytes.tryEmit(buffer.copyOfRange(0, numBytes))
                } catch (_: IOException) {
                    break
                }
            }

            // Disconnect the BT Connection (unexpected - not user-initiated)
            disconnectBluetooth(userInitiated = false)
        }
    }

    // Sends instructions/msgs to robot
    fun writeMessage(bytes: ByteArray) {
        android.util.Log.w(
            "BT_LIFECYCLE",
            "BT_TEST: writeMessage hash=\${System.identityHashCode(this)} outputStreamNull=\${outputStream == null}"
        )
        bluetoothScope.launch(Dispatchers.IO) {
            try {
                // Retrieve a reference to the outputstream
                val btOutputStream = outputStream ?: return@launch

                // Push data and send it over BT conn
                btOutputStream.write(bytes)
                btOutputStream.flush()
            } catch (_: Exception) {
                // Unexpected disconnection during write
                disconnectBluetooth(userInitiated = false)
            }
        }
    }

    // Function to retrieve device name if it exists safel
    private fun getSafeDeviceName(device: BluetoothDevice?): String {
        if (device == null) {
            return "Unknown"
        }

        return try {
            // Retrieve nickname if it exists else just return MAC Address
            device.name ?: device.address
        } catch (_: SecurityException) {
            device.address
        }
    }

    // Retrieves details about the BT Device
    private fun getBluetoothDeviceMetadata(intent: Intent): BluetoothDevice? {
        // Retrieve the BT device object if we use EXTRA_DEVICE key to getr basic metadata info
        return if (Build.VERSION.SDK_INT >= 33) {
            // If >= Android 13
            intent.getParcelableExtra(
                BluetoothDevice.EXTRA_DEVICE,
                BluetoothDevice::class.java
            )
        } else {
            // Older
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
        }
    }

    // Returns the list of devices we have paired previously if we have permission
    fun retrievePairedDevices(): List<BluetoothDevice> {
        // Checks if we can retrieve the list of paired devices
        val adapter = bluetoothAdapter ?: return emptyList()
        if (!hasConnectPermission()) {
            return emptyList()
        }

        return try {
            adapter.bondedDevices?.toList().orEmpty()
        } catch (_: SecurityException) {
            emptyList()
        }
    }
}
