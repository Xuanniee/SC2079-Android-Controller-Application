package com.sc2079.androidcontroller.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.R
import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import kotlinx.coroutines.launch

/**
 * Data class for map mode items
 */
data class ModeItem(
    val mode: MapEditMode,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * Map Mode Toolbar - Horizontal toolbar with mode selection buttons
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapModeToolbar(
    selectedMode: MapEditMode,
    onModeSelected: (MapEditMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modeItems = listOf(
        ModeItem(MapEditMode.Cursor, stringResource(R.string.cursor), Icons.Filled.Settings),
        ModeItem(MapEditMode.SetStart, stringResource(R.string.set_start), Icons.Filled.Flag),
        ModeItem(MapEditMode.PlaceObstacle, stringResource(R.string.place_obstacle), Icons.Filled.Edit),
        ModeItem(MapEditMode.DragObstacle, stringResource(R.string.drag_obstacle), Icons.Filled.OpenWith),
        ModeItem(MapEditMode.ChangeObstacleFace, stringResource(R.string.change_face), Icons.Filled.Gesture)
    )
    
    val scope = rememberCoroutineScope()
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        modeItems.forEach { item ->
            val isSelected = selectedMode == item.mode
            val tooltipState = rememberTooltipState()
            
            TooltipBox(
                positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                tooltip = {
                    PlainTooltip {
                        Text(item.label)
                    }
                },
                state = tooltipState
            ) {
                Box(
                    modifier = Modifier
                        .pointerInput(item.mode) {
                            detectTapGestures(
                                onLongPress = {
                                    scope.launch {
                                        tooltipState.show()
                                    }
                                }
                            )
                        }
                ) {
                    IconButton(
                        onClick = { onModeSelected(item.mode) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.tertiary else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(24.dp),
                            tint = if (isSelected) {
                                MaterialTheme.colorScheme.onTertiary
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                        )
                    }
                }
            }
        }
    }
}
