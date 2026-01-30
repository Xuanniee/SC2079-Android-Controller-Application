package com.sc2079.androidcontroller.features.map.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.features.map.presentation.components.CellState
import com.sc2079.androidcontroller.features.map.presentation.components.CellType
import com.sc2079.androidcontroller.features.map.presentation.components.GridPosition
import com.sc2079.androidcontroller.features.map.presentation.components.MapGrid
import com.sc2079.androidcontroller.ui.theme.SC2079AndroidControllerApplicationTheme

/**
 * Map view screen with interactive grid
 */
@Composable
fun MapScreen(
    onNavigateBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Track cell states - you can manage this with a ViewModel in production
    var cellStates by remember { mutableStateOf<Map<GridPosition, CellState>>(emptyMap()) }
    var lastClickedPosition by remember { mutableStateOf<GridPosition?>(null) }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Arena Map (15 x 21)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = if (lastClickedPosition != null) {
                        "Selected: Row ${lastClickedPosition!!.row}, Column ${lastClickedPosition!!.column}"
                    } else {
                        "Tap on a cell to select it"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
        
        // Map Grid - takes available space
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            MapGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                cellStates = cellStates,
                onCellClick = { position ->
                    lastClickedPosition = position
                    
                    // Toggle selection on the clicked cell
                    val currentState = cellStates[position]
                    val newStates = cellStates.toMutableMap()
                    
                    // Clear previous selections (single selection mode)
                    newStates.keys.forEach { key ->
                        newStates[key]?.let { state ->
                            newStates[key] = state.copy(isSelected = false)
                        }
                    }
                    
                    // Set the new selection
                    newStates[position] = CellState(
                        position = position,
                        isSelected = true,
                        cellType = currentState?.cellType ?: CellType.EMPTY
                    )
                    
                    cellStates = newStates
                    
                    // Here you can fire your event/callback with the position
                    // For example: onCellSelected(position)
                }
            )
        }
        
        // Legend card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = "Legend",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "• Empty: Navigable space • Obstacle: Blocked • Robot: Position • Target: Destination",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    SC2079AndroidControllerApplicationTheme {
        MapScreen()
    }
}
