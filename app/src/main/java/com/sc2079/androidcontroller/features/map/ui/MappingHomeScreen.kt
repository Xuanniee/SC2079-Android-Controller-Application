package com.sc2079.androidcontroller.features.map.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel
import com.sc2079.androidcontroller.features.map.data.local.MapPreferencesDataSource
import com.sc2079.androidcontroller.features.map.data.repository.MapRepositoryImpl
import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import com.sc2079.androidcontroller.features.map.presentation.MapViewModel
import com.sc2079.androidcontroller.features.map.presentation.MapViewModelFactory
import com.sc2079.androidcontroller.features.map.presentation.RobotProtocol
import com.sc2079.androidcontroller.features.map.domain.model.FaceDir

@Composable
fun MappingHomeScreen(
    bluetoothViewModel: BluetoothViewModel
) {
    // Simple local wiring; later you can inject with DI.
    val context = androidx.compose.ui.platform.LocalContext.current
    val repo = remember { MapRepositoryImpl(MapPreferencesDataSource(context.applicationContext)) }

    val mapViewModel: MapViewModel = viewModel(factory = MapViewModelFactory(repo))
    val uiState by mapViewModel.uiState.collectAsState()

    // Process only new robot messages (so recomposition doesn't re-apply)
    val btUi by bluetoothViewModel.bluetoothUiState.collectAsState()
    var lastProcessedIdx by remember { mutableIntStateOf(0) }

    // Apply incoming messages to map state (C10/C9)
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
