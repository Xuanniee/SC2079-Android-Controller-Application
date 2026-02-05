package com.sc2079.androidcontroller.features.map.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.features.bluetooth.domain.Message
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel
import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import com.sc2079.androidcontroller.features.map.presentation.MapViewModel
import com.sc2079.androidcontroller.features.map.presentation.RobotProtocol
import com.sc2079.androidcontroller.features.map.ui.components.DirectionPickerDialog
import com.sc2079.androidcontroller.features.map.ui.components.MessageLogBottomDialog

/**
 * Direction enum for arrow buttons
 */
enum class Direction { UP, DOWN, LEFT, RIGHT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingHomeScreen(
    bluetoothViewModel: BluetoothViewModel,
    mapViewModel: MapViewModel
) {
    val uiState by mapViewModel.uiState.collectAsState()

    // Process only new robot messages (so recomposition doesn't re-apply)
    val btUi by bluetoothViewModel.bluetoothUiState.collectAsState()
    var lastProcessedIdx by remember { mutableIntStateOf(0) }
    // BT Message Log
    var showLogSheet by remember { mutableStateOf(false) }
    val logItems = remember(btUi.messages) {
        btUi.messages
            .filter { RobotProtocol.validateProtocolMessage(it.messageBody) }
            .map { Message(fromRobot = it.fromRobot, messageBody = it.messageBody) }
    }


    LaunchedEffect(btUi.messages.size) {
        val msgs = btUi.messages
        for (i in lastProcessedIdx until msgs.size) {
            val m = msgs[i]
            if (m.fromRobot) mapViewModel.applyRobotMessage(m.messageBody)
        }
        lastProcessedIdx = msgs.size
    }

    // Ensure we wait until obstacles update, then we sync the UI to show the new obstacles
    var pendingSyncAfterLoad by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.obstacles, pendingSyncAfterLoad) {
        if (pendingSyncAfterLoad) {
            // TODO I think better to not send clear to robot first ig
            // bluetoothViewModel.sendMessage(RobotProtocol.clear())
            bluetoothViewModel.sendMessage(RobotProtocol.sendObstacleList(uiState.obstacles))
            pendingSyncAfterLoad = false
        }
    }


    // Face picker dialog state
    var facePickerForObstacle by remember { mutableStateOf<Int?>(null) }
    val obstacleNo = facePickerForObstacle

    // Status can display both the Robot Status and Last BT Msg
    val statusSubtitle = remember(btUi.messages.size, uiState.robotStatus) {
        buildString {
            if (uiState.robotStatus.isNotBlank()) {
                append("RobotStatus: ${uiState.robotStatus.take(64)} • ")
            }

            // Track the BT Messages
            val last = btUi.messages.lastOrNull()
            if (last == null) {
                append("No messages yet")
            }
            else if (last.fromRobot) {
                // Message from Robot
                append("Robot: ${last.messageBody.take(64)}")
            }
            else {
                // Message from User
                append("You: ${last.messageBody.take(64)}")
            }
        }
    }

    // --- Save dialog state (so Save actually works with names, not hardcoded "default") ---
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveName by remember { mutableStateOf("map1") }

    // --- Load dropdown state ---
    var loadExpanded by remember { mutableStateOf(false) }
    var selectedLoadName by remember(uiState.savedMaps) {
        mutableStateOf(uiState.savedMaps.firstOrNull() ?: "default")
    }

    // Keep selectedLoadName valid if the list changes
    LaunchedEffect(uiState.savedMaps) {
        if (uiState.savedMaps.isNotEmpty() && selectedLoadName !in uiState.savedMaps) {
            selectedLoadName = uiState.savedMaps.first()
        }
    }

    Scaffold(
        // topBar = { ... } // keep commented as you had it
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                // .background(MaterialTheme.colorScheme.background)
                .padding(12.dp)
        ) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // --- Tablet / Landscape: Left map, right panel (like your screenshot)
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // LEFT: Map surface
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(14.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f))
                                .padding(14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            MappingScreen(
                                uiState = uiState,
                                onSetMode = { mapViewModel.setEditMode(it) },
                                onReset = {
                                    // Reset the Map with the VM and send Messages to update
                                    mapViewModel.resetAll()
                                    bluetoothViewModel.sendMessage(RobotProtocol.clear())
                                    // TODO Need decide if we want to send an empty list when we reset the obstacle
                                    bluetoothViewModel.sendMessage(RobotProtocol.sendObstacleList(emptyList()))
                                },
                                onSendClear = { bluetoothViewModel.sendMessage(RobotProtocol.clear()) },
                                onSaveMap = { mapViewModel.saveCurrentMap(it) },
                                onLoadMap = { mapViewModel.loadMap(it) },
                                onDeleteMap = { mapViewModel.deleteMap(it) },
                                // Placement of a new obstacle
                                onTapCell = { x, y ->
                                    // Retrieve the Robot Old Position (if it exists)
                                    val oldRobotPosition = uiState.robotPosition
                                    // Retrieve the result of tapping cell
                                    val added = mapViewModel.onTapCell(x, y)

//                                    // TEST to ensure msg validation works
//                                    android.util.Log.d("SENDING MSG", "Should see that we came in")
//                                    bluetoothViewModel.sendMessage(
//                                        "TESTING SHOULDNT SEE"
//                                    )

                                    // Determine which mode of controller we are in
                                    when (uiState.editMode) {
                                        // Placing Robot Position
                                        MapEditMode.SetStart -> {
                                            // For updating the starting placement of robot, and not moving
                                            val newRobotPosition = mapViewModel.uiState.value.robotPosition
                                            if (newRobotPosition != null && newRobotPosition != oldRobotPosition) {
                                                // Send Message for Placing Robot
                                                bluetoothViewModel.sendMessage(
                                                    RobotProtocol.placeRobot(newRobotPosition)
                                                )
                                            }
                                        }

                                        // Place Obstacle
                                        MapEditMode.PlaceObstacle -> {
                                            // If no obstacle previously, we can send a message indicating we added it
                                            if (added != null) {
                                                bluetoothViewModel.sendMessage(RobotProtocol.upsertObstacle(added))
                                            }
                                        }

                                        // Other Modes dont need to send messages
                                        else -> Unit
                                    }
//                                    if (added != null) {
//                                        bluetoothViewModel.sendMessage(
//                                            RobotProtocol.upsertObstacle(added)
//                                        )
//                                    }
                                },
                                onTapObstacleForFace = { no ->
                                    if (uiState.editMode == MapEditMode.ChangeObstacleFace) {
                                        facePickerForObstacle = no
                                    }
                                },
                                onStartDragObstacle = { /* no-op */ },
                                onDragObstacleToCell = { no, x, y ->
                                    val moved = mapViewModel.onDragObstacle(no, x, y)
                                    if (moved != null) {
                                        bluetoothViewModel.sendMessage(
                                            RobotProtocol.upsertObstacle(
                                                moved
                                            )
                                        )
                                    }
                                },
                                onDragOutsideRemove = { no ->
                                    val removed = mapViewModel.removeObstacleByNo(no)
                                    if (removed != null) {
                                        bluetoothViewModel.sendMessage(
                                            RobotProtocol.removeObstacle(
                                                removed
                                            )
                                        )
                                    }
                                },
                                onEndDrag = { /* no-op */ }
                            )
                        }
                    }

                    // RIGHT: Panel
                    RightPanel(
                        modifier = Modifier
                            .widthIn(min = 320.dp, max = 420.dp)
                            .fillMaxHeight(),
                        editMode = uiState.editMode,
                        savedMaps = uiState.savedMaps,
                        selectedLoadName = selectedLoadName,
                        loadExpanded = loadExpanded,
                        onLoadExpandedChange = { loadExpanded = it },
                        onPickLoadName = { selectedLoadName = it },
                        onSetMode = { mapViewModel.setEditMode(it) },
                        onReset = {

                            mapViewModel.resetAll()
                            bluetoothViewModel.sendMessage(RobotProtocol.clear())
                            // TODO Need decide if we want to send an empty list when we reset the obstacle
                            bluetoothViewModel.sendMessage(RobotProtocol.sendObstacleList(emptyList()))
                        },
                        onSave = { showSaveDialog = true },
                        onLoad = {
                            // Return the name of the loaded map, default if it doesnt exist
                            val name = if (uiState.savedMaps.isNotEmpty()) selectedLoadName else "default"

                            // Ensure we send the obstacle lists only after loading
                            pendingSyncAfterLoad = true
                            // loadMap is an async function call so we must wait for uiState.obstacles to update first
                            mapViewModel.loadMap(name)
                        },
                        statusTitle = "Activity Status",
                        statusSubtitle = statusSubtitle,
                        // To sync obstacle list by sending it to RPI
                        onSync = {
                            // TODO Better to not Always clear first to ensure obstacle is latest
//                            bluetoothViewModel.sendMessage(
//                                RobotProtocol.clear()
//                            )
                            bluetoothViewModel.sendMessage(
                                RobotProtocol.sendObstacleList(uiState.obstacles)
                            )
                        },
                        // To Open Message Log
                        onOpenLog = { showLogSheet = true },
                        onDirection = { /* TODO wire manual move if you want */ }
                    )
                }
            } else {
                // --- Portrait fallback: stack map then panel
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        MappingScreen(
                            uiState = uiState,
                            onSetMode = { mapViewModel.setEditMode(it) },
                            onReset = {
                                mapViewModel.resetAll()
                                bluetoothViewModel.sendMessage(RobotProtocol.clear())
                                // TODO Need decide if we want to send an empty list when we reset the obstacle
                                bluetoothViewModel.sendMessage(RobotProtocol.sendObstacleList(emptyList()))
                            },
                            onSendClear = { bluetoothViewModel.sendMessage(RobotProtocol.clear()) },
                            onSaveMap = { mapViewModel.saveCurrentMap(it) },
                            onLoadMap = { mapViewModel.loadMap(it) },
                            onDeleteMap = { mapViewModel.deleteMap(it) },

                            onTapCell = { x, y ->
                                // Retrieve the Robot Old Position (if it exists)
                                val oldRobotPosition = uiState.robotPosition
                                // Retrieve the result of tapping cell
                                val added = mapViewModel.onTapCell(x, y)

                                // Determine which mode of controller we are in
                                when (uiState.editMode) {
                                    // Placing Robot Position
                                    MapEditMode.SetStart -> {
                                        // For updating the starting placement of robot, and not moving
                                        val newRobotPosition =
                                            mapViewModel.uiState.value.robotPosition
                                        if (newRobotPosition != null && newRobotPosition != oldRobotPosition) {
                                            // Send Message for Placing Robot
                                            bluetoothViewModel.sendMessage(
                                                RobotProtocol.placeRobot(newRobotPosition)
                                            )
                                        }
                                    }

                                    // Place Obstacle
                                    MapEditMode.PlaceObstacle -> {
                                        // If no obstacle previously, we can send a message indicating we added it
                                        if (added != null) {
                                            bluetoothViewModel.sendMessage(
                                                RobotProtocol.upsertObstacle(
                                                    added
                                                )
                                            )
                                        }
                                    }

                                    // Other Modes dont need to send messages
                                    else -> Unit
                                }
                            },
                            onTapObstacleForFace = { no ->
                                if (uiState.editMode == MapEditMode.ChangeObstacleFace) facePickerForObstacle = no
                            },
                            onStartDragObstacle = { },
                            onDragObstacleToCell = { no, x, y ->
                                val moved = mapViewModel.onDragObstacle(no, x, y)
                                if (moved != null) bluetoothViewModel.sendMessage(RobotProtocol.upsertObstacle(moved))
                            },
                            onDragOutsideRemove = { no ->
                                val removed = mapViewModel.removeObstacleByNo(no)
                                if (removed != null) bluetoothViewModel.sendMessage(RobotProtocol.removeObstacle(removed))
                            },
                            onEndDrag = { }
                        )
                    }

                    RightPanel(
                        modifier = Modifier.fillMaxWidth(),
                        editMode = uiState.editMode,
                        savedMaps = uiState.savedMaps,
                        selectedLoadName = selectedLoadName,
                        loadExpanded = loadExpanded,
                        onLoadExpandedChange = { loadExpanded = it },
                        onPickLoadName = { selectedLoadName = it },
                        onSetMode = { mapViewModel.setEditMode(it) },
                        onReset = {
                            mapViewModel.resetAll()
                            bluetoothViewModel.sendMessage(RobotProtocol.clear())
                            // TODO Need decide if we want to send an empty list when we reset the obstacle
                            bluetoothViewModel.sendMessage(RobotProtocol.sendObstacleList(emptyList()))
                        },
                        onSave = { showSaveDialog = true },
                        onLoad = {
                            // Return the name of the loaded map, default if it doesnt exist
                            val name = if (uiState.savedMaps.isNotEmpty()) selectedLoadName else "default"

                            // Ensure we send the obstacle lists only after loading
                            pendingSyncAfterLoad = true
                            mapViewModel.loadMap(name)
                        },
                        statusTitle = "Activity Status",
                        statusSubtitle = statusSubtitle,
                        // To sync obstacle list by sending it to RPI
                        onSync = {
                            // TODO Better to not Always clear first to ensure obstacle is latest
//                            bluetoothViewModel.sendMessage(
//                                RobotProtocol.clear()
//                            )
                            bluetoothViewModel.sendMessage(
                                RobotProtocol.sendObstacleList(uiState.obstacles)
                            )
                        },
                        // To open messages
                        onOpenLog = { showLogSheet = true },
                        onDirection = { }
                    )
                }
            }
        }
    }

    // Dialog to show messages
    MessageLogBottomDialog(
        visible = showLogSheet,
        items = logItems,
        onDismiss = { showLogSheet = false },
        // TODO for now we dont clear messages. Need decide if we want bluetoothViewModel::clearMessages
        onClear = null
    )

    // --- Save dialog ---
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Map") },
            text = {
                Column {
                    OutlinedTextField(
                        value = saveName,
                        onValueChange = { saveName = it },
                        label = { Text("Name") },
                        singleLine = true
                    )
                    if (uiState.savedMaps.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Existing: ${uiState.savedMaps.joinToString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    mapViewModel.saveCurrentMap(saveName)
                    showSaveDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Face picker dialog
    if (obstacleNo != null) {
        // If we have a valid Obstacle ID to change direction
        val current = uiState.obstacles.firstOrNull { it.obstacleId == obstacleNo }?.faceDir ?: FaceDir.NORTH

        DirectionPickerDialog(
            title = "Obstacle $obstacleNo Face",
            initial = current,
            onDismiss = { facePickerForObstacle = null },
            // Select a newFace
            onConfirm = { newFace ->
                // Default Face is Up if NULL
                val oldFace = uiState.obstacles.firstOrNull { it.obstacleId == obstacleNo }?.faceDir
                    ?: FaceDir.NORTH
                val updated = mapViewModel.setObstacleFace(obstacleNo, newFace)
                if (updated != null) {
                    bluetoothViewModel.sendMessage(
                        RobotProtocol.changeObstacleOrientation(updated, oldFace, newFace)
                    )
                }
                facePickerForObstacle = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RightPanel(
    editMode: MapEditMode,
    savedMaps: List<String>,
    selectedLoadName: String,
    loadExpanded: Boolean,
    onLoadExpandedChange: (Boolean) -> Unit,
    onPickLoadName: (String) -> Unit,
    onSetMode: (MapEditMode) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onLoad: () -> Unit,
    statusTitle: String,
    statusSubtitle: String,
    onOpenLog: () -> Unit,
    // To sync obstacle list with robot
    onSync: () -> Unit,
    onDirection: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Map Mode card (top)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(
                    text = "Map Mode: ${editModeLabel(editMode)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ModeIcon(
                        icon = Icons.Default.Settings,
                        selected = editMode == MapEditMode.Cursor,
                        onClick = { onSetMode(MapEditMode.Cursor) }
                    )
                    ModeIcon(
                        icon = Icons.Default.Flag,
                        selected = editMode == MapEditMode.SetStart,
                        onClick = { onSetMode(MapEditMode.SetStart) }
                    )
                    ModeIcon(
                        icon = Icons.Default.Edit,
                        selected = editMode == MapEditMode.PlaceObstacle,
                        onClick = { onSetMode(MapEditMode.PlaceObstacle) }
                    )
                    ModeIcon(
                        icon = Icons.Default.OpenWith,
                        selected = editMode == MapEditMode.DragObstacle,
                        onClick = { onSetMode(MapEditMode.DragObstacle) }
                    )
                    ModeIcon(
                        icon = Icons.Default.Gesture,
                        selected = editMode == MapEditMode.ChangeObstacleFace,
                        onClick = { onSetMode(MapEditMode.ChangeObstacleFace) }
                    )
                }

                Spacer(Modifier.height(14.dp))

                // Actions row 1: Reset + Save
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onReset,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Reset Map")
                    }

                    FilledTonalButton(
                        onClick = onSave,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Save Map")
                    }
                }

                Spacer(Modifier.height(10.dp))

                // Actions row 2: Load button + Sync Robot Messages
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
//                    ExposedDropdownMenuBox(
//                        expanded = loadExpanded,
//                        onExpandedChange = { onLoadExpandedChange(!loadExpanded) },
//                        modifier = Modifier.weight(1f)
//                    ) {
//                        OutlinedTextField(
//                            modifier = Modifier
//                                .menuAnchor()
//                                .fillMaxWidth(),
//                            readOnly = true,
//                            value = if (savedMaps.isNotEmpty()) selectedLoadName else "No saved maps",
//                            onValueChange = {},
//                            label = { Text("Load Map") },
//                            singleLine = true,
//                            trailingIcon = {
//                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = loadExpanded)
//                            },
//                            enabled = savedMaps.isNotEmpty()
//                        )
//
//                        ExposedDropdownMenu(
//                            expanded = loadExpanded && savedMaps.isNotEmpty(),
//                            onDismissRequest = { onLoadExpandedChange(false) }
//                        ) {
//                            savedMaps.forEach { name ->
//                                DropdownMenuItem(
//                                    text = { Text(name) },
//                                    onClick = {
//                                        onPickLoadName(name)
//                                        onLoadExpandedChange(false)
//                                    }
//                                )
//                            }
//                        }
//                    }

                    // Load Maps
                    FilledTonalButton(
                        onClick = onLoad,
                        enabled = savedMaps.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Load")
                    }

                    // Robot Sync Button
                    FilledTonalButton(
                        // Sync with Robot
                        onClick = onSync,
                        enabled = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Sync")
                    }

                }
            }
        }

        // Controls card (bottom)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Controls",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                StatusRow(
                    title = statusTitle,
                    subtitle = statusSubtitle,
                    onOpenLog = onOpenLog,
                )

                // D-pad (cross layout like screenshot)
                DPad(onDirection = onDirection, modifier = Modifier.align(Alignment.CenterHorizontally))
            }
        }
    }
}

