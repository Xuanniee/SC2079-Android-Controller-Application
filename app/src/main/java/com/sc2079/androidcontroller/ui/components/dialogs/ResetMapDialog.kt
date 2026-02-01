package com.sc2079.androidcontroller.ui.components.dialogs

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.sc2079.androidcontroller.R

/**
 * Reset Map Confirmation Dialog
 */
@Composable
fun ResetMapDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = { 
            Text(
                stringResource(R.string.reset_map_title),
                color = MaterialTheme.colorScheme.onSurface
            ) 
        },
        text = {
            Text(
                stringResource(R.string.reset_map_message),
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { 
                Text(
                    stringResource(R.string.reset_map),
                    color = MaterialTheme.colorScheme.error
                ) 
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { 
                Text(
                    stringResource(R.string.back),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}
