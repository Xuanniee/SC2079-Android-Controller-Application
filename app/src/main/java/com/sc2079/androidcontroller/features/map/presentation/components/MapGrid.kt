package com.sc2079.androidcontroller.features.map.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.ui.theme.CustomSuccess
import com.sc2079.androidcontroller.ui.theme.SC2079AndroidControllerApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Data class representing a position in the grid
 */
data class GridPosition(
    val row: Int,
    val column: Int
)

/**
 * Data class for cell state
 */
data class CellState(
    val position: GridPosition,
    val isSelected: Boolean = false,
    val isConfirmed: Boolean = false,
    val cellType: CellType = CellType.EMPTY
)

/**
 * Enum representing different cell types for the map
 */
enum class CellType {
    EMPTY,
    OBSTACLE,
    ROBOT,
    TARGET,
    PATH,
    EXPLORED
}

/**
 * A 15x21 grid component for the map view.
 * Each cell is clickable and fires an event with its position.
 * The grid fills all available width and is scrollable if needed.
 *
 * @param columns Number of columns (default 15)
 * @param rows Number of rows (default 21)
 * @param cellStates Map of grid positions to their states
 * @param onCellClick Callback fired when a cell is clicked with its position
 * @param modifier Modifier for the grid
 */
@Composable
fun MapGrid(
    modifier: Modifier = Modifier,
    columns: Int = 15,
    rows: Int = 21,
    cellStates: Map<GridPosition, CellState> = emptyMap(),
    onCellClick: (GridPosition) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        for (row in 0 until rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                for (column in 0 until columns) {
                    val position = GridPosition(row, column)
                    val cellState = cellStates[position] ?: CellState(position)
                    
                    MapBox(
                        cellState = cellState,
                        onClick = { onCellClick(position) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * MapBox component with animation support
 * - Single tap: Shows red flashing radius animation
 * - Long press (3+ seconds): Becomes confirmed state, red border disappears
 */
@Composable
private fun MapBox(
    cellState: CellState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 4.dp
    val shape = RoundedCornerShape(cornerRadius)
    
    // Animation state for flashing red radius
    var showFlashAnimation by remember { mutableStateOf(false) }
    var isLongPressing by remember { mutableStateOf(false) }
    var confirmedState by remember(cellState.position) { 
        mutableStateOf(cellState.isConfirmed) 
    }
    
    // Tap detection for double and triple taps
    var lastTapTime by remember { mutableStateOf(0L) }
    var tapCount by remember { mutableStateOf(0) }
    val tapTimeout = 300L // 300ms window for tap sequences
    
    // Update confirmed state when cellState changes
    LaunchedEffect(cellState.isConfirmed) {
        confirmedState = cellState.isConfirmed
    }
    
    // Handle flash animation timeout - changed to 10 seconds for auto-cancel
    LaunchedEffect(showFlashAnimation) {
        if (showFlashAnimation && !confirmedState) {
            delay(10000) // 10 seconds
            showFlashAnimation = false
        }
    }
    
    // Handle long press reset - wait 3 seconds
    LaunchedEffect(isLongPressing) {
        if (isLongPressing) {
            delay(3000) // 3 seconds
            // If still pressing after 3 seconds, reset the state
            if (isLongPressing) {
                confirmedState = false
                showFlashAnimation = false
                isLongPressing = false
            }
        }
    }
    
    // Flashing animation
    val flashAnimation by animateFloatAsState(
        targetValue = if (showFlashAnimation && !confirmedState) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "flash"
    )
    
    val backgroundColor = when {
        confirmedState -> CustomSuccess // Green background when confirmed
        cellState.isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> getCellColor(cellState.cellType)
    }
    
    // Red border only shows when not confirmed and (selected or flashing)
    val showRedBorder = !confirmedState && (cellState.isSelected || showFlashAnimation)
    val borderColor = when {
        showRedBorder -> Color.Red
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }
    
    val density = LocalDensity.current
    
    Box(
        modifier = modifier
            .wrapContentSize()
    ) {
        // Map box cell
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(shape)
                .background(backgroundColor, shape)
                .border(
                    width = if (showRedBorder) 2.dp else 0.5.dp,
                    color = borderColor,
                    shape = shape
                )
                .pointerInput(cellState.position) {
                detectTapGestures(
                    onTap = {
                        // Handle single tap, double tap, or triple tap
                        val currentTime = System.currentTimeMillis()
                        val timeSinceLastTap = currentTime - lastTapTime
                        
                        if (timeSinceLastTap < tapTimeout) {
                            // Tap within timeout window - increment count
                            tapCount++
                        } else {
                            // New tap sequence - reset count
                            tapCount = 1
                        }
                        
                        when (tapCount) {
                            1 -> {
                                // Single tap - show flash animation
                                if (!confirmedState) {
                                    showFlashAnimation = true
                                }
                                onClick()
                            }
                            2 -> {
                                // Double tap - confirm the state
                                confirmedState = true
                                showFlashAnimation = false
                                onClick()
                            }
                            3 -> {
                                // Triple tap - reset the state
                                confirmedState = false
                                showFlashAnimation = false
                                tapCount = 0 // Reset count after triple tap
                                onClick()
                            }
                        }
                        
                        lastTapTime = currentTime
                    },
                    onPress = {
                        // Start long press detection
                        // First, trigger flash animation for immediate feedback
                        showFlashAnimation = true
                        
                        // Wait a short time to distinguish tap from long press
                        val isQuickTap = withTimeoutOrNull(150) {
                            tryAwaitRelease()
                            true
                        } == true
                        
                        if (isQuickTap) {
                            // It was a quick tap, flash animation already started
                            // Don't call onClick here as onTap will handle it
                        } else {
                            // It's a long press - start long press detection
                            isLongPressing = true
                            // Use withTimeoutOrNull to check if 3 seconds pass before release
                            val result = withTimeoutOrNull(3000) {
                                tryAwaitRelease()
                            }
                            // If result is null, timeout occurred (3 seconds passed while still pressing)
                            // Reset the state immediately
                            if (result == null) {
                                // 3 seconds passed while still pressing - reset the state
                                confirmedState = false
                                showFlashAnimation = false
                            }
                            // Reset long pressing flag
                            isLongPressing = false
                        }
                    }
                )
            }
        ) {
            // Draw flashing red radius animation
            if (showFlashAnimation && !confirmedState) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val maxRadius = size.minDimension / 2
                    val animatedRadius = maxRadius * flashAnimation
                    
                    // Draw with higher alpha for better visibility
                    drawCircle(
                        color = Color.Red.copy(alpha = 0.5f * flashAnimation),
                        radius = animatedRadius,
                        center = center
                    )
                }
            }
        }
    }
}

/**
 * Get the color for a specific cell type using theme colors
 * Map tiles (EMPTY) use tertiary color
 */
@Composable
private fun getCellColor(cellType: CellType): Color {
    return when (cellType) {
        CellType.EMPTY -> MaterialTheme.colorScheme.tertiary // Map tiles use tertiary
        CellType.OBSTACLE -> MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
        CellType.ROBOT -> MaterialTheme.colorScheme.primary
        CellType.TARGET -> MaterialTheme.colorScheme.error
        CellType.PATH -> MaterialTheme.colorScheme.secondary
        CellType.EXPLORED -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
    }
}

@Preview(showBackground = true)
@Composable
fun MapGridPreview() {
    SC2079AndroidControllerApplicationTheme {
        val sampleStates = mapOf(
            GridPosition(5, 7) to CellState(GridPosition(5, 7), cellType = CellType.ROBOT),
            GridPosition(10, 10) to CellState(GridPosition(10, 10), cellType = CellType.TARGET),
            GridPosition(3, 3) to CellState(GridPosition(3, 3), cellType = CellType.OBSTACLE),
            GridPosition(3, 4) to CellState(GridPosition(3, 4), cellType = CellType.OBSTACLE),
            GridPosition(4, 3) to CellState(GridPosition(4, 3), cellType = CellType.OBSTACLE),
        )
        
        MapGrid(
            cellStates = sampleStates,
            onCellClick = { position ->
                println("Clicked cell at row=${position.row}, column=${position.column}")
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MapGridWithSelectionPreview() {
    SC2079AndroidControllerApplicationTheme {
        val sampleStates = mapOf(
            GridPosition(5, 7) to CellState(GridPosition(5, 7), isSelected = true),
        )
        
        MapGrid(
            cellStates = sampleStates,
            onCellClick = {}
        )
    }
}
