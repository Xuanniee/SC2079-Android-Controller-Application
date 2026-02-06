package com.sc2079.androidcontroller.features.map.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.features.map.ui.util.Direction

@Composable
fun DirectionButton(
    direction: Direction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (direction) {
        Direction.NORTH -> Icons.Default.ArrowUpward
        Direction.SOUTH -> Icons.Default.ArrowDownward
        Direction.WEST -> Icons.AutoMirrored.Filled.ArrowBack
        Direction.EAST -> Icons.AutoMirrored.Filled.ArrowForward
    }

    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}