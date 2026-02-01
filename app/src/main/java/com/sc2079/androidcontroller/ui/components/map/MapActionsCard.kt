package com.sc2079.androidcontroller.ui.components.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.R
import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode

/**
 * Map Actions Card - Contains toolbar and save/reset buttons
 */
@Composable
fun MapActionsCard(
    selectedMode: MapEditMode,
    onModeSelected: (MapEditMode) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    onLoad: () -> Unit,
    chipSplashTrigger: Int = 0,
    hideButtonText: Boolean = false,
    modifier: Modifier = Modifier
) {
    val modeItems = listOf(
        ModeItem(MapEditMode.Cursor, stringResource(R.string.cursor), Icons.Filled.Settings),
        ModeItem(MapEditMode.SetStart, stringResource(R.string.set_start), Icons.Filled.Flag),
        ModeItem(MapEditMode.PlaceObstacle, stringResource(R.string.place_obstacle), Icons.Filled.Edit),
        ModeItem(MapEditMode.DragObstacle, stringResource(R.string.drag_obstacle), Icons.Filled.OpenWith),
        ModeItem(MapEditMode.ChangeObstacleFace, stringResource(R.string.change_face), Icons.Filled.Gesture)
    )
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
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Confirmation Chip - always shown with splash effect
            ChipWithSplash(
                chipSplashTrigger = chipSplashTrigger,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Map Mode Toolbar
            Text(
                text = "${stringResource(R.string.map_mode)}: ${selectedModeItem.label}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            MapModeToolbar(
                selectedMode = selectedMode,
                onModeSelected = onModeSelected
            )
            
            // Reset, Save, and Load Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Reset Map Button - Red background
                MapActionButton(
                    onClick = onReset,
                    icon = Icons.Filled.Delete,
                    text = stringResource(R.string.reset_map),
                    contentDescription = stringResource(R.string.reset_map),
                    backgroundColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError,
                    hideText = hideButtonText,
                    modifier = Modifier.weight(1f)
                )

                // Save Map Button - Tertiary background
                MapActionButton(
                    onClick = onSave,
                    icon = Icons.Filled.Save,
                    text = stringResource(R.string.save_map),
                    contentDescription = stringResource(R.string.save_map),
                    backgroundColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    hideText = hideButtonText,
                    modifier = Modifier.weight(1f)
                )
                
                // Load Map Button - Tertiary background
                MapActionButton(
                    onClick = onLoad,
                    icon = Icons.Filled.Folder,
                    text = stringResource(R.string.load_map),
                    contentDescription = stringResource(R.string.load_map),
                    backgroundColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    hideText = hideButtonText,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
