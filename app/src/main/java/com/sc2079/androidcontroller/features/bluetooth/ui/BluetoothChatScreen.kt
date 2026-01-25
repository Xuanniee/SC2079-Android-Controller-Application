package com.sc2079.androidcontroller.features.bluetooth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel

@Composable
fun BluetoothChatScreen(
    bluetoothViewModel: BluetoothViewModel,
    onBack: () -> Unit
) {
    val bluetoothUiState by bluetoothViewModel.bluetoothUiState.collectAsState()
    var input by remember { mutableStateOf("") }

    Column(
        Modifier.fillMaxSize().padding(16.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack
            ) {
                Text("Back")
            }

            Spacer(Modifier.weight(1f))

            OutlinedButton(
                onClick = { bluetoothViewModel.disconnect() }
            ) {
                Text("Disconnect")
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Chat",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(bluetoothUiState.messages) { _, msg ->
                val prefix = if (msg.fromRobot) "RX: " else "TX: "
                Text(prefix + msg.messageBody)
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                label = { Text("Message") }
            )
            Button(
                onClick = {
                    bluetoothViewModel.sendMessage(input)
                    input = ""
                }
            ) { Text("Send") }
        }
    }
}
