package com.sc2079.androidcontroller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.launch
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
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
import androidx.compose.foundation.layout.height
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.animation.Crossfade
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
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel
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
import com.sc2079.androidcontroller.ui.components.dialogs.LoadMapDialog
import com.sc2079.androidcontroller.ui.components.dialogs.ResetMapDialog
import com.sc2079.androidcontroller.ui.components.dialogs.SaveMapDialog
import com.sc2079.androidcontroller.features.bluetooth.domain.BluetoothConnState
import com.sc2079.androidcontroller.features.controller.domain.ControlState
import com.sc2079.androidcontroller.features.controller.domain.model.ActivityStatus
import com.sc2079.androidcontroller.features.controller.domain.model.RobotStatus
import com.sc2079.androidcontroller.features.controller.domain.usecase.MoveRobotUseCase
import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.presentation.RobotInboundEvent
import com.sc2079.androidcontroller.features.map.presentation.RobotMessageParser
import kotlinx.coroutines.delay
import java.nio.charset.Charset
import com.sc2079.androidcontroller.ui.components.home.ControlsCard
import com.sc2079.androidcontroller.ui.components.home.MapCard
import com.sc2079.androidcontroller.ui.components.map.MapActionsCard
import com.sc2079.androidcontroller.ui.theme.CustomSuccess
import com.sc2079.androidcontroller.features.map.presentation.RobotProtocol
import com.sc2079.androidcontroller.ui.theme.SC2079AndroidControllerApplicationTheme

/**
 * Home screen - Main map view with controls
 * Adapts layout based on screen orientation:
 * - Portrait: Map on top, controls below
 * - Landscape: Map on left (2/3), controls on right (1/3)
 */
