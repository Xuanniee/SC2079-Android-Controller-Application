package com.sc2079.androidcontroller.features.map.ui.sections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import com.sc2079.androidcontroller.features.map.ui.components.DPad
import com.sc2079.androidcontroller.features.map.ui.util.Direction
import com.sc2079.androidcontroller.features.map.ui.components.ModeIcon
import com.sc2079.androidcontroller.features.map.ui.components.StatusRow
import com.sc2079.androidcontroller.features.map.ui.util.editModeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RightPanel(
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


//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun RightPanel(
//    editMode: MapEditMode,
//    savedMaps: List<String>,
//    selectedLoadName: String,
//    loadExpanded: Boolean,
//    onLoadExpandedChange: (Boolean) -> Unit,
//    onPickLoadName: (String) -> Unit,
//    onSetMode: (MapEditMode) -> Unit,
//    onReset: () -> Unit,
//    onSave: () -> Unit,
//    onLoad: () -> Unit,
//    statusTitle: String,
//    statusSubtitle: String,
//    onOpenLog: () -> Unit,
//    onSync: () -> Unit,
//    onDirection: (Direction) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    Column(
//        modifier = modifier,
//        verticalArrangement = Arrangement.spacedBy(12.dp)
//    ) {
//        Card(
//            modifier = Modifier.fillMaxWidth(),
//            shape = RoundedCornerShape(20.dp),
//            colors = CardDefaults.cardColors(
//                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
//            ),
//            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//        ) {
//            Column(Modifier.padding(14.dp)) {
//                Text(
//                    text = "Map Mode: ${editModeLabel(editMode)}",
//                    style = MaterialTheme.typography.titleSmall,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//
//                Spacer(Modifier.height(12.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.SpaceEvenly,
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    ModeIcon(Icons.Default.Settings, editMode == MapEditMode.Cursor) { onSetMode(MapEditMode.Cursor) }
//                    ModeIcon(Icons.Default.Flag, editMode == MapEditMode.SetStart) { onSetMode(MapEditMode.SetStart) }
//                    ModeIcon(Icons.Default.Edit, editMode == MapEditMode.PlaceObstacle) { onSetMode(MapEditMode.PlaceObstacle) }
//                    ModeIcon(Icons.Default.OpenWith, editMode == MapEditMode.DragObstacle) { onSetMode(MapEditMode.DragObstacle) }
//                    ModeIcon(Icons.Default.Gesture, editMode == MapEditMode.ChangeObstacleFace) { onSetMode(MapEditMode.ChangeObstacleFace) }
//                }
//
//                Spacer(Modifier.height(14.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(10.dp)
//                ) {
//                    FilledTonalButton(
//                        onClick = onReset,
//                        modifier = Modifier.weight(1f),
//                        shape = RoundedCornerShape(16.dp)
//                    ) {
//                        Icon(Icons.Default.Delete, contentDescription = null)
//                        Spacer(Modifier.width(8.dp))
//                        Text("Reset Map")
//                    }
//
//                    FilledTonalButton(
//                        onClick = onSave,
//                        modifier = Modifier.weight(1f),
//                        shape = RoundedCornerShape(16.dp)
//                    ) {
//                        Icon(Icons.Default.Save, contentDescription = null)
//                        Spacer(Modifier.width(8.dp))
//                        Text("Save Map")
//                    }
//                }
//
//                Spacer(Modifier.height(10.dp))
//
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                    horizontalArrangement = Arrangement.spacedBy(10.dp),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    FilledTonalButton(
//                        onClick = onLoad,
//                        enabled = savedMaps.isNotEmpty(),
//                        modifier = Modifier.weight(1f),
//                        shape = RoundedCornerShape(16.dp)
//                    ) {
//                        Icon(Icons.Default.FolderOpen, contentDescription = null)
//                        Spacer(Modifier.width(8.dp))
//                        Text("Load")
//                    }
//
//                    FilledTonalButton(
//                        onClick = onSync,
//                        enabled = true,
//                        modifier = Modifier.weight(1f),
//                        shape = RoundedCornerShape(16.dp)
//                    ) {
//                        Icon(Icons.Default.Sync, contentDescription = null)
//                        Spacer(Modifier.width(8.dp))
//                        Text("Sync")
//                    }
//                }
//            }
//        }
//
//        Card(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(1f, fill = false),
//            shape = RoundedCornerShape(18.dp),
//            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
//            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
//        ) {
//            Column(
//                modifier = Modifier.padding(14.dp),
//                verticalArrangement = Arrangement.spacedBy(12.dp)
//            ) {
//                Text(
//                    text = "Controls",
//                    style = MaterialTheme.typography.titleMedium,
//                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                )
//
//                StatusRow(
//                    title = statusTitle,
//                    subtitle = statusSubtitle,
//                    onOpenLog = onOpenLog,
//                )
//
//                DPad(
//                    onDirection = onDirection,
//                    modifier = Modifier.align(Alignment.CenterHorizontally)
//                )
//            }
//        }
//    }
//}
