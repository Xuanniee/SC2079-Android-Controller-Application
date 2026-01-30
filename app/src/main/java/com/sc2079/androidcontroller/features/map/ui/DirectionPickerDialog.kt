package com.sc2079.androidcontroller.features.map.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.*
import androidx.compose.runtime.*

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir

@Composable
fun DirectionPickerDialog(
    title: String,
    initial: FaceDir,
    onDismiss: () -> Unit,
    onConfirm: (FaceDir) -> Unit
) {
    var selected by remember { mutableStateOf(initial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                FaceDir.entries.forEach { dir ->
                    Row {
                        RadioButton(
                            selected = selected == dir,
                            onClick = { selected = dir }
                        )
                        Text(dir.name)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}