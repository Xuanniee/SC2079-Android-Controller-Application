package com.sc2079.androidcontroller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.R
import com.sc2079.androidcontroller.features.map.presentation.components.CellState
import com.sc2079.androidcontroller.features.map.presentation.components.CellType
import com.sc2079.androidcontroller.features.map.presentation.components.GridPosition
import com.sc2079.androidcontroller.features.map.presentation.components.MapGrid
import com.sc2079.androidcontroller.ui.components.FadeInAnimation
import com.sc2079.androidcontroller.ui.theme.SC2079AndroidControllerApplicationTheme

/**
 * Direction enum for arrow buttons
 */
enum class Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

/**
 * Home screen - Main map view with controls
 * Adapts layout based on screen orientation:
 * - Portrait: Map on top, controls below
 * - Landscape: Map on left (2/3), controls on right (1/3)
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    var cellStates by remember { mutableStateOf<Map<GridPosition, CellState>>(emptyMap()) }
    var lastClickedPosition by remember { mutableStateOf<GridPosition?>(null) }
    
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        val isLandscape = maxWidth > maxHeight
        
        if (isLandscape) {
            // Landscape layout: Map left (2/3), Controls right (1/3)
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Map Card - 2/3 of screen width
                FadeInAnimation(
                    durationMillis = 1000,
                    modifier = Modifier.weight(2f)
                ) {
                    MapCard(
                        cellStates = cellStates,
                        onCellClick = { position ->
                            lastClickedPosition = position
                            val currentState = cellStates[position]
                            val newStates = cellStates.toMutableMap()
                            
                            newStates.keys.forEach { key ->
                                newStates[key]?.let { state ->
                                    newStates[key] = state.copy(isSelected = false)
                                }
                            }
                            
                            newStates[position] = CellState(
                                position = position,
                                isSelected = true,
                                cellType = currentState?.cellType ?: CellType.EMPTY
                            )
                            
                            cellStates = newStates
                        }
                    )
                }
                
                // Controls Section - 1/3 of screen width
                FadeInAnimation(
                    durationMillis = 1000,
                    delayMillis = 200,
                    modifier = Modifier.weight(1f)
                ) {
                    ControlsCard()
                }
            }
        } else {
            // Portrait layout: Map on top, Controls below
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Map Card - Takes most of the space
                FadeInAnimation(
                    durationMillis = 1000,
                    modifier = Modifier.weight(1f)
                ) {
                    MapCard(
                        cellStates = cellStates,
                        onCellClick = { position ->
                            lastClickedPosition = position
                            val currentState = cellStates[position]
                            val newStates = cellStates.toMutableMap()
                            
                            newStates.keys.forEach { key ->
                                newStates[key]?.let { state ->
                                    newStates[key] = state.copy(isSelected = false)
                                }
                            }
                            
                            newStates[position] = CellState(
                                position = position,
                                isSelected = true,
                                cellType = currentState?.cellType ?: CellType.EMPTY
                            )
                            
                            cellStates = newStates
                        }
                    )
                }
                
                // Controls Section
                FadeInAnimation(durationMillis = 1000, delayMillis = 200) {
                    ControlsCard()
                }
            }
        }
    }
}

/**
 * Map card component
 */
@Composable
private fun MapCard(
    cellStates: Map<GridPosition, CellState>,
    onCellClick: (GridPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        MapGrid(
            modifier = Modifier.fillMaxSize(),
            cellStates = cellStates,
            onCellClick = onCellClick
        )
    }
}

/**
 * Controls card component
 */
@Composable
private fun ControlsCard(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
    ) {
        Text(
                text = stringResource(R.string.controls),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Activity Status Card
                StatusCard(
                    title = stringResource(R.string.activity_status),
                    subtitle = stringResource(R.string.status_subhead),
                    modifier = Modifier.weight(1f, fill = false)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Direction Control Buttons
                DirectionControlButtons()
            }
        }
    }
}

/**
 * Direction control buttons (Up, Down, Left, Right)
 */
@Composable
private fun DirectionControlButtons() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DirectionButton(
            direction = Direction.UP,
            onClick = { /* Handle up */ }
        )
        DirectionButton(
            direction = Direction.DOWN,
            onClick = { /* Handle down */ }
        )
        DirectionButton(
            direction = Direction.LEFT,
            onClick = { /* Handle left */ }
        )
        DirectionButton(
            direction = Direction.RIGHT,
            onClick = { /* Handle right */ }
        )
    }
}

/**
 * Individual direction button component - Square with rounded borders
 */
@Composable
private fun DirectionButton(
    direction: Direction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val icon = when (direction) {
        Direction.UP -> Icons.Default.ArrowUpward
        Direction.DOWN -> Icons.Default.ArrowDownward
        Direction.LEFT -> Icons.AutoMirrored.Filled.ArrowBack
        Direction.RIGHT -> Icons.AutoMirrored.Filled.ArrowForward
    }
    
    val contentDescription = when (direction) {
        Direction.UP -> "Move Up"
        Direction.DOWN -> "Move Down"
        Direction.LEFT -> "Move Left"
        Direction.RIGHT -> "Move Right"
    }
    
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp) // Square button
            .clip(RoundedCornerShape(5.dp)), // Rounded borders
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp)
        )
    }
}

/**
 * Status card component
 */
@Composable
private fun StatusCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        // Status icons
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(8.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SC2079AndroidControllerApplicationTheme {
        HomeScreen()
    }
}
