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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.graphics.Paint
import androidx.compose.foundation.layout.BoxWithConstraints
import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
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
    val cellType: CellType = CellType.EMPTY,
    // Obstacle data
    val obstacleId: Int? = null,
    val obstacleFaceDir: com.sc2079.androidcontroller.features.map.domain.model.FaceDir? = null,
    val displayedTargetId: Int? = null,
    // Robot data
    val robotFaceDir: com.sc2079.androidcontroller.features.map.domain.model.FaceDir? = null
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
 * A 20x20 grid component for the map view.
 * Each cell is clickable and fires an event with its position.
 * The grid fills all available width and is scrollable if needed.
 *
 * @param columns Number of columns (default 20)
 * @param rows Number of rows (default 20)
 * @param cellStates Map of grid positions to their states
 * @param onCellClick Callback fired when a cell is clicked with its position
 * @param modifier Modifier for the grid
 */
@Composable
fun MapGrid(
    modifier: Modifier = Modifier,
    columns: Int = 20,
    rows: Int = 20,
    cellStates: Map<GridPosition, CellState> = emptyMap(),
    onCellClick: (GridPosition) -> Unit = {},
    onCellRemove: (GridPosition) -> Unit = {}
) {
    // Track the latest selected position to ensure only one box shows animation
    var latestSelectedPosition by remember { mutableStateOf<GridPosition?>(null) }

    BoxWithConstraints(
        modifier = modifier
    ) {
        val cellSize = minOf(maxWidth / columns, maxHeight / rows)
        Column(
            modifier = modifier
                .fillMaxWidth(),
//            .verticalScroll(rememberScrollState()),
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
                        val isLatestSelected = latestSelectedPosition == position

                        MapBox(
                            cellState = cellState,
                            isLatestSelected = isLatestSelected,
                            onClick = {
                                latestSelectedPosition = position
                                onCellClick(position)
                            },
                            onRemove = {
                                onCellRemove(position)
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
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
    isLatestSelected: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val cornerRadius = 4.dp
    val shape = RoundedCornerShape(cornerRadius)

    // Animation state for flashing red radius - only allowed if this is the latest selected
    var showFlashAnimation by remember { mutableStateOf(false) }

    // Track if this cell has a pending placement (obstacle/robot placed but not confirmed)
    var hasPendingPlacement by remember { mutableStateOf(false) }

    // Clear animation if this box is no longer the latest selected
    LaunchedEffect(isLatestSelected) {
        if (!isLatestSelected) {
            showFlashAnimation = false
            // Reset tracking when no longer selected
            hasPendingPlacement = false
        }
    }
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
        // If confirmed, no longer pending
        if (confirmedState) {
            hasPendingPlacement = false
        }
    }

    // Reset pending placement when animation stops (unless confirmed)
    LaunchedEffect(showFlashAnimation, confirmedState) {
        if (!showFlashAnimation && !confirmedState) {
            hasPendingPlacement = false
        }
    }

    /**
     * TODO Commenting this out to stop auto remove logic
     */
//    // Handle flash animation timeout - changed to 10 seconds for auto-cancel
//    // If timeout occurs and placement is not confirmed, remove it
//    LaunchedEffect(showFlashAnimation) {
//        if (showFlashAnimation && !confirmedState) {
//            delay(10000) // 10 seconds
//            showFlashAnimation = false
//            // If there's a pending placement that wasn't confirmed, remove it
//            if (hasPendingPlacement && !confirmedState) {
//                onRemove()
//                hasPendingPlacement = false
//            }
//        }
//    }

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

    // Looping border animation - snake-like movement
    val infiniteTransition = rememberInfiniteTransition(label = "looping_border")
    val borderProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "border_progress"
    )

    val backgroundColor = when {
        cellState.isSelected -> MaterialTheme.colorScheme.primaryContainer
        cellState.cellType == CellType.OBSTACLE -> Color(0xFFB39DDB) // Purple for obstacles
        cellState.cellType == CellType.ROBOT -> Color(0xFFE53935) // Red for robot
        else -> getCellColor(cellState.cellType)
    }

    /**
     * TODO Stop animated border for now as causing bugs
     */
    // Show animated border only if this is the latest selected box and not confirmed
    // Show animation if this box is the latest selected and flash animation is active
//    val showAnimatedBorder = !confirmedState && isLatestSelected && showFlashAnimation
    val showAnimatedBorder = false
    val staticBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val errorColor = MaterialTheme.colorScheme.error // Read error color in composable context

    val density = LocalDensity.current
    val textSizePx = with(density) { 11.sp.toPx() }
    val strokeWidth = with(density) { 5.dp.toPx() } // Increased from 3.dp to 5.dp for more visible border
    val cornerRadiusPx = with(density) { cornerRadius.toPx() }

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
                .then(
                    if (showAnimatedBorder) {
                        Modifier.drawWithContent {
                            drawContent() // Draws the background/content of the box

                            // Define the rounded rectangle path
                            val path = Path().apply {
                                val rect = Rect(Offset.Zero, size)
                                // Manually construct rounded rectangle path
                                val r = cornerRadiusPx
                                // Top edge
                                moveTo(r, 0f)
                                lineTo(size.width - r, 0f)
                                // Top-right corner
                                arcTo(
                                    rect = Rect(size.width - r * 2, 0f, size.width, r * 2),
                                    startAngleDegrees = -90f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                // Right edge
                                lineTo(size.width, size.height - r)
                                // Bottom-right corner
                                arcTo(
                                    rect = Rect(size.width - r * 2, size.height - r * 2, size.width, size.height),
                                    startAngleDegrees = 0f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                // Bottom edge
                                lineTo(r, size.height)
                                // Bottom-left corner
                                arcTo(
                                    rect = Rect(0f, size.height - r * 2, r * 2, size.height),
                                    startAngleDegrees = 90f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                // Left edge
                                lineTo(0f, r)
                                // Top-left corner
                                arcTo(
                                    rect = Rect(0f, 0f, r * 2, r * 2),
                                    startAngleDegrees = 180f,
                                    sweepAngleDegrees = 90f,
                                    forceMoveTo = false
                                )
                                close()
                            }

                            val pathMeasure = PathMeasure()
                            pathMeasure.setPath(path, false)

                            val totalLength = pathMeasure.length
                            val snakeLength = totalLength * 0.8f // 80% coverage
                            val startDistance = borderProgress * totalLength

                            val destinationPath = androidx.compose.ui.graphics.Path()

                            // Extract the segment and handle the wrap-around
                            if (startDistance + snakeLength <= totalLength) {
                                // Simple case: segment fits within the path length
                                pathMeasure.getSegment(
                                    startDistance,
                                    startDistance + snakeLength,
                                    destinationPath
                                )
                            } else {
                                // Wrap-around case: segment splits between end and start
                                pathMeasure.getSegment(startDistance, totalLength, destinationPath)
                                pathMeasure.getSegment(0f, (startDistance + snakeLength) % totalLength, destinationPath)
                            }

                            // Draw the animated path using theme error color
                            drawPath(
                                path = destinationPath,
                                color = errorColor,
                                style = Stroke(
                                    width = strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    } else {
                        Modifier.border(
                            width = 0.5.dp,
                            color = staticBorderColor,
                            shape = shape
                        )
                    }
                )
                .pointerInput(cellState.position) {
                detectTapGestures(
                    onTap = {
                        onClick()

                        /**
                         * TODO Temporary remove the logic to do the 3 taps as is causing bugs
                         */
//                        // Handle single tap, double tap, or triple tap
//                        val currentTime = System.currentTimeMillis()
//                        val timeSinceLastTap = currentTime - lastTapTime
//
//                        if (timeSinceLastTap < tapTimeout) {
//                            // Tap within timeout window - increment count
//                            tapCount++
//                        } else {
//                            // New tap sequence - reset count
//                            tapCount = 1
//                        }
//
//                        when (tapCount) {
//                            1 -> {
//                                // Single tap - show flash animation (will be cleared if not latest selected)
//                                if (!confirmedState) {
//                                    showFlashAnimation = true
//                                    // Check if cell was empty before tap - if so, mark as pending placement
//                                    // This will be checked after onClick() potentially places something
//                                    if (cellState.cellType == CellType.EMPTY) {
//                                        hasPendingPlacement = true
//                                    }
//                                }
//                                onClick()
//                            }
//                            2 -> {
//                                // Double tap - confirm the state
//                                confirmedState = true
//                                showFlashAnimation = false
//                                hasPendingPlacement = false // Clear pending placement on confirmation
//                                onClick()
//                            }
//                            3 -> {
//                                // Triple tap - reset the state and remove cell content
//                                confirmedState = false
//                                showFlashAnimation = false
//                                tapCount = 0 // Reset count after triple tap
//                                onRemove()
//                            }
//                        }

//                        lastTapTime = currentTime
                    },
                    onPress = {
                        // Start long press detection
                        // First, trigger flash animation for immediate feedback
                        if (!confirmedState) {
                            showFlashAnimation = true
                        }

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
                            // Reset the state immediately and remove cell content
                            if (result == null) {
                                // 3 seconds passed while still pressing - reset the state and remove cell content
                                confirmedState = false
                                showFlashAnimation = false
                                onRemove()
                            }
                            // Reset long pressing flag
                            isLongPressing = false
                        }
                    }
                )
            }
        ) {
            // Draw obstacle visuals (purple background, face direction stripe, text)
            if (cellState.cellType == CellType.OBSTACLE && cellState.obstacleId != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val rect = Rect(0f, 0f, size.width, size.height)

                    // Draw face direction stripe
                    cellState.obstacleFaceDir?.let { faceDir ->
                        val stripe = faceStripe(rect, faceDir)
                        drawRect(
                            color = Color(0xFF5E35B1), // Darker purple stripe
                            topLeft = stripe.topLeft,
                            size = stripe.size
                        )
                    }

                    // Draw text labels
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = Paint().apply {
                            isAntiAlias = true
                            textAlign = Paint.Align.CENTER
                            textSize = textSizePx
                        }
                        val centerX = rect.center.x
                        val centerY = rect.center.y

                        // Draw obstacle ID
                        paint.color = android.graphics.Color.BLACK
                        drawText("O${cellState.obstacleId}", centerX, centerY - paint.textSize * 0.1f, paint)

                        // Draw target ID if present
                        cellState.displayedTargetId?.let { targetId ->
                            paint.color = android.graphics.Color.DKGRAY
                            drawText("T$targetId", centerX, centerY + paint.textSize * 1.0f, paint)
                        }
                    }
                }
            }

            // Draw robot visuals (teal background, direction marker)
            if (cellState.cellType == CellType.ROBOT && cellState.robotFaceDir != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val d = size.minDimension * 0.25f
                    val marker = when (cellState.robotFaceDir) {
                        FaceDir.NORTH -> Offset(center.x, center.y - d)
                        FaceDir.SOUTH -> Offset(center.x, center.y + d)
                        FaceDir.WEST -> Offset(center.x - d, center.y)
                        FaceDir.EAST -> Offset(center.x + d, center.y)
                    }

                    drawCircle(
                        color = Color(0xFF004D40), // Dark teal for direction marker
                        radius = size.minDimension * 0.08f,
                        center = marker
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
        CellType.OBSTACLE -> Color(0xFFB39DDB) // Purple for obstacles (matches GridMapCanvas)
        CellType.ROBOT -> Color(0xFFE53935) // Red for robot
        CellType.TARGET -> MaterialTheme.colorScheme.error
        CellType.PATH -> MaterialTheme.colorScheme.secondary
        CellType.EXPLORED -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
    }
}

/**
 * Helper function to calculate face direction stripe rectangle
 */
private fun faceStripe(rect: Rect, face: FaceDir): Rect {
    val t = rect.width * 0.12f
    return when (face) {
        FaceDir.NORTH -> Rect(rect.left, rect.top, rect.right, rect.top + t)
        FaceDir.SOUTH -> Rect(rect.left, rect.bottom - t, rect.right, rect.bottom)
        FaceDir.WEST -> Rect(rect.left, rect.top, rect.left + t, rect.bottom)
        FaceDir.EAST -> Rect(rect.right - t, rect.top, rect.right, rect.bottom)
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
            onCellClick = { position ->
                // Preview doesn't need actual logic
            }
        )
    }
}
