package com.sc2079.androidcontroller.features.map.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel
import com.sc2079.androidcontroller.features.map.data.local.MapPreferencesDataSource
import com.sc2079.androidcontroller.features.map.data.repository.MapRepositoryImpl
import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import com.sc2079.androidcontroller.features.map.presentation.MapViewModel
import com.sc2079.androidcontroller.features.map.presentation.MapViewModelFactory
import com.sc2079.androidcontroller.features.map.presentation.RobotProtocol
import com.sc2079.androidcontroller.ui.components.FadeInAnimation

/**
 * Direction enum for arrow buttons
 */
enum class Direction { UP, DOWN, LEFT, RIGHT }

@Composable
fun MappingHomeScreen(
    bluetoothViewModel: BluetoothViewModel
) {
    val context = LocalContext.current
    val repo = remember { MapRepositoryImpl(MapPreferencesDataSource(context.applicationContext)) }

    val mapViewModel: MapViewModel = viewModel(factory = MapViewModelFactory(repo))
    val uiState by mapViewModel.uiState.collectAsState()

    // Process only new robot messages (so recomposition doesn't re-apply)
    val btUi by bluetoothViewModel.bluetoothUiState.collectAsState()
    var lastProcessedIdx by remember { mutableIntStateOf(0) }

    LaunchedEffect(btUi.messages.size) {
        val msgs = btUi.messages
        for (i in lastProcessedIdx until msgs.size) {
            val m = msgs[i]
            if (m.fromRobot) mapViewModel.applyRobotMessage(m.messageBody)
        }
        lastProcessedIdx = msgs.size
    }

    // Face picker dialog state
    var facePickerForObstacle by remember { mutableStateOf<Int?>(null) }
    val obstacleNo = facePickerForObstacle

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        // Height of the bottom controls bar we overlay
        val bottomControlsHeight = if (maxWidth > maxHeight) 88.dp else 104.dp

        Box(Modifier.fillMaxSize()) {
            // Give MappingScreen bottom padding so it won't be covered by the overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = bottomControlsHeight + 12.dp)
            ) {
                FadeInAnimation(durationMillis = 800) {
                    MappingScreen(
                        uiState = uiState,
                        onSetMode = { mapViewModel.setEditMode(it) },
                        onReset = { mapViewModel.resetAll() },
                        onSendClear = { bluetoothViewModel.sendMessage(RobotProtocol.clear()) },
                        onSaveMap = { mapViewModel.saveCurrentMap(it) },
                        onLoadMap = { mapViewModel.loadMap(it) },
                        onDeleteMap = { mapViewModel.deleteMap(it) },

                        onTapCell = { x, y ->
                            val added = mapViewModel.onTapCell(x, y)
                            if (added != null) {
                                bluetoothViewModel.sendMessage(RobotProtocol.upsertObstacle(added))
                            }
                        },
                        onTapObstacleForFace = { no ->
                            if (uiState.editMode == MapEditMode.ChangeObstacleFace) {
                                facePickerForObstacle = no
                            }
                        },
                        onStartDragObstacle = { /* no-op */ },
                        onDragObstacleToCell = { no, x, y ->
                            val moved = mapViewModel.onDragObstacle(no, x, y)
                            if (moved != null) bluetoothViewModel.sendMessage(RobotProtocol.upsertObstacle(moved))
                        },
                        onDragOutsideRemove = { no ->
                            val removed = mapViewModel.removeObstacleByNo(no)
                            if (removed != null) bluetoothViewModel.sendMessage(RobotProtocol.removeObstacle(removed))
                        },
                        onEndDrag = { /* no-op */ }
                    )
                }
            }

            // ✅ Bottom controls/status OVERLAY (guaranteed bottom)
            AnimatedVisibility(
                visible = true,
                enter = fadeIn(animationSpec = tween(durationMillis = 700, delayMillis = 150)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(bottomControlsHeight)
            ) {
                ControlsCard(
                    modifier = Modifier.fillMaxSize(),
                    statusTitle = "Activity Status",
                    statusSubtitle = buildString {
                        val last = btUi.messages.lastOrNull()
                        if (last == null) append("No messages yet")
                        else if (last.fromRobot) append("Robot: ${last.messageBody.take(24)}")
                        else append("You: ${last.messageBody.take(24)}")
                    },
                    onDirection = { _ ->
                        // TODO wire to your manual move protocol if you want
                        Unit
                    }
                )
            }
        }
    }

    // Face picker dialog (keep exactly as your original)
    if (obstacleNo != null) {
        val current = uiState.obstacles.firstOrNull { it.obstacleId == obstacleNo }?.faceDir ?: FaceDir.UP
        DirectionPickerDialog(
            title = "Obstacle $obstacleNo face",
            initial = current,
            onDismiss = { facePickerForObstacle = null },
            onConfirm = { newFace ->
                val updated = mapViewModel.setObstacleFace(obstacleNo, newFace)
                if (updated != null) bluetoothViewModel.sendMessage(RobotProtocol.upsertObstacle(updated))
                facePickerForObstacle = null
            }
        )
    }
}

/**
 * Controls card component (ported from HomeScreen styling)
 */
@Composable
private fun ControlsCard(
    statusTitle: String,
    statusSubtitle: String,
    onDirection: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Text(
                text = "Controls",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusCard(
                    title = statusTitle,
                    subtitle = statusSubtitle,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(12.dp))

                DirectionControlButtons(onDirection = onDirection)
            }
        }
    }
}

@Composable
private fun DirectionControlButtons(
    onDirection: (Direction) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        DirectionButton(direction = Direction.UP, onClick = { onDirection(Direction.UP) })
        DirectionButton(direction = Direction.DOWN, onClick = { onDirection(Direction.DOWN) })
        DirectionButton(direction = Direction.LEFT, onClick = { onDirection(Direction.LEFT) })
        DirectionButton(direction = Direction.RIGHT, onClick = { onDirection(Direction.RIGHT) })
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

    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(5.dp)),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
    }
}

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
        Column(Modifier.weight(1f, fill = false)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                maxLines = 1
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(18.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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


//package com.sc2079.androidcontroller.features.map.ui
//
//import androidx.compose.runtime.*
//import androidx.lifecycle.viewmodel.compose.viewModel
//import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel
//import com.sc2079.androidcontroller.features.map.data.local.MapPreferencesDataSource
//import com.sc2079.androidcontroller.features.map.data.repository.MapRepositoryImpl
//import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
//import com.sc2079.androidcontroller.features.map.presentation.MapViewModel
//import com.sc2079.androidcontroller.features.map.presentation.MapViewModelFactory
//import com.sc2079.androidcontroller.features.map.presentation.RobotProtocol
//import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
//
//@Composable
//fun MappingHomeScreen(
//    bluetoothViewModel: BluetoothViewModel
//) {
//    // Simple local wiring; later you can inject with DI.
//    val context = androidx.compose.ui.platform.LocalContext.current
//    val repo = remember { MapRepositoryImpl(MapPreferencesDataSource(context.applicationContext)) }
//
//    val mapViewModel: MapViewModel = viewModel(factory = MapViewModelFactory(repo))
//    val uiState by mapViewModel.uiState.collectAsState()
//
//    // Process only new robot messages (so recomposition doesn't re-apply)
//    val btUi by bluetoothViewModel.bluetoothUiState.collectAsState()
//    var lastProcessedIdx by remember { mutableIntStateOf(0) }
//
//    // Apply incoming messages to map state (C10/C9)
//    LaunchedEffect(btUi.messages.size) {
//        val msgs = btUi.messages
//        for (i in lastProcessedIdx until msgs.size) {
//            val m = msgs[i]
//            if (m.fromRobot) mapViewModel.applyRobotMessage(m.messageBody)
//        }
//        lastProcessedIdx = msgs.size
//    }
//
//    // Face picker dialog state
//    var facePickerForObstacle by remember { mutableStateOf<Int?>(null) }
//    val obstacleNo = facePickerForObstacle
//
//    MappingScreen(
//        uiState = uiState,
//        onSetMode = { mapViewModel.setEditMode(it) },
//        onReset = { mapViewModel.resetAll() },
//        onSendClear = { bluetoothViewModel.sendMessage(RobotProtocol.clear()) },
//        onSaveMap = { mapViewModel.saveCurrentMap(it) },
//        onLoadMap = { mapViewModel.loadMap(it) },
//        onDeleteMap = { mapViewModel.deleteMap(it) },
//
//        onTapCell = { x, y ->
//            val added = mapViewModel.onTapCell(x, y)
//            if (added != null) {
//                bluetoothViewModel.sendMessage(RobotProtocol.upsertObstacle(added))
//            }
//        },
//        onTapObstacleForFace = { no ->
//            if (uiState.editMode == MapEditMode.ChangeObstacleFace) {
//                facePickerForObstacle = no
//            }
//        },
//        onStartDragObstacle = { /* no-op */ },
//        onDragObstacleToCell = { no, x, y ->
//            val moved = mapViewModel.onDragObstacle(no, x, y)
//            if (moved != null) bluetoothViewModel.sendMessage(RobotProtocol.upsertObstacle(moved))
//        },
//        onDragOutsideRemove = { no ->
//            val removed = mapViewModel.removeObstacleByNo(no)
//            if (removed != null) bluetoothViewModel.sendMessage(RobotProtocol.removeObstacle(removed))
//        },
//        onEndDrag = { /* no-op */ }
//    )
//
//    if (obstacleNo != null) {
//        val current = uiState.obstacles.firstOrNull { it.obstacleId == obstacleNo }?.faceDir ?: FaceDir.UP
//        DirectionPickerDialog(
//            title = "Obstacle $obstacleNo face",
//            initial = current,
//            onDismiss = { facePickerForObstacle = null },
//            onConfirm = { newFace ->
//                val updated = mapViewModel.setObstacleFace(obstacleNo, newFace)
//                if (updated != null) bluetoothViewModel.sendMessage(RobotProtocol.upsertObstacle(updated))
//                facePickerForObstacle = null
//            }
//        )
//    }
//}
