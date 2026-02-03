package com.sc2079.androidcontroller.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.features.controller.domain.model.ActivityStatus

/**
 * Status card component that displays activity status
 */
@Composable
fun StatusCard(
    title: String,
    status: ActivityStatus,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = getStatusText(status),
                style = MaterialTheme.typography.bodySmall,
                color = getStatusColor(status)
            )
        }
        
        // Status indicator with icon
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(getStatusBackgroundColor(status))
                .padding(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getStatusIcon(status),
                    contentDescription = status.name,
                    modifier = Modifier.size(20.dp),
                    tint = getStatusIconColor(status)
                )
                // Status indicator dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(getStatusDotColor(status))
                )
            }
        }
    }
}

/**
 * Get the text for the status
 */
private fun getStatusText(status: ActivityStatus): String {
    return when (status) {
        ActivityStatus.CONNECTED -> "Connected"
        ActivityStatus.DISCONNECTED -> "Disconnected"
        ActivityStatus.MOVING -> "Moving"
        ActivityStatus.STOPPED -> "Stopped"
        ActivityStatus.SCANNING -> "Scanning"
    }
}

/**
 * Get the color for the status text
 */
@Composable
private fun getStatusColor(status: ActivityStatus): Color {
    return when (status) {
        ActivityStatus.CONNECTED -> Color(0xFF4CAF50) // Green
        ActivityStatus.DISCONNECTED -> Color(0xFFF44336) // Red
        ActivityStatus.MOVING -> Color(0xFF2196F3) // Blue
        ActivityStatus.STOPPED -> Color(0xFF9E9E9E) // Grey
        ActivityStatus.SCANNING -> Color(0xFFFF9800) // Orange
    }
}

/**
 * Get the background color for the status indicator
 */
@Composable
private fun getStatusBackgroundColor(status: ActivityStatus): Color {
    return when (status) {
        ActivityStatus.CONNECTED -> Color(0xFF4CAF50).copy(alpha = 0.2f)
        ActivityStatus.DISCONNECTED -> Color(0xFFF44336).copy(alpha = 0.2f)
        ActivityStatus.MOVING -> Color(0xFF2196F3).copy(alpha = 0.2f)
        ActivityStatus.STOPPED -> Color(0xFF9E9E9E).copy(alpha = 0.2f)
        ActivityStatus.SCANNING -> Color(0xFFFF9800).copy(alpha = 0.2f)
    }
}

/**
 * Get the icon for the status
 */
private fun getStatusIcon(status: ActivityStatus): androidx.compose.ui.graphics.vector.ImageVector {
    return when (status) {
        ActivityStatus.CONNECTED -> Icons.Default.BluetoothConnected
        ActivityStatus.DISCONNECTED -> Icons.Default.Bluetooth
        ActivityStatus.MOVING -> Icons.Default.PlayArrow
        ActivityStatus.STOPPED -> Icons.Default.Stop
        ActivityStatus.SCANNING -> Icons.Default.BluetoothSearching
    }
}

/**
 * Get the icon color for the status
 */
@Composable
private fun getStatusIconColor(status: ActivityStatus): Color {
    return getStatusColor(status)
}

/**
 * Get the dot color for the status indicator
 */
@Composable
private fun getStatusDotColor(status: ActivityStatus): Color {
    return getStatusColor(status)
}
