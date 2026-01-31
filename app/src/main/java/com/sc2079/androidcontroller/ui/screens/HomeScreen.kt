package com.sc2079.androidcontroller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.sc2079.androidcontroller.R
import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import com.sc2079.androidcontroller.features.map.presentation.components.CellState
import com.sc2079.androidcontroller.features.map.presentation.components.CellType
import com.sc2079.androidcontroller.features.map.presentation.components.GridPosition
import com.sc2079.androidcontroller.features.map.presentation.components.MapGrid
import com.sc2079.androidcontroller.ui.components.FadeInAnimation
import com.sc2079.androidcontroller.ui.theme.CustomSuccess
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
    
    // Map mode dropdown state
    var expanded by remember { mutableStateOf(false) }
    var selectedMode by remember { mutableStateOf(MapEditMode.Cursor) }
    
    // Save map dialog state
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveName by remember { mutableStateOf("map1") }
    
    // Reset map confirmation dialog state
    var showResetDialog by remember { mutableStateOf(false) }
    
    // Saved maps list (placeholder - can be connected to actual state later)
    var savedMaps by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Map Actions Card (Dropdown + Save/Reset)
                        MapActionsCard(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            selectedMode = selectedMode,
                            onModeSelected = { selectedMode = it },
                            onReset = { 
                                // Show confirmation dialog
                                showResetDialog = true
                            },
                            onSave = { showSaveDialog = true },
                            savedMaps = savedMaps,
                            onLoadMap = { /* Handle load */ },
                            onDeleteMap = { /* Handle delete */ }
                        )
                        
                        // Controls Card
                        ControlsCard()
                    }
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
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Map Actions Card (Dropdown + Save/Reset)
                        MapActionsCard(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            selectedMode = selectedMode,
                            onModeSelected = { selectedMode = it },
                            onReset = { 
                                // Show confirmation dialog
                                showResetDialog = true
                            },
                            onSave = { showSaveDialog = true },
                            savedMaps = savedMaps,
                            onLoadMap = { /* Handle load */ },
                            onDeleteMap = { /* Handle delete */ }
                        )
                        
                        // Controls Card
                        ControlsCard()
                    }
                }
            }
        }
        
        // Snackbar Host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = CustomSuccess,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp) // Increased border radius by 4dp (from default 4dp to 8dp)
            )
        }
    }
    
    // Save Map Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { 
                Text(
                    "Save Map",
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = saveName,
                        onValueChange = { saveName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedLabelColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            focusedBorderColor = MaterialTheme.colorScheme.outline,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // Handle save - can be connected to actual save functionality
                    showSaveDialog = false
                    // Show snackbar for save
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Map has been saved")
                    }
                }) { 
                    Text(
                        "Save",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { 
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
    
    // Reset Map Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { 
                Text(
                    "Reset Map",
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = {
                Text(
                    "Are you sure you want to reset the map? This action cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    // Handle reset
                    showResetDialog = false
                    // Show snackbar for reset
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Map has been reset")
                    }
                }) { 
                    Text(
                        "Reset",
                        color = MaterialTheme.colorScheme.error
                    ) 
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { 
                    Text(
                        "Cancel",
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                }
            }
        )
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
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Activity Status Card
                StatusCard(
                    title = stringResource(R.string.activity_status),
                    subtitle = stringResource(R.string.status_subhead),
                    modifier = Modifier.weight(1f, fill = false).fillMaxWidth().height(200.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
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
    
    Box(
        modifier = modifier
            .size(60.dp) // Square button
            .clip(RoundedCornerShape(16.dp)) // Slightly rounded corners
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
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

/**
 * Map Actions Card - Contains dropdown and save/reset buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MapActionsCard(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    selectedMode: MapEditMode,
    onModeSelected: (MapEditMode) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    savedMaps: List<String>,
    onLoadMap: (String) -> Unit,
    onDeleteMap: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val modeItems = remember {
        listOf(
            ModeItem(MapEditMode.Cursor, "Cursor", Icons.Filled.Settings),
            ModeItem(MapEditMode.SetStart, "Set Start", Icons.Filled.Flag),
            ModeItem(MapEditMode.PlaceObstacle, "Place Obstacle", Icons.Filled.Edit),
            ModeItem(MapEditMode.DragObstacle, "Drag Obstacle", Icons.Filled.OpenWith),
            ModeItem(MapEditMode.ChangeObstacleFace, "Change Face", Icons.Filled.Gesture)
        )
    }
    val selectedModeItem = modeItems.first { it.mode == selectedMode }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Map Mode Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandedChange
            ) {
                // Determine background color based on selected mode
                val backgroundColor = when (selectedMode) {
                    MapEditMode.Cursor -> MaterialTheme.colorScheme.primaryContainer
                    MapEditMode.SetStart -> MaterialTheme.colorScheme.secondaryContainer
                    else -> Color.Transparent
                }
                
                val contentColor = when (selectedMode) {
                    MapEditMode.Cursor -> MaterialTheme.colorScheme.onPrimaryContainer
                    MapEditMode.SetStart -> MaterialTheme.colorScheme.onSecondaryContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .background(backgroundColor, RoundedCornerShape(4.dp)),
                    readOnly = true,
                    value = selectedModeItem.label,
                    onValueChange = {},
                    singleLine = true,
                    label = { Text("Map Mode") },
                    leadingIcon = { 
                        Icon(
                            selectedModeItem.icon, 
                            contentDescription = null,
                            tint = contentColor
                        ) 
                    },
                    trailingIcon = { 
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) 
                    },
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        focusedTextColor = contentColor,
                        unfocusedTextColor = contentColor,
                        focusedLabelColor = contentColor,
                        unfocusedLabelColor = contentColor.copy(alpha = 0.7f),
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f),
                        focusedContainerColor = backgroundColor,
                        unfocusedContainerColor = backgroundColor
                    )
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    modeItems.forEach { item ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    item.label,
                                    color = MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            leadingIcon = { 
                                Icon(
                                    item.icon, 
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface
                                ) 
                            },
                            onClick = {
                                onExpandedChange(false)
                                onModeSelected(item.mode)
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = MaterialTheme.colorScheme.onSurface,
                                leadingIconColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
            
            // Reset and Save Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier.weight(1f)
                ) { 
                    Text("Reset Map") 
                }

                OutlinedButton(
                    onClick = onSave,
                    modifier = Modifier.weight(1f)
                ) { 
                    Text("Save Map") 
                }
            }
            
            // Saved Maps List
            if (savedMaps.isNotEmpty()) {
                Text(
                    "Saved Maps", 
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                savedMaps.forEach { name ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            name, 
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = { onLoadMap(name) }) { 
                            Text("Load") 
                        }
                        TextButton(onClick = { onDeleteMap(name) }) { 
                            Text("Delete") 
                        }
                    }
                }
            }
        }
    }
}

/**
 * Data class for mode items
 */
private data class ModeItem(
    val mode: MapEditMode,
    val label: String,
    val icon: ImageVector
)

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    SC2079AndroidControllerApplicationTheme {
        HomeScreen()
    }
}
