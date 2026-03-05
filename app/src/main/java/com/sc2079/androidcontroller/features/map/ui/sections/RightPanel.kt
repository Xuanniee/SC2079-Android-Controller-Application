package com.sc2079.androidcontroller.features.map.ui.sections

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import com.sc2079.androidcontroller.R
import com.sc2079.androidcontroller.features.controller.domain.model.BluetoothStatus
import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import com.sc2079.androidcontroller.features.map.ui.components.DPad
import com.sc2079.androidcontroller.features.map.ui.util.Direction
import com.sc2079.androidcontroller.features.map.ui.components.ModeIcon
import com.sc2079.androidcontroller.features.map.ui.components.StatusRow
import com.sc2079.androidcontroller.features.map.ui.util.editModeLabel
import com.sc2079.androidcontroller.ui.components.home.StatusCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RightPanel(
    // For signaling an aggressive algo for retry
    retryEnabled: Boolean,
    onRetryChange: (Boolean) -> Unit,
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
    bluetoothStatus: BluetoothStatus,
    onOpenLog: () -> Unit,
    // To sync obstacle list with robot
    onSync: () -> Unit,
    onDirection: (Direction) -> Unit,
    // Bool to track if App is in Landscape or Protrait
    isLandscape: Boolean,
    // For switching RIght Handed Toggle
    isRightHanded: Boolean,
    modifier: Modifier = Modifier
) {
    // Track the Control Panel Tabs, 0 = Map Mode, 1 = Controls
    var selectedTab by remember { mutableStateOf(0) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Place a Tab Row at the Top for User to select Map or Controller Mode
        CustomTabRow(
            selectedTab = selectedTab,
            onTabSelected = {
                selectedTab = it
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Place the 2 Tabs here
        Crossfade(
            targetState = selectedTab,
            animationSpec = tween(durationMillis = 250),
            label = "rightpanel-tabs"
        ) { tab ->
            when (tab) {
                // Map Tab
                0 -> {
                    // Map Control Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                // Space evenly
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "Map Mode: ${editModeLabel(editMode)}",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                // Retry Toggle using Less Aggressive Algo
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Retry",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Switch(
                                            checked = retryEnabled,
                                            onCheckedChange = onRetryChange
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // 1. Contains all the Map Mode Buttons Control Panel
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

                            // 2. Actions row 1 - Reset + Sync Robot Messages
                            Row(
                                modifier = Modifier.fillMaxWidth(),
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

                            Spacer(Modifier.height(10.dp))

//                            // 3. Actions row 2 - Load button + Save
//                            Row(
//                                modifier = Modifier.fillMaxWidth(),
//                                horizontalArrangement = Arrangement.spacedBy(10.dp),
//                                verticalAlignment = Alignment.CenterVertically
//                            ) {
//                                // Save Button
//                                FilledTonalButton(
//                                    onClick = onSave,
//                                    modifier = Modifier.weight(1f),
//                                    shape = RoundedCornerShape(16.dp)
//                                ) {
//                                    Icon(Icons.Default.Save, contentDescription = null)
//                                    Spacer(Modifier.width(8.dp))
//                                    Text("Save Map")
//                                }
//
//                                // Load Maps
//                                FilledTonalButton(
//                                    onClick = onLoad,
//                                    enabled = savedMaps.isNotEmpty(),
//                                    modifier = Modifier.weight(1f),
//                                    shape = RoundedCornerShape(16.dp)
//                                ) {
//                                    Icon(Icons.Default.FolderOpen, contentDescription = null)
//                                    Spacer(Modifier.width(8.dp))
//                                    Text("Load")
//                                }
//                            }
                        }
                    }
                }
                // Other Tab - Controller
                else -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        // For Controller, I want the controls to be right of status update
                        // if portrait.
                        // Landscape, DPAD shud be below the updates
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Controls",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (isLandscape) {
                                // Landscape, show the BT Status, then Messages, then DPAD
                                StatusCard(
                                    title = stringResource(R.string.bluetooth_status),
                                    status = bluetoothStatus,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                StatusRow(
                                    title = statusTitle,
                                    subtitle = statusSubtitle,
                                    onOpenLog = onOpenLog,
                                )

                                Spacer(Modifier.height(12.dp))

                                // D-pad (cross layout like screenshot)
                                DPad(
                                    onDirection = onDirection,
                                    modifier = Modifier.align(Alignment.CenterHorizontally)
                                )
                            } else {
                                // Portrait Mode - Right Handled Control
                                if (isRightHanded) {
                                    // Default Toggle - So Dpad on the Right
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // Left Status about 60% of screen
                                        Column(
                                            modifier = Modifier
                                                .weight(0.6f),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            StatusCard(
                                                title = stringResource(R.string.bluetooth_status),
                                                status = bluetoothStatus,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            StatusRow(
                                                title = statusTitle,
                                                subtitle = statusSubtitle,
                                                onOpenLog = onOpenLog,
                                            )
                                        }

                                        // Divider
                                        VerticalDivider(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .padding(horizontal = 12.dp),
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        )

                                        // RIGHT — 40% Screen for DPAD
                                        Box(
                                            modifier = Modifier
                                                .weight(0.4f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            DPad(
                                                onDirection = onDirection,
                                                modifier = Modifier
                                                    .wrapContentWidth()
//                                                .align(Alignment.Top)
                                            )
                                        }
                                    }
                                } else {
                                    // Portrait Mode - Left Handled Control, Dpad on the left
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // RIGHT — 40% Screen for DPAD
                                        Box(
                                            modifier = Modifier
                                                .weight(0.4f),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            DPad(
                                                onDirection = onDirection,
                                                modifier = Modifier
                                                    .wrapContentWidth()
//                                                .align(Alignment.Top)
                                            )
                                        }

                                        // Divider
                                        VerticalDivider(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .padding(horizontal = 12.dp),
                                            thickness = 1.dp,
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        )

                                        // Left Status about 60% of screen
                                        Column(
                                            modifier = Modifier
                                                .weight(0.6f),
                                            verticalArrangement = Arrangement.spacedBy(3.dp)
                                        ) {
                                            StatusCard(
                                                title = stringResource(R.string.bluetooth_status),
                                                status = bluetoothStatus,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            StatusRow(
                                                title = statusTitle,
                                                subtitle = statusSubtitle,
                                                onOpenLog = onOpenLog,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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

@Composable
private fun CustomTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

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