package com.sc2079.androidcontroller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Save
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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.sc2079.androidcontroller.R
import com.sc2079.androidcontroller.features.map.data.local.MapPreferencesDataSource
import com.sc2079.androidcontroller.features.map.data.repository.MapRepositoryImpl
import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import com.sc2079.androidcontroller.features.map.presentation.MapViewModel
import com.sc2079.androidcontroller.features.map.presentation.MapViewModelFactory
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
    // Initialize MapViewModel
    val context = LocalContext.current
    val mapRepository = remember { MapRepositoryImpl(MapPreferencesDataSource(context.applicationContext)) }
    val mapViewModel: MapViewModel = viewModel(factory = MapViewModelFactory(mapRepository))
    val mapUiState by mapViewModel.uiState.collectAsState()
    
    // Convert MapUiState to CellState map
    val cellStates = remember(mapUiState.obstacles, mapUiState.robotPosition) {
        val states = mutableMapOf<GridPosition, CellState>()
        // Add obstacles
        mapUiState.obstacles.forEach { obstacle ->
            val position = GridPosition(obstacle.y, obstacle.x) // y=row, x=column
            states[position] = CellState(
                position = position,
                cellType = CellType.OBSTACLE,
                obstacleId = obstacle.obstacleId,
                obstacleFaceDir = obstacle.faceDir,
                displayedTargetId = obstacle.displayedTargetId
            )
        }
        // Add robot position
        mapUiState.robotPosition?.let { robot ->
            val position = GridPosition(robot.y, robot.x) // y=row, x=column
            states[position] = CellState(
                position = position,
                cellType = CellType.ROBOT,
                robotFaceDir = robot.faceDir
            )
        }
        states.toMap()
    }
    
    // Map mode dropdown state
    var expanded by remember { mutableStateOf(false) }
    val selectedMode = mapUiState.editMode
    
    // Save map dialog state
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveName by remember { mutableStateOf("map1") }
    
    // Load map dialog state
    var showLoadDialog by remember { mutableStateOf(false) }
    var selectedMapName by remember { mutableStateOf<String?>(null) }
    
    
    // Reset map confirmation dialog state
    var showResetDialog by remember { mutableStateOf(false) }
    
    // Snackbar state
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    
    // State to trigger chip splash effect when a cell is tapped
    var chipSplashTrigger by remember { mutableStateOf(0) }
    
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
                            // Convert GridPosition (row, column) to (x, y) for MapViewModel
                            // GridPosition(row, column) -> (x=column, y=row)
                            val x = position.column
                            val y = position.row
                            mapViewModel.onTapCell(x, y)
                            
                            // Trigger chip splash effect
                            chipSplashTrigger++
                        },
                        onCellRemove = { position ->
                            // Convert GridPosition (row, column) to (x, y) for MapViewModel
                            // GridPosition(row, column) -> (x=column, y=row)
                            val x = position.column
                            val y = position.row
                            
                            // Remove obstacle at this position if any
                            val obstacle = mapUiState.obstacles.firstOrNull { it.x == x && it.y == y }
                            obstacle?.let {
                                mapViewModel.removeObstacleByNo(it.obstacleId)
                            }
                            
                            // Remove robot if at this position
                            mapUiState.robotPosition?.let { robot ->
                                if (robot.x == x && robot.y == y) {
                                    mapViewModel.clearRobotPosition()
                                }
                            }
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
                            onModeSelected = { mapViewModel.setEditMode(it) },
                            onReset = { 
                                // Show confirmation dialog
                                showResetDialog = true
                            },
                            onSave = { showSaveDialog = true },
                            onLoad = { showLoadDialog = true },
                            chipSplashTrigger = chipSplashTrigger
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
                            // Convert GridPosition (row, column) to (x, y) for MapViewModel
                            // GridPosition(row, column) -> (x=column, y=row)
                            val x = position.column
                            val y = position.row
                            mapViewModel.onTapCell(x, y)
                            
                            // Trigger chip splash effect
                            chipSplashTrigger++
                        },
                        onCellRemove = { position ->
                            // Convert GridPosition (row, column) to (x, y) for MapViewModel
                            // GridPosition(row, column) -> (x=column, y=row)
                            val x = position.column
                            val y = position.row
                            
                            // Remove obstacle at this position if any
                            val obstacle = mapUiState.obstacles.firstOrNull { it.x == x && it.y == y }
                            obstacle?.let {
                                mapViewModel.removeObstacleByNo(it.obstacleId)
                            }
                            
                            // Remove robot if at this position
                            mapUiState.robotPosition?.let { robot ->
                                if (robot.x == x && robot.y == y) {
                                    mapViewModel.clearRobotPosition()
                                }
                            }
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
                            onModeSelected = { mapViewModel.setEditMode(it) },
                            onReset = { 
                                // Show confirmation dialog
                                showResetDialog = true
                            },
                            onSave = { showSaveDialog = true },
                            onLoad = { showLoadDialog = true },
                            chipSplashTrigger = chipSplashTrigger
                        )
                        
                        // Controls Card
                        ControlsCard()
                    }
                }
            }
        }
        
        // Snackbar Host (bottom) - for success messages
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
                    TextField(
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
                Button(
                    onClick = {
                        mapViewModel.saveCurrentMap(saveName)
                        showSaveDialog = false
                        // Show snackbar for save
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("Map '$saveName' has been saved")
                        }
                    },
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
                    onClick = { showSaveDialog = false },
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
    
    // Load Map Dialog
    if (showLoadDialog) {
        AlertDialog(
            onDismissRequest = { 
                showLoadDialog = false
                selectedMapName = null
            },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { 
                Text(
                    "Load Map",
                    color = MaterialTheme.colorScheme.onSurface
                ) 
            },
            text = {
                if (mapUiState.savedMaps.isEmpty()) {
                    Text(
                        "No saved maps available.",
                        color = MaterialTheme.colorScheme.onSurface
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        mapUiState.savedMaps.forEach { mapName ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedMapName = mapName },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    RadioButton(
                                        selected = selectedMapName == mapName,
                                        onClick = { selectedMapName = mapName },
                                        colors = RadioButtonDefaults.colors(
                                            selectedColor = MaterialTheme.colorScheme.primary
                                        )
                                    )
                                    Text(
                                        text = mapName,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Button(
                                    onClick = {
                                        mapViewModel.deleteMap(mapName)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Map '$mapName' has been deleted")
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "Delete",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedMapName?.let { name ->
                            mapViewModel.loadMap(name)
                            showLoadDialog = false
                            selectedMapName = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Map '$name' has been loaded")
                            }
                        }
                    },
                    enabled = selectedMapName != null,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        disabledContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.5f)
                    )
                ) { 
                    Text("Load")
                }
            },
            dismissButton = {
                Button(
                    onClick = { 
                        showLoadDialog = false
                        selectedMapName = null
                    },
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
                    mapViewModel.resetAll()
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
    onCellRemove: (GridPosition) -> Unit = {},
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
            onCellClick = onCellClick,
            onCellRemove = onCellRemove
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
    onLoad: () -> Unit,
    chipSplashTrigger: Int = 0,
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
            // Confirmation Chip - always shown with splash effect
            ChipWithSplash(
                chipSplashTrigger = chipSplashTrigger,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Map Mode Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = onExpandedChange
            ) {
                // Use secondary background color
                val backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                val contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                
                TextField(
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                        .background(backgroundColor, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
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
                    modifier = Modifier.background(
                        backgroundColor,
                        RoundedCornerShape(16.dp)
                    ),
                    onDismissRequest = { onExpandedChange(false) }
                ) {
                    modeItems.forEach { item ->
                        DropdownMenuItem(
                            text = { 
                                Text(
                                    item.label,
                                    color = contentColor
                                ) 
                            },
                            leadingIcon = { 
                                Icon(
                                    item.icon, 
                                    contentDescription = null,
                                    tint = contentColor
                                ) 
                            },
                            onClick = {
                                onExpandedChange(false)
                                onModeSelected(item.mode)
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = contentColor,
                                leadingIconColor = contentColor
                            )
                        )
                    }
                }
            }
            
            // Reset, Save, and Load Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reset Map Button - Red background
                MapActionButton(
                    onClick = onReset,
                    icon = Icons.Filled.Delete,
                    text = "Reset Map",
                    contentDescription = "Reset Map",
                    backgroundColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.weight(1f)
                )

                // Save Map Button - Tertiary background
                MapActionButton(
                    onClick = onSave,
                    icon = Icons.Filled.Save,
                    text = "Save Map",
                    contentDescription = "Save Map",
                    backgroundColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.weight(1f)
                )
                
                // Load Map Button - Tertiary background
                MapActionButton(
                    onClick = onLoad,
                    icon = Icons.Filled.Folder,
                    text = "Load Map",
                    contentDescription = "Load Map",
                    backgroundColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Chip with splash effect that triggers when a map box is tapped
 */
@Composable
private fun ChipWithSplash(
    chipSplashTrigger: Int,
    modifier: Modifier = Modifier
) {
    // Animation state for splash effect
    var isAnimating by remember(chipSplashTrigger) { mutableStateOf(false) }
    
    // Trigger animation when chipSplashTrigger changes
    LaunchedEffect(chipSplashTrigger) {
        if (chipSplashTrigger > 0) {
            isAnimating = true
            kotlinx.coroutines.delay(200) // Animation duration
            isAnimating = false
        }
    }
    
    // Scale animation for splash effect - scales up then back down
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 0.97f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "chip_splash"
    )
    
    // Alpha animation for additional visual feedback
    val alpha by animateFloatAsState(
        targetValue = if (isAnimating) 0.7f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "chip_alpha"
    )
    
    AssistChip(
        onClick = { /* Non-dismissible */ },
        label = {
            Text("Double tap to confirm selection, or it will not be confirmed")
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
            labelColor = MaterialTheme.colorScheme.error.copy(alpha = alpha)
        ),
        modifier = modifier
            .scale(scale)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = alpha),
                shape = RoundedCornerShape(8.dp)
            )
    )
}

/**
 * Map Action Button - Rectangular button with icon and text
 */
@Composable
private fun MapActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    contentDescription: String,
    backgroundColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp) // Increased height to accommodate icon and text vertically
            .clip(RoundedCornerShape(16.dp)) // Rounded corners
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor
            )
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
