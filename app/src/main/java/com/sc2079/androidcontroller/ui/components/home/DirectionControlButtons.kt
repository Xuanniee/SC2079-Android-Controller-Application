package com.sc2079.androidcontroller.ui.components.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Direction control buttons (Up, Down, Left, Right) - D-pad layout
 */
@Composable
fun DirectionControlButtons(
    onUpClick: () -> Unit = {},
    onDownClick: () -> Unit = {},
    onLeftClick: () -> Unit = {},
    onRightClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .wrapContentHeight()
            .fillMaxWidth()
    ) {
        // Container box to position buttons relative to center
        // Use a responsive size that scales with available width but has min/max constraints
        Box(
            modifier = Modifier
                .size(220.dp, 220.dp) // Slightly larger for better spacing
                .align(Alignment.Center)
        ) {
            // Button size is 60dp, so we'll use offsets to create nice spacing
            val buttonSize = 60.dp
            val spacing = 24.dp // Increased spacing between buttons for better visual separation
            
            // Top button - centered horizontally, offset up from center
            DirectionButton(
                direction = Direction.UP,
                onClick = onUpClick,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = -(buttonSize / 2 + spacing))
            )
            
            // Bottom button - centered horizontally, offset down from center
            DirectionButton(
                direction = Direction.DOWN,
                onClick = onDownClick,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = buttonSize / 2 + spacing)
            )
            
            // Left button - centered vertically, offset left from center
            DirectionButton(
                direction = Direction.LEFT,
                onClick = onLeftClick,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = -(buttonSize / 2 + spacing))
            )
            
            // Right button - centered vertically, offset right from center
            DirectionButton(
                direction = Direction.RIGHT,
                onClick = onRightClick,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(x = buttonSize / 2 + spacing)
            )
        }
    }
}
