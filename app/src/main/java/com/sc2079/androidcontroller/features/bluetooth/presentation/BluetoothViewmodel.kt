package com.sc2079.androidcontroller.features.bluetooth.presentation

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sc2079.androidcontroller.features.bluetooth.data.BluetoothClassicManager
import com.sc2079.androidcontroller.features.bluetooth.domain.BluetoothConnState
import com.sc2079.androidcontroller.features.bluetooth.domain.BluetoothUiState
import com.sc2079.androidcontroller.features.bluetooth.domain.Message
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.nio.charset.Charset
import kotlinx.coroutines.flow.SharedFlow

/**
 * Viewmodel intermediate layer between the UI Layer (Screens) & Data Layer (BT Classic Mgr)
 */
class BluetoothViewModel(
    private val bluetoothManager: BluetoothClassicManager
) : ViewModel() {

    /**
     * Stateflows
     */
    // Stores the list of BT Devices already paired with the Application
    private val _pairedBtDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    // Stores new BT devices discovered during a BT scan
    private val _discoveredBtDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    // Tracks the BT Device selected by User (Haven't connect, just chosen)
    private val _selectedDevice = MutableStateFlow<BluetoothDevice?>(null)
    // Tracks the History of Command between App & Robot
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    // Variable to track if we are scanning for BT devices
    private val _isScanning = MutableStateFlow(false)
    // Tracks the latest error encountered during the Connection Process
    private val _lastError = MutableStateFlow<String?>(null)

    /**
     * Handle for tracking and managing the Bluetooth Scanning task in the Background
     *
     * Necessary so that we can terminate and prevent the App from unnecessary using resources
     * in the background.
     */
    private var bluetoothScanJob: Job? = null

    // --- helpers to avoid the "combine 7 params picks wrong overload" issue ---
    /**
     * Merge multiple stateflows into a singular stateflow stream so that UI can easily be updated
     * if one of the Stateflows changes.
     *
     * Combine() is ideal for merging 2/3 stateflows, so we create intermediate stateflows in
     * selectionUi and deviceListUi
     */
    // Holds the stateflows for variable that has to deal with user interaction
    private val selectionUi: Flow<Triple<BluetoothDevice?, Boolean, String?>> =
        combine(_selectedDevice, _isScanning, _lastError) { selectedDevice, isScanning, err ->
            Triple(selectedDevice, isScanning, err)
        }
    // Holds the stateflows for storage of devices
    private val deviceListUi: Flow<Triple<List<BluetoothDevice>, List<BluetoothDevice>, List<Message>>> =
        combine(_pairedBtDevices, _discoveredBtDevices, _messages) { pairedBtDevices, discoveredBtDevices, messages ->
            Triple(pairedBtDevices, discoveredBtDevices, messages)
        }

    /**
     * SSOT UI State value for Bluetooth so we ensure that any change in the stateflows will
     * recompute the UI.
     */
    val bluetoothUiState: StateFlow<BluetoothUiState> =
        // Combine acts as the listener for the 3 possible inputs: status from hardware, user
        // selection, and devices
        combine(bluetoothManager.bluetoothConnState, selectionUi, deviceListUi)
            { connection, (selectedDevice, isScanning, err), (pairedBtDevices, discoveredBtDevices, messages) ->
                // Reaches here if there is a change
                // Extract the name and MAC address of the new device detected
                val selectedDeviceName = selectedDevice?.safeName() ?: "-"
                val selectedDeviceAddr = selectedDevice?.address

                // Recreate the BT UiState based on new values as it is immutable
                BluetoothUiState(
                    bluetoothConnState = connection,
                    pairedBtDevices = pairedBtDevices,
                    discoveredBtDevices = discoveredBtDevices,
                    selectedDeviceName = selectedDeviceName,
                    selectedDeviceAddress = selectedDeviceAddr,
                    isScanning = isScanning,
                    messages = messages,
                    lastError = err
                )
            }.stateIn(
                // Converst the UiState into a Stateflow
                viewModelScope,
                // Wait 5s when rotating screen b4 killing BT conns
                SharingStarted.WhileSubscribed(5_000),
                BluetoothUiState()
            )

    /**
     * Expose incoming Bluetooth bytes for parsing ROBOT messages
     */
    val incomingBtBytes: SharedFlow<ByteArray> = bluetoothManager.incomingBtBytes

    /**
     * Entry Point when ViewModel is created to start background takss
     */
    init {
        android.util.Log.w(
            "BT_LIFECYCLE",
            "BluetoothViewModel CREATED — hash=${System.identityHashCode(this)}"
        )
        // Use a coroutine to listen for messages from the Robot via a background thread
        viewModelScope.launch {
            // Waits for incoming data and converts it to a new message to be appended
            bluetoothManager.incomingBtBytes.collect { bytes ->
                val newMessage = bytes.toString(Charset.defaultCharset())
                _messages.update { it + Message(
                    fromRobot = true,
                    messageBody = newMessage
                ) }
            }
        }
        // Another coroutine to listen for errors with BT conns
        viewModelScope.launch {
            // Collects only error states and updates the error message
            bluetoothManager.bluetoothConnState.collect { btConnState ->
                if (btConnState is BluetoothConnState.Error) {
                    _lastError.value = btConnState.msg
                }
            }
        }
    }

    /**
     * Extension and Helper Functions for Bluetooth Module
     */
    // Retreive device name if possible, else return MAC address
    private fun BluetoothDevice.safeName(): String {
        return try {
            this.name ?: this.address
        } catch (_: SecurityException) {
            this.address
        }
    }

    // Requests list of devices paired before from Android OS
    fun retrievePairedDevices() {
        _pairedBtDevices.value = bluetoothManager.retrievePairedDevices()
    }

    // Focus the UI element to indicate to user device is selected
    fun selectDevice(device: BluetoothDevice) {
        // Set the UI State value to whatever device was selected
        _selectedDevice.value = device
    }

    // Main Function to start scanning for nearby BT devices in the background
    fun startBluetoothScan() {
        // EC - Block users from performing more than 1 BT scan
        if (_isScanning.value) {
            return
        }

        // Reset list of devices for only nearby devices and stop old BT jobs
        _discoveredBtDevices.value = emptyList()
        _isScanning.value = true
        bluetoothScanJob?.cancel()

        // Start a coroutine to do the background scanning of BT devices
        bluetoothScanJob = viewModelScope.launch {
            // Call the biz logic to search for BT devices
            bluetoothManager.startDiscovery()
                .catch {
                    // Update user if we cannot start scan like BT is off
                    err -> _lastError.value = err.message ?: "Scan failed"
                }
                .onCompletion {
                    _isScanning.value = false
                }
                // Run this function as a listener for new device
                .collect {
                    // Every new BT device detected cannot be appended until we are sure that it is
                    // not inside our bt device list
                    newDevice -> _discoveredBtDevices.update { existingDeviceList ->
                        if (existingDeviceList.any { it.address == newDevice.address }) {
                            // Dont add to the old list
                            existingDeviceList
                        } else {
                            // Add to the BT device list
                            existingDeviceList + newDevice
                        }
                    }
                }
        }
    }

    // User to manually cancel BT Scan
    fun stopBluetoothScan() {
        // Cancel the job if it exists and update stateflow vars
        bluetoothScanJob?.cancel()
        bluetoothScanJob = null
        _isScanning.value = false
    }

    // Establish a BT conn with selected device
    fun connectSelectedDevice() {
        // Ensure that user has seleceted a device
        val deviceToConnect = _selectedDevice.value ?: run {
            _lastError.value = "No device selected"
            return
        }
        bluetoothManager.connectBluetooth(deviceToConnect)
    }

    // Have the application run as a BT server instead of client
    fun hostBluetoothServer() {
        bluetoothManager.startBluetoothServer(serviceName = "SC2079_BT")
    }

    // Disconnect the BT Conn (user-initiated)
    fun disconnect() {
        bluetoothManager.disconnectBluetooth(userInitiated = true)
    }

    // Send a Command to the Robot
    fun sendMessage(message: String) {
        // TODO Ensure a valid command is provided. For now, ensure not empty can already
        if (message.isBlank()) {
            return
        }

        // Update the Commands History with a new message provided
        _messages.update { it + Message(
            fromRobot = false,
            messageBody = message
        ) }
        // Write the command to the screen so we can see a message for now
        bluetoothManager.writeMessage(message.toByteArray(Charset.defaultCharset()))
    }

    // Override the onClear hook to release all BT sources if app is closed
    override fun onCleared() {
        // Check if it runs while rotating screen
        super.onCleared()
        android.util.Log.e(
            "BT_LIFECYCLE",
            "BluetoothViewModel.onCleared() CALLED — hash=${System.identityHashCode(this)}"
        )

        // Cancel Job before disconnect
        try {
            bluetoothScanJob?.cancel()
        } catch (_: Exception) {}
        bluetoothManager.disconnectBluetooth(userInitiated = true)
    }
}
