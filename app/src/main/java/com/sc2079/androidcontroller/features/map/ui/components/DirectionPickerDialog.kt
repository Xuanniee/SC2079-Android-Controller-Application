package com.sc2079.androidcontroller.features.map.ui.components

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
        containerColor = MaterialTheme.colorScheme.surface,
        title = { 
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface
            ) 
        },
        text = {
            Column {
                FaceDir.entries.forEach { dir ->
                    Row {
                        RadioButton(
                            selected = selected == dir,
                            onClick = { selected = dir },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            dir.name,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selected) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) { 
                Text("Save") 
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) { 
                Text("Cancel") 
            }
        }
    )
}