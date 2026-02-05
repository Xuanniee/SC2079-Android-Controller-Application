package com.sc2079.androidcontroller.features.map.ui.screens

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
import com.sc2079.androidcontroller.features.map.presentation.RobotProtocolParser
import com.sc2079.androidcontroller.features.map.ui.MappingScreen
import com.sc2079.androidcontroller.features.map.ui.components.DirectionPickerDialog
import com.sc2079.androidcontroller.features.map.ui.components.MessageLogBottomDialog
import com.sc2079.androidcontroller.features.map.ui.sections.RightPanel

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
            .filter { message ->
                // Get the message and filter to allow it as long as it is protocl or robot meessage
                val messageBody = message.messageBody
                RobotProtocol.validateProtocolMessage(messageBody) || RobotProtocol.validateRobotMessage(messageBody)
            }
            .map { Message(fromRobot = it.fromRobot, messageBody = it.messageBody) }
    }

    // Runs only when a new message is detected
    LaunchedEffect(btUi.messages.size) {
        // Process new messages starting from the previous lastProcessedIdx
        val messages = btUi.messages

        for (i in lastProcessedIdx until messages.size) {
            // Only parse messages from Robot, not the User
            val message = messages[i]
            if (!message.fromRobot) {
                continue
            }

            // Only accept robot events (ROBOT / TARGET). Ignore anything else.
            val events = RobotProtocolParser.parseRobotBatch(message.messageBody)
            for (e in events) {
                mapViewModel.applyRobotEvent(e)
            }

            // For single event
//            val event = RobotProtocolParser.parseRobot(message.messageBody) ?: continue
//            // Apply the Robot Event to change
//            mapViewModel.applyRobotEvent(event)
        }
        lastProcessedIdx = messages.size
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

            // Track the BT Messages and show the latest filtered message
            val last = btUi.messages.lastOrNull { message ->
                // Only robot or protocol message allowed
                val messageBody = message.messageBody
                RobotProtocol.validateProtocolMessage(messageBody) || RobotProtocol.validateRobotMessage(messageBody)
            }
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
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // LEFT: Map surface
                    Card(
                        modifier = Modifier
                            .weight(0.7f)
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
                                    bluetoothViewModel.sendMessage(
                                        RobotProtocol.sendObstacleList(
                                            emptyList()
                                        )
                                    )
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
                            .weight(0.3f)
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
                                bluetoothViewModel.sendMessage(
                                    RobotProtocol.sendObstacleList(
                                        emptyList()
                                    )
                                )
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
                                if (uiState.editMode == MapEditMode.ChangeObstacleFace) facePickerForObstacle =
                                    no
                            },
                            onStartDragObstacle = { },
                            onDragObstacleToCell = { no, x, y ->
                                val moved = mapViewModel.onDragObstacle(no, x, y)
                                if (moved != null) bluetoothViewModel.sendMessage(
                                    RobotProtocol.upsertObstacle(
                                        moved
                                    )
                                )
                            },
                            onDragOutsideRemove = { no ->
                                val removed = mapViewModel.removeObstacleByNo(no)
                                if (removed != null) bluetoothViewModel.sendMessage(
                                    RobotProtocol.removeObstacle(
                                        removed
                                    )
                                )
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
