package com.sc2079.androidcontroller.features.map.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.features.bluetooth.domain.Message

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessageLogBottomDialog(
    visible: Boolean,
    items: List<Message>,
    onDismiss: () -> Unit,
    title: String = "Bluetooth Log",
    onClear: (() -> Unit)? = null
) {
    // Don't show the Log if User did not click
    if (!visible) {
        return
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        MessageLog(
            title = title,
            items = items,
            onClear = onClear
        )
    }
}

@Composable
fun MessageLog(
    title: String,
    items: List<Message>,
    onClear: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Auto-scroll to latest when new messages arrive
    LaunchedEffect(items.size) {
        if (items.isNotEmpty()) {
            listState.animateScrollToItem(items.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)

            if (onClear != null) {
                TextButton(onClick = onClear) { Text("Clear") }
            }
        }

        Spacer(Modifier.height(10.dp))

        if (items.isEmpty()) {
            Text(
                "No messages yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
            return
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 240.dp, max = 520.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(items) { idx, message ->
                // Determine the speaker of the message
                val speaker = if (message.fromRobot) {
                    "Robot"
                } else {
                    "You"
                }
                // Provide a color based on the Speaker
                val badgeColor = if (message.fromRobot)
                    MaterialTheme.colorScheme.secondaryContainer
                else
                    MaterialTheme.colorScheme.primaryContainer

                // Create a Chat Log
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Row {
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = badgeColor
                            ) {
                                Text(
                                    speaker,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "#${idx + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }

                        Spacer(Modifier.height(6.dp))

                        // full message (no truncation)
                        Text(
                            message.messageBody,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}