@Composable
fun HomeScreen(
    bluetoothViewModel: BluetoothViewModel,
    mapViewModel: MapViewModel,
    modifier: Modifier = Modifier
) {
    // Collect the MapUiState as a Stateflow for UI changes
    val mapUiState by mapViewModel.uiState.collectAsState()
    
    // Get Bluetooth state
    val bluetoothUiState by bluetoothViewModel.bluetoothUiState.collectAsState()
    
    // Initialize RobotStatus state and MoveRobotUseCase
    val moveRobotUseCase = remember { MoveRobotUseCase() }
    var robotStatus by remember { 
        mutableStateOf<RobotStatus?>(
            // Initialize from map's robot position if available
            mapUiState.robotPosition?.let { 
                RobotStatus.fromPosition(it.x, it.y, it.faceDir)
            }
        )
    }
    
    // Get coroutine scope for async operations
    val robotScope = rememberCoroutineScope()
    
    // Track if robot is scanning (when "ROBOT" or "scanning" string is received)
    var isRobotScanning by remember { mutableStateOf(false) }
    
    // Listen to incoming Bluetooth messages and parse ROBOT messages
    LaunchedEffect(Unit) {
        bluetoothViewModel.incomingBtBytes.collect { bytes ->
            val message = String(bytes, Charset.defaultCharset())
            val messageUpper = message.uppercase().trim()
            
            // Check for "ROBOT,scanning" pattern first (case insensitive)
            // This is a special message indicating the robot is scanning
            // Matches: "ROBOT,scanning", "ROBOT, scanning", "robot,scanning", etc.
            if (messageUpper.matches(Regex("ROBOT\\s*,\\s*SCANNING"))) {
                // Set scanning status
                isRobotScanning = true
                // Reset scanning status after 2 seconds
                robotScope.launch {
                    delay(2000)
                    isRobotScanning = false
                }
                return@collect // Don't process further if it's a scanning message
            }
            
            // Check for "ROBOT,stopped" pattern (case insensitive)
            // This is a special message indicating the robot has stopped
            // Matches: "ROBOT,stopped", "ROBOT, stopped", "robot,stopped", etc.
            if (messageUpper.matches(Regex("ROBOT\\s*,\\s*STOPPED"))) {
                // Update robotStatus to indicate stopped
                robotStatus = robotStatus?.copy(
                    isMoving = false,
                    statusMessage = "Stopped"
                ) ?: RobotStatus(
                    x = 0,
                    y = 0,
                    faceDir = FaceDir.NORTH,
                    statusMessage = "Stopped",
                    isMoving = false
                )
                return@collect // Don't process further if it's a stopped message
            }
            
            // Parse messages to check for valid ROBOT position messages ("ROBOT, x, y, direction")
            val events = RobotMessageParser.parse(message)
            
            // Process ROBOT messages to update robot position and set moving status
            events.forEach { event ->
                if (event is RobotInboundEvent.RobotPoseEvent) {
                    // Update robotStatus with new position from ROBOT message
                    // This indicates the robot is moving (only when ROBOT message is received)
                    robotStatus = RobotStatus(
                        x = event.x,
                        y = event.y,
                        faceDir = event.dir,
                        statusMessage = "Moving",
                        isMoving = true
                    )
                    
                    // Reset isMoving to false after 2 seconds (robot stopped moving)
                    robotScope.launch {
                        delay(2000)
                        robotStatus = robotStatus?.copy(
                            isMoving = false,
                            statusMessage = "Stopped"
                        )
                    }
                }
            }
        }
    }
    
    // Update robotStatus when map's robot position changes (from external sources)
    // But only if it's not from a ROBOT message (to avoid overwriting)
    LaunchedEffect(mapUiState.robotPosition) {
        // Only update if robotStatus is null or not currently moving
        if (robotStatus == null || robotStatus?.isMoving != true) {
            mapUiState.robotPosition?.let { robotPos ->
                robotStatus = RobotStatus.fromPosition(robotPos.x, robotPos.y, robotPos.faceDir)
            }
        }
    }
    
    // Update map's robot position when robotStatus changes (from control buttons)
    LaunchedEffect(robotStatus) {
        robotStatus?.let { status ->
            // Update robot position on map
            if (mapUiState.editMode == MapEditMode.SetStart) {
                mapViewModel.onTapCell(status.x, status.y)
            } else {
                // If not in SetStart mode, we need to update the robot position directly
                // This will be handled by the map when robotStatus changes
            }
        }
    }
    
    // Handler functions for control buttons
    val handleMoveUp = {
        val current = robotStatus ?: RobotStatus.fromPosition(0, 0, FaceDir.NORTH)
        robotStatus = moveRobotUseCase.moveAbsolute(current, "up")
    }
    
    val handleMoveDown = {
        val current = robotStatus ?: RobotStatus.fromPosition(0, 0, FaceDir.NORTH)
        robotStatus = moveRobotUseCase.moveAbsolute(current, "down")
    }
    
    val handleMoveLeft = {
        val current = robotStatus ?: RobotStatus.fromPosition(0, 0, FaceDir.NORTH)
        robotStatus = moveRobotUseCase.moveAbsolute(current, "left")
    }
    
    val handleMoveRight = {
        val current = robotStatus ?: RobotStatus.fromPosition(0, 0, FaceDir.NORTH)
        robotStatus = moveRobotUseCase.moveAbsolute(current, "right")
    }
    
    // Compute ActivityStatus based on Bluetooth state and robot status
    val activityStatus = remember(bluetoothUiState.bluetoothConnState, bluetoothUiState.isScanning, robotStatus, isRobotScanning) {
        when {
            // Scanning takes priority - check if robot sent "ROBOT" or "scanning" message
            isRobotScanning -> ActivityStatus.SCANNING
            bluetoothUiState.isScanning -> ActivityStatus.SCANNING
            bluetoothUiState.bluetoothConnState is BluetoothConnState.Connecting -> ActivityStatus.SCANNING
            bluetoothUiState.bluetoothConnState is BluetoothConnState.Listening -> ActivityStatus.SCANNING
            // If connected, check robot movement status
            bluetoothUiState.bluetoothConnState is BluetoothConnState.Connected -> {
                when {
                    robotStatus?.isMoving == true -> ActivityStatus.MOVING
                    robotStatus != null -> ActivityStatus.STOPPED
                    else -> ActivityStatus.CONNECTED
                }
            }
            // Default to disconnected
            else -> ActivityStatus.DISCONNECTED
        }
    }
    
    // Convert MapUiState to CellState map, using robotStatus if available
    val cellStates = remember(mapUiState.obstacles, robotStatus, mapUiState.robotPosition) {
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
        // Add robot position - prefer robotStatus over mapUiState.robotPosition
        val robotPos = robotStatus ?: mapUiState.robotPosition?.let { 
            RobotStatus.fromPosition(it.x, it.y, it.faceDir)
        }
        robotPos?.let { robot ->
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
    val selectedMode = mapUiState.editMode

    /**
     * TODO Appears to hold logic for confirming placement, going to comment this out
     * as it is creating a lot of bugs
     */
    // var previousMode by remember { mutableStateOf(selectedMode) }
    // var latestSelectedPosition by remember { mutableStateOf<GridPosition?>(null) }
    
//    // When mode changes, reset unconfirmed placements
//    LaunchedEffect(selectedMode) {
//        if (previousMode != selectedMode && latestSelectedPosition != null) {
//            // Mode changed - check if there's an unconfirmed placement at the latest selected position
//            val position = latestSelectedPosition!!
//            val x = position.column
//            val y = position.row
//
//            // Check if there's an obstacle at this position (from PlaceObstacle mode)
//            if (previousMode == MapEditMode.PlaceObstacle) {
//                val obstacle = mapUiState.obstacles.firstOrNull { it.x == x && it.y == y }
//                obstacle?.let {
//                    mapViewModel.removeObstacleByNo(it.obstacleId)
//                }
//            }
//
//            // Check if there's a robot at this position (from SetStart mode)
//            if (previousMode == MapEditMode.SetStart) {
//                mapUiState.robotPosition?.let { robot ->
//                    if (robot.x == x && robot.y == y) {
//                        mapViewModel.clearRobotPosition()
//                    }
//                }
//            }
//
//            // Reset latest selected position
//            latestSelectedPosition = null
//        }
//        previousMode = selectedMode
//    }
    
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

    /**
     * TODO Commenting out all the chipSplashTrigger as causing bugs
     */
//    // State to trigger chip splash effect when a cell is tapped
//    var chipSplashTrigger by remember { mutableStateOf(0) }
//
    // Tab state for mobile screens (0 = Map Mode, 1 = Controller)
    var selectedTab by remember { mutableStateOf(0) }
    
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        val isLandscape = maxWidth > maxHeight
        // Detect mobile phone: use smallest width (sw600dp breakpoint)
        // Mobile: smallest dimension < 600dp, Tablet: smallest dimension >= 600dp
        val minDimension = minOf(maxWidth, maxHeight)
        val isMobile = minDimension < 600.dp
        
        if (isLandscape) {
            // Landscape layout: Reorder based on right-handed preference
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Reorder based on right-handed preference
                if (ControlState.isRightHanded) {
                    // Right-handed: Map on left (2/3), Controls on right (1/3)
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

//                                // TODO REmove for now Track latest selected position for mode change detection
//                                latestSelectedPosition = position

                                mapViewModel.onTapCell(x, y)

//                                // Trigger chip splash effect
//                                chipSplashTrigger++
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
                    if (isMobile) {
                        // Mobile: Show tabs above the card (no fade animation wrapper for tabs)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Top
                        ) {
                            // Custom tabs styled like theme toggle buttons - at the top
                            CustomTabRow(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Show only active tab content with fade animation
                            Box(modifier = Modifier.weight(1f)) {
                                Crossfade(
                                    targetState = selectedTab,
                                    animationSpec = tween(durationMillis = 300),
                                    label = "tabContent"
                                ) { tab ->
                                    when (tab) {
                                        0 -> {
                                            // Map Actions Card (Toolbar + Save/Reset)
                                            MapActionsCard(
                                                selectedMode = selectedMode,
                                                onModeSelected = { mapViewModel.setEditMode(it) },
                                                onReset = { 
                                                    // Show confirmation dialog
                                                    showResetDialog = true
                                                },
                                                onSave = { showSaveDialog = true },
                                                onLoad = { showLoadDialog = true },
//                                                chipSplashTrigger = chipSplashTrigger,
                                                hideButtonText = isLandscape && isMobile,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        1 -> {
                                            // Controls Card
                                            ControlsCard(
                                                activityStatus = activityStatus,
                                                onUpClick = handleMoveUp,
                                                onDownClick = handleMoveDown,
                                                onLeftClick = handleMoveLeft,
                                                onRightClick = handleMoveRight,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Tablet: Show both cards
                        FadeInAnimation(
                            durationMillis = 1000,
                            delayMillis = 200,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Map Actions Card (Toolbar + Save/Reset)
                                MapActionsCard(
                                    selectedMode = selectedMode,
                                    onModeSelected = { mapViewModel.setEditMode(it) },
                                    onReset = { 
                                        // Show confirmation dialog
                                        showResetDialog = true
                                    },
                                    onSave = { showSaveDialog = true },
                                    onLoad = { showLoadDialog = true },
//                                    chipSplashTrigger = chipSplashTrigger,
                                    hideButtonText = isLandscape && isMobile
                                )
                                
                                // Controls Card
                                ControlsCard(
                                    activityStatus = activityStatus,
                                    onUpClick = handleMoveUp,
                                    onDownClick = handleMoveDown,
                                    onLeftClick = handleMoveLeft,
                                    onRightClick = handleMoveRight
                                )
                            }
                        }
                    }
                } else {
                    // Left-handed: Controls on left (1/3), Map on right (2/3)
                    // Controls Section - 1/3 of screen width
                    if (isMobile) {
                        // Mobile: Show tabs above the card (no fade animation wrapper for tabs)
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            verticalArrangement = Arrangement.Top
                        ) {
                            // Custom tabs styled like theme toggle buttons - at the top
                            CustomTabRow(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Show only active tab content with fade animation
                            Box(modifier = Modifier.weight(1f)) {
                                Crossfade(
                                    targetState = selectedTab,
                                    animationSpec = tween(durationMillis = 300),
                                    label = "tabContent"
                                ) { tab ->
                                    when (tab) {
                                        0 -> {
                                            // Map Actions Card (Toolbar + Save/Reset)
                                            MapActionsCard(
                                                selectedMode = selectedMode,
                                                onModeSelected = { mapViewModel.setEditMode(it) },
                                                onReset = { 
                                                    // Show confirmation dialog
                                                    showResetDialog = true
                                                },
                                                onSave = { showSaveDialog = true },
                                                onLoad = { showLoadDialog = true },
//                                                chipSplashTrigger = chipSplashTrigger,
                                                hideButtonText = isLandscape && isMobile,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                        1 -> {
                                            // Controls Card
                                            ControlsCard(
                                                activityStatus = activityStatus,
                                                onUpClick = handleMoveUp,
                                                onDownClick = handleMoveDown,
                                                onLeftClick = handleMoveLeft,
                                                onRightClick = handleMoveRight,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Tablet: Show both cards
                        FadeInAnimation(
                            durationMillis = 1000,
                            delayMillis = 200,
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Map Actions Card (Toolbar + Save/Reset)
                                MapActionsCard(
                                    selectedMode = selectedMode,
                                    onModeSelected = { mapViewModel.setEditMode(it) },
                                    onReset = { 
                                        // Show confirmation dialog
                                        showResetDialog = true
                                    },
                                    onSave = { showSaveDialog = true },
                                    onLoad = { showLoadDialog = true },
//                                    chipSplashTrigger = chipSplashTrigger,
                                    hideButtonText = isLandscape && isMobile
                                )
                                
                                // Controls Card
                                ControlsCard(
                                    activityStatus = activityStatus,
                                    onUpClick = handleMoveUp,
                                    onDownClick = handleMoveDown,
                                    onLeftClick = handleMoveLeft,
                                    onRightClick = handleMoveRight
                                )
                            }
                        }
                    }
                    
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

//                                // Track latest selected position for mode change detection
//                                latestSelectedPosition = position

                                mapViewModel.onTapCell(x, y)

//                                // Trigger chip splash effect
//                                chipSplashTrigger++
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

//                            // Track latest selected position for mode change detection
//                            latestSelectedPosition = position

                            mapViewModel.onTapCell(x, y)

//                            // Trigger chip splash effect
//                            chipSplashTrigger++
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
                
                // Controls Section - Side by side in portrait mode (tablet) or tabs (mobile)
                FadeInAnimation(durationMillis = 1000, delayMillis = 200) {
                    if (isMobile) {
                        // Mobile: Show tabs
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Custom tabs styled like theme toggle buttons
                            CustomTabRow(
                                selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it }
                            )
                            
                            // Show only active tab content
                            when (selectedTab) {
                                0 -> {
                                    // Map Actions Card (Toolbar + Save/Reset)
                                    MapActionsCard(
                                        selectedMode = selectedMode,
                                        onModeSelected = { mapViewModel.setEditMode(it) },
                                        onReset = { 
                                            // Show confirmation dialog
                                            showResetDialog = true
                                        },
                                        onSave = { showSaveDialog = true },
                                        onLoad = { showLoadDialog = true },
//                                        chipSplashTrigger = chipSplashTrigger,
                                        hideButtonText = isLandscape && isMobile,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                1 -> {
                                    // Controls Card
                                    ControlsCard(
                                        activityStatus = activityStatus,
                                        onUpClick = handleMoveUp,
                                        onDownClick = handleMoveDown,
                                        onLeftClick = handleMoveLeft,
                                        onRightClick = handleMoveRight,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    } else {
                        // Tablet: Show both cards side by side
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Reorder based on right-handed preference
                            // Access ControlState directly to ensure recomposition when it changes
                            if (ControlState.isRightHanded) {
                                // Right-handed: Map Actions Card on left, Controls Card on right
                                MapActionsCard(
                                    selectedMode = selectedMode,
                                    onModeSelected = { mapViewModel.setEditMode(it) },
                                    onReset = { 
                                        // Show confirmation dialog
                                        showResetDialog = true
                                    },
                                    onSave = { showSaveDialog = true },
                                    onLoad = { showLoadDialog = true },
//                                    chipSplashTrigger = chipSplashTrigger,
                                    hideButtonText = isLandscape && isMobile,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Controls Card - 1/2 width
                                ControlsCard(
                                    activityStatus = activityStatus,
                                    onUpClick = handleMoveUp,
                                    onDownClick = handleMoveDown,
                                    onLeftClick = handleMoveLeft,
                                    onRightClick = handleMoveRight,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                // Left-handed: Controls Card on left, Map Actions Card on right
                                ControlsCard(
                                    activityStatus = activityStatus,
                                    onUpClick = handleMoveUp,
                                    onDownClick = handleMoveDown,
                                    onLeftClick = handleMoveLeft,
                                    onRightClick = handleMoveRight,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Map Actions Card (Toolbar + Save/Reset) - 1/2 width
                                MapActionsCard(
                                    selectedMode = selectedMode,
                                    onModeSelected = { mapViewModel.setEditMode(it) },
                                    onReset = { 
                                        // Show confirmation dialog
                                        showResetDialog = true
                                    },
                                    onSave = { showSaveDialog = true },
                                    onLoad = { showLoadDialog = true },
//                                    chipSplashTrigger = chipSplashTrigger,
                                    hideButtonText = isLandscape && isMobile,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
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
        SaveMapDialog(
            mapName = saveName,
            onMapNameChange = { saveName = it },
            onConfirm = {
                mapViewModel.saveCurrentMap(saveName)
                showSaveDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Map '$saveName' has been saved")
                }
            },
            onDismiss = { showSaveDialog = false }
        )
    }
    
    // Load Map Dialog
    if (showLoadDialog) {
        LoadMapDialog(
            savedMaps = mapUiState.savedMaps,
            selectedMapName = selectedMapName,
            onMapSelected = { selectedMapName = it },
            onDeleteMap = { mapName ->
                mapViewModel.deleteMap(mapName)
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Map '$mapName' has been deleted")
                }
            },
            onConfirm = {
                selectedMapName?.let { name ->
                    mapViewModel.loadMap(name)
                    showLoadDialog = false
                    selectedMapName = null
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Map '$name' has been loaded")
                    }
                }
            },
            onDismiss = {
                showLoadDialog = false
                selectedMapName = null
            }
        )
    }
    
    // Reset Map Confirmation Dialog
    if (showResetDialog) {
        ResetMapDialog(
            onConfirm = {
                mapViewModel.resetAll()
                showResetDialog = false
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Map has been reset")
                }
            },
            onDismiss = { showResetDialog = false }
        )
    }
}

/**
 * Custom tab row styled like theme toggle buttons
 */
@Composable
private fun CustomTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        CustomTab(
            label = stringResource(R.string.map_mode_tab),
            isSelected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            modifier = Modifier.weight(1f)
        )
        
        CustomTab(
            label = stringResource(R.string.controller_tab),
            isSelected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Individual custom tab button styled like theme toggle button
 */
@Composable
private fun CustomTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}

// Preview commented out - requires BluetoothViewModel dependency
// @Preview(showBackground = true)
// @Composable
// fun HomeScreenPreview() {
//     SC2079AndroidControllerApplicationTheme {
//         // HomeScreen requires bluetoothViewModel parameter
//         // Preview would need a mock BluetoothClassicManager
//     }
// }