@Composable
private fun ModeIcon(
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (selected) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    val stroke = if (selected) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = bg,
        border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(stroke))
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusRow(
    title: String,
    subtitle: String,
    onOpenLog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                maxLines = 1
            )
        }
//        IconButton(onClick = { /* settings */ }) {
//            Icon(Icons.Default.Settings, contentDescription = "Settings")
//        }
        // Open Message Log Button
        IconButton(onClick = onOpenLog) {
            Icon(Icons.Default.List, contentDescription = "Message Log")
        }


//        // simple "toggle" dot to mimic screenshot
//        Switch(
//            checked = true,
//            onCheckedChange = null,
//            enabled = false
//        )
    }
}

@Composable
private fun DPad(
    onDirection: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DirectionButton(
            direction = Direction.UP,
            onClick = { onDirection(Direction.UP) }
        )

        Spacer(Modifier.height(5.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            DirectionButton(Direction.LEFT, onClick = { onDirection(Direction.LEFT) })
            Spacer(Modifier.size(44.dp)) // empty center (like screenshot)
            DirectionButton(Direction.RIGHT, onClick = { onDirection(Direction.RIGHT) })
        }

        Spacer(Modifier.height(5.dp))

        DirectionButton(
            direction = Direction.DOWN,
            onClick = { onDirection(Direction.DOWN) }
        )
    }
}

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

    FilledTonalIconButton(
        onClick = onClick,
        modifier = modifier.size(44.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null)
    }
}

private fun editModeLabel(mode: MapEditMode): String = when (mode) {
    MapEditMode.Cursor -> "None"
    MapEditMode.SetStart -> "Place Robot"
    MapEditMode.PlaceObstacle -> "Add Obstacle"
    MapEditMode.DragObstacle -> "Drag Obstacle"
    MapEditMode.ChangeObstacleFace -> "Change Face"
}