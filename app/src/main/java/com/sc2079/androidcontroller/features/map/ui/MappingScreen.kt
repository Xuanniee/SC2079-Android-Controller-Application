package com.sc2079.androidcontroller.features.map.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import com.sc2079.androidcontroller.features.map.presentation.MapUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MappingScreen(
    uiState: MapUiState,
    onSetMode: (MapEditMode) -> Unit,
    onReset: () -> Unit,
    onSendClear: () -> Unit,
    onSaveMap: (String) -> Unit,
    onLoadMap: (String) -> Unit,
    onDeleteMap: (String) -> Unit,

    // map interactions
    onTapCell: (x: Int, y: Int) -> Unit,
    onTapObstacleForFace: (obstacleNo: Int) -> Unit,
    onStartDragObstacle: (obstacleNo: Int) -> Unit,
    onDragObstacleToCell: (obstacleNo: Int, x: Int, y: Int) -> Unit,
    onDragOutsideRemove: (obstacleNo: Int) -> Unit,
    onEndDrag: () -> Unit
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveName by remember { mutableStateOf("map1") }

    // Dropdown state
    var expanded by remember { mutableStateOf(false) }

    val robotLabel = uiState.robotPosition?.let { "(${it.x},${it.y}) ${it.faceDir}" } ?: "(not set)"
    val modeItems = remember {
        listOf(
            ModeItem(MapEditMode.Cursor, "Cursor", Icons.Filled.Settings),
            ModeItem(MapEditMode.SetStart, "Set Start", Icons.Filled.Flag),
            ModeItem(MapEditMode.PlaceObstacle, "Place Obstacle", Icons.Filled.Edit),
            ModeItem(MapEditMode.DragObstacle, "Drag Obstacle", Icons.Filled.OpenWith),
            ModeItem(MapEditMode.ChangeObstacleFace, "Change Face", Icons.Filled.Gesture)
        )
    }
    val selectedMode = modeItems.first { it.mode == uiState.editMode }

    Column(
        Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // --- Top row: Robot + Reset/Save (left cluster) + Mode dropdown (right) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left cluster takes remaining space
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text("Robot: $robotLabel") }
                )

                // Reset + Save beside robot
                OutlinedButton(
                    onClick = {
                        onReset()
                        onSendClear()
                    }
                ) { Text("Reset") }

                OutlinedButton(
                    onClick = { showSaveDialog = true }
                ) { Text("Save") }
            }

            // Map Mode dropdown on the RIGHT
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    modifier = Modifier
                        .menuAnchor()
                        .widthIn(min = 180.dp),
                    readOnly = true,
                    value = selectedMode.label,
                    onValueChange = {},
                    singleLine = true,
                    label = { Text("Map Mode") },
                    leadingIcon = { Icon(selectedMode.icon, contentDescription = null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    modeItems.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item.label) },
                            leadingIcon = { Icon(item.icon, contentDescription = null) },
                            onClick = {
                                expanded = false
                                onSetMode(item.mode)
                            }
                        )
                    }
                }
            }
        }

        // Optional: show STATUS text under the top row if you want (keeps UI clean)
        if (uiState.robotStatus.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Status: ${uiState.robotStatus}",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // --- Saved maps list (kept, but without the "Actions" card wrapper) ---
        if (uiState.savedMaps.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Card {
                Column(Modifier.padding(12.dp)) {
                    Text("Saved Maps", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))

                    uiState.savedMaps.forEach { name ->
                        Row(
                            Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(name, modifier = Modifier.weight(1f))
                            TextButton(onClick = { onLoadMap(name) }) { Text("Load") }
                            TextButton(onClick = { onDeleteMap(name) }) { Text("Delete") }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // --- Map Canvas ---
        GridMapCanvas(
            uiState = uiState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            onTapCell = onTapCell,
            onTapObstacleForFace = onTapObstacleForFace,
            onStartDragObstacle = onStartDragObstacle,
            onDragObstacleToCell = onDragObstacleToCell,
            onDragOutsideRemove = onDragOutsideRemove,
            onEndDrag = onEndDrag
        )
    }

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
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSaveMap(saveName)
                    showSaveDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
            }
        )
    }
}

private data class ModeItem(
    val mode: MapEditMode,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)


//package com.sc2079.androidcontroller.features.map.ui
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Edit
//import androidx.compose.material.icons.filled.Flag
//import androidx.compose.material.icons.filled.Gesture
//import androidx.compose.material.icons.filled.OpenWith
//import androidx.compose.material.icons.filled.PanToolAlt
//import androidx.compose.material.icons.filled.Settings
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.unit.dp
//import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
//import com.sc2079.androidcontroller.features.map.presentation.MapUiState
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun MappingScreen(
//    uiState: MapUiState,
//    onSetMode: (MapEditMode) -> Unit,
//    onReset: () -> Unit,
//    onSendClear: () -> Unit,
//    onSaveMap: (String) -> Unit,
//    onLoadMap: (String) -> Unit,
//    onDeleteMap: (String) -> Unit,
//
//    // map interactions
//    onTapCell: (x: Int, y: Int) -> Unit,
//    onTapObstacleForFace: (obstacleNo: Int) -> Unit,
//    onStartDragObstacle: (obstacleNo: Int) -> Unit,
//    onDragObstacleToCell: (obstacleNo: Int, x: Int, y: Int) -> Unit,
//    onDragOutsideRemove: (obstacleNo: Int) -> Unit,
//    onEndDrag: () -> Unit
//) {
//    var showSaveDialog by remember { mutableStateOf(false) }
//    var saveName by remember { mutableStateOf("map1") }
//
//    // Dropdown state
//    var expanded by remember { mutableStateOf(false) }
//
//    val robotLabel = uiState.robotPosition?.let { "(${it.x},${it.y}) ${it.faceDir}" } ?: "(not set)"
//    val modeItems = remember {
//        listOf(
//            ModeItem(MapEditMode.Cursor, "Cursor", Icons.Filled.Settings),
//            ModeItem(MapEditMode.SetStart, "Set Start", Icons.Filled.Flag),
//            ModeItem(MapEditMode.PlaceObstacle, "Place Obstacle", Icons.Filled.Edit),
//            ModeItem(MapEditMode.DragObstacle, "Drag Obstacle", Icons.Filled.OpenWith),
//            ModeItem(MapEditMode.ChangeObstacleFace, "Change Face", Icons.Filled.Gesture)
//        )
//    }
//    val selectedMode = modeItems.first { it.mode == uiState.editMode }
//
//    Column(
//        Modifier
//            .fillMaxSize()
//            .padding(12.dp)
//    ) {
//        // --- Top row: Robot label (left) + Mode dropdown (right), aligned ---
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            // Robot status chip/card
//            AssistChip(
//                onClick = {},
//                label = { Text("Robot: $robotLabel") }
//            )
//
//            Spacer(Modifier.weight(1f))
//
//            // Map Mode dropdown on the RIGHT, level with Robot label
//            ExposedDropdownMenuBox(
//                expanded = expanded,
//                onExpandedChange = { expanded = !expanded }
//            ) {
//                OutlinedTextField(
//                    modifier = Modifier
//                        .menuAnchor()
//                        .widthIn(min = 180.dp), // keeps it compact on tablets
//                    readOnly = true,
//                    value = selectedMode.label,
//                    onValueChange = {},
//                    singleLine = true,
//                    label = { Text("Map Mode") },
//                    leadingIcon = { Icon(selectedMode.icon, contentDescription = null) },
//                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
//                )
//
//                ExposedDropdownMenu(
//                    expanded = expanded,
//                    onDismissRequest = { expanded = false }
//                ) {
//                    modeItems.forEach { item ->
//                        DropdownMenuItem(
//                            text = { Text(item.label) },
//                            leadingIcon = { Icon(item.icon, contentDescription = null) },
//                            onClick = {
//                                expanded = false
//                                onSetMode(item.mode)
//                            }
//                        )
//                    }
//                }
//            }
//        }
//
//        // Optional: show STATUS text under the top row if you want (keeps UI clean)
//        if (uiState.robotStatus.isNotBlank()) {
//            Spacer(Modifier.height(8.dp))
//            Text(
//                text = "Status: ${uiState.robotStatus}",
//                style = MaterialTheme.typography.bodyMedium
//            )
//        }
//
//        Spacer(Modifier.height(12.dp))
//
//        // --- Actions card (Reset / Save / Saved maps) ---
//        Card {
//            Column(Modifier.padding(12.dp)) {
//                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                    OutlinedButton(onClick = {
//                        onReset()
//                        onSendClear()
//                    }) { Text("Reset") }
//
//                    OutlinedButton(onClick = { showSaveDialog = true }) { Text("Save") }
//                }
//
//                if (uiState.savedMaps.isNotEmpty()) {
//                    Spacer(Modifier.height(12.dp))
//                    Text("Saved Maps", style = MaterialTheme.typography.titleSmall)
//
//                    uiState.savedMaps.forEach { name ->
//                        Row(
//                            Modifier.fillMaxWidth(),
//                            verticalAlignment = Alignment.CenterVertically,
//                            horizontalArrangement = Arrangement.spacedBy(8.dp)
//                        ) {
//                            Text(name, modifier = Modifier.weight(1f))
//                            TextButton(onClick = { onLoadMap(name) }) { Text("Load") }
//                            TextButton(onClick = { onDeleteMap(name) }) { Text("Delete") }
//                        }
//                    }
//                }
//            }
//        }
//
//        Spacer(Modifier.height(12.dp))
//
//        // --- Map Canvas ---
//        GridMapCanvas(
//            uiState = uiState,
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(1f),
//            onTapCell = onTapCell,
//            onTapObstacleForFace = onTapObstacleForFace,
//            onStartDragObstacle = onStartDragObstacle,
//            onDragObstacleToCell = onDragObstacleToCell,
//            onDragOutsideRemove = onDragOutsideRemove,
//            onEndDrag = onEndDrag
//        )
//    }
//
//    // --- Save dialog ---
//    if (showSaveDialog) {
//        AlertDialog(
//            onDismissRequest = { showSaveDialog = false },
//            title = { Text("Save Map") },
//            text = {
//                Column {
//                    OutlinedTextField(
//                        value = saveName,
//                        onValueChange = { saveName = it },
//                        label = { Text("Name") },
//                        singleLine = true
//                    )
//                }
//            },
//            confirmButton = {
//                TextButton(onClick = {
//                    onSaveMap(saveName)
//                    showSaveDialog = false
//                }) { Text("Save") }
//            },
//            dismissButton = {
//                TextButton(onClick = { showSaveDialog = false }) { Text("Cancel") }
//            }
//        )
//    }
//}
//
//private data class ModeItem(
//    val mode: MapEditMode,
//    val label: String,
//    val icon: androidx.compose.ui.graphics.vector.ImageVector
//)
