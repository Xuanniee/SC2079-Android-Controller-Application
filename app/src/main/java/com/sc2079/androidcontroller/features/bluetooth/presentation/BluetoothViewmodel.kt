package com.sc2079.androidcontroller.features.bluetooth.presentation

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sc2079.androidcontroller.features.bluetooth.data.BluetoothClassicManager
import com.sc2079.androidcontroller.features.bluetooth.domain.BluetoothConnState
import com.sc2079.androidcontroller.features.bluetooth.domain.ChatMessage
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.nio.charset.Charset

data class BluetoothUiState(
    val connState: BluetoothConnState = BluetoothConnState.Disconnected,
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val discoveredDevices: List<BluetoothDevice> = emptyList(),
    val selectedDevice: BluetoothDevice? = null,
    val messages: List<ChatMessage> = emptyList(),
    val isScanning: Boolean = false
)

class BluetoothViewModel(
    private val manager: BluetoothClassicManager
) : ViewModel() {

    private val _paired = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    private val _discovered = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    private val _selected = MutableStateFlow<BluetoothDevice?>(null)
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    private val _isScanning = MutableStateFlow(false)

    private var scanJob: Job? = null

    val uiState: StateFlow<BluetoothUiState> =
        combine(
            listOf(
                manager.connState,
                _paired,
                _discovered,
                _selected,
                _messages,
                _isScanning
            )
        ) { arr ->
            val conn = arr[0] as BluetoothConnState
            val paired = arr[1] as List<BluetoothDevice>
            val discovered = arr[2] as List<BluetoothDevice>
            val selected = arr[3] as BluetoothDevice?
            val messages = arr[4] as List<ChatMessage>
            val scanning = arr[5] as Boolean

            BluetoothUiState(
                connState = conn,
                pairedDevices = paired,
                discoveredDevices = discovered,
                selectedDevice = selected,
                messages = messages,
                isScanning = scanning
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            BluetoothUiState()
        )


    init {
        // Refactor of BluetoothCommunications receiver: "incomingMessage"
        viewModelScope.launch {
            manager.incomingBytes.collect { bytes ->
                val text = bytes.toString(Charset.defaultCharset())
                // Keep it simple like the original: append as-is
                _messages.value = (_messages.value + ChatMessage(text = text, isIncoming = true))
                    .takeLast(400)
            }
        }
    }

    fun refreshPaired() {
        _paired.value = manager.bondedDevices()
    }

    fun startScan() {
        stopScan()
        _discovered.value = emptyList()

        scanJob = viewModelScope.launch {
            _isScanning.value = true
            manager.discoveryFlow()
                .catch {
                    // surface scan errors via connection state like your UI status text
                    // (you can also add a separate ui event flow)
                }
                .collect { device ->
                    // de-dupe by address
                    val cur = _discovered.value
                    if (cur.none { it.address == device.address }) {
                        _discovered.value = cur + device
                    }
                }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        scanJob = null
        _isScanning.value = false
    }

    fun selectDevice(device: BluetoothDevice) {
        _selected.value = device
    }

    fun connectSelected() {
        val device = _selected.value ?: return
        manager.connect(device)
    }

    fun disconnect() {
        manager.disconnect()
    }

    // Refactor of BluetoothCommunications "send" button + SharedPreferences history
    // (we keep history in-memory; you can persist with DataStore if you want)
    fun sendText(text: String) {
        if (text.isBlank()) return
        _messages.value = (_messages.value + ChatMessage(text = text, isIncoming = false)).takeLast(400)

        // Original Java used defaultCharset bytes
        manager.write(text.toByteArray(Charset.defaultCharset()))
    }
}
