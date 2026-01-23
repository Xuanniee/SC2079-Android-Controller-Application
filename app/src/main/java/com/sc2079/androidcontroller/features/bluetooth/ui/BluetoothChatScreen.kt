package com.sc2079.androidcontroller.features.bluetooth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.features.bluetooth.domain.BluetoothConnState

@Composable
fun BluetoothChatScreen(
    vm: BluetoothViewModel,
    onBack: () -> Unit
) {
    val ui by vm.uiState.collectAsState()
    var input by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack) { Text("Back") }
            Text(
                text = when (val s = ui.connState) {
                    BluetoothConnState.Disconnected -> "Disconnected"
                    is BluetoothConnState.Connecting -> "Connecting..."
                    is BluetoothConnState.Connected -> "Connected: ${s.deviceName}"
                    is BluetoothConnState.Error -> "Error"
                }
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(ui.messages) { msg ->
                val prefix = if (msg.isIncoming) "RX: " else "TX: "
                Text(prefix + msg.text)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = input,
                onValueChange = { input = it },
                label = { Text("Type message") },
                singleLine = true
            )
            Button(
                onClick = {
                    vm.sendText(input)
                    input = ""
                },
                enabled = ui.connState is BluetoothConnState.Connected
            ) { Text("Send") }
        }
    }
}
