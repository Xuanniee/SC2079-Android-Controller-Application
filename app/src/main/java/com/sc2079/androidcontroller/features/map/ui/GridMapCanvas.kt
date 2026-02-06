package com.sc2079.androidcontroller.features.map.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import com.sc2079.androidcontroller.features.map.presentation.MapUiState
import kotlin.math.floor
import kotlin.math.min

@Composable
fun GridMapCanvas(
    uiState: MapUiState,
    modifier: Modifier = Modifier,
    onTapCell: (x: Int, y: Int) -> Unit,
    onTapObstacleForFace: (obstacleNo: Int) -> Unit,
    onStartDragObstacle: (obstacleNo: Int) -> Unit,
    onDragObstacleToCell: (obstacleNo: Int, x: Int, y: Int) -> Unit,
    onDragOutsideRemove: (obstacleNo: Int) -> Unit,
    onEndDrag: () -> Unit
) {
    val density = LocalDensity.current
    val textSizePx = with(density) { 11.sp.toPx() }
    val axisTextSizePx = with(density) { 10.sp.toPx() }

    // Density-aware strokes
    val minorStroke = with(density) { 0.6.dp.toPx() }
    val majorStroke = with(density) { 1.2.dp.toPx() }
    val borderStroke = with(density) { 2.dp.toPx() }

    // Space reserved for labels (outside the grid)
    val labelPadPx = with(density) { 18.dp.toPx() }

    val grid = 20
    val majorEvery = 5

    var draggingObstacleNo by remember { mutableStateOf<Int?>(null) }

    // Always read latest state inside pointerInput (avoids stale captures)
    val latestObstacles by rememberUpdatedState(uiState.obstacles)
    val latestEditMode by rememberUpdatedState(uiState.editMode)

    LaunchedEffect(uiState.editMode) {
        android.util.Log.e("MAP_DEBUG", "editMode=${uiState.editMode}")
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(uiState.editMode) {
                when (uiState.editMode) {
                    MapEditMode.DragObstacle -> {
                        detectDragGestures(
                            onDragStart = { pos ->
                                // Use EXACT same geometry as drawing
                                val g = gridGeom(size.toSize(), labelPadPx, grid)

                                val cell = screenToCell(
                                    pos = pos,
                                    g = g,
                                    grid = grid
                                ) ?: return@detectDragGestures

                                android.util.Log.e(
                                    "MAP_DEBUG",
                                    "dragStart pos=$pos cell=$cell obstacles=${latestObstacles.map { it.x to it.y }}"
                                )

                                val hit = latestObstacles.firstOrNull { it.x == cell.x && it.y == cell.y }
                                    ?: return@detectDragGestures

                                draggingObstacleNo = hit.obstacleId
                                onStartDragObstacle(hit.obstacleId)
                            },
                            onDrag = { change, _ ->
                                // Consume so other recognizers don't interfere
                                change.consume()

                                if (latestEditMode != MapEditMode.DragObstacle) return@detectDragGestures
                                val no = draggingObstacleNo ?: return@detectDragGestures

                                val g = gridGeom(size.toSize(), labelPadPx, grid)

                                val cell = screenToCell(
                                    pos = change.position,
                                    g = g,
                                    grid = grid
                                )

                                if (cell == null) {
                                    onDragOutsideRemove(no)
                                    draggingObstacleNo = null
                                    return@detectDragGestures
                                }

                                onDragObstacleToCell(no, cell.x, cell.y)
                            },
                            onDragEnd = {
                                draggingObstacleNo = null
                                onEndDrag()
                            },
                            onDragCancel = {
                                draggingObstacleNo = null
                                onEndDrag()
                            }
                        )
                    }

                    else -> {
                        detectTapGestures { pos ->
                            val g = gridGeom(size.toSize(), labelPadPx, grid)

                            val cell = screenToCell(
                                pos = pos,
                                g = g,
                                grid = grid
                            ) ?: return@detectTapGestures

                            when (latestEditMode) {
                                MapEditMode.ChangeObstacleFace -> {
                                    val hit = latestObstacles.firstOrNull { it.x == cell.x && it.y == cell.y }
                                    if (hit != null) onTapObstacleForFace(hit.obstacleId)
                                }
                                else -> onTapCell(cell.x, cell.y)
                            }
                        }
                    }
                }
            }
    ) {
        // ---- Compute centered square grid region ----
        val g = gridGeom(size, labelPadPx, grid)

        val gridLeft = g.gridLeft
        val gridTop = g.gridTop
        val gridRight = gridLeft + g.gridWidth
        val gridBottom = gridTop + g.gridHeight

        val cellW = g.cellW
        val cellH = g.cellH

        // ---- Rounded clip path for the grid area ----
        val cornerRadius = 18.dp.toPx()

        val gridRoundRect = androidx.compose.ui.geometry.RoundRect(
            rect = Rect(gridLeft, gridTop, gridRight, gridBottom),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

        val gridClipPath = androidx.compose.ui.graphics.Path().apply {
            addRoundRect(gridRoundRect)
        }

        // ---- Draw grid clipped (so no square corners) ----
        clipPath(gridClipPath) {

            // ---- Grid lines ----
            for (i in 0..grid) {
                val isMajor = (i % majorEvery == 0)

                val stroke = if (isMajor) majorStroke else minorStroke
                val col = Color.Black

                // vertical
                drawLine(
                    color = col,
                    start = Offset(gridLeft + i * cellW, gridTop),
                    end = Offset(gridLeft + i * cellW, gridBottom),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )

                // horizontal
                drawLine(
                    color = col,
                    start = Offset(gridLeft, gridTop + i * cellH),
                    end = Offset(gridRight, gridTop + i * cellH),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round
                )
            }

            // ---- Obstacles ----
            uiState.obstacles.forEach { obs ->
                val rect = cellRect(obs.x, obs.y, g, grid)

                // Ensure Obstacle is Black
                drawRect(
                    color = Color.Black,
                    topLeft = rect.topLeft,
                    size = rect.size
                )

                // Strip color is Yellow
                val stripe = faceStripe(rect, obs.faceDir)
                drawRect(
                    color = Color.Yellow,
                    topLeft = stripe.topLeft,
                    size = stripe.size
                )

                drawContext.canvas.nativeCanvas.apply {
                    val paint = Paint().apply {
                        isAntiAlias = true
                        textAlign = Paint.Align.CENTER
                        textSize = textSizePx
                    }
                    val cx = rect.center.x
                    val cy = rect.center.y

                    // Number the obstacle only in White Paint
                    paint.color = android.graphics.Color.WHITE
                    drawText("${obs.obstacleId}", cx, cy - textSizePx * 0.1f, paint)

                    obs.displayedTargetId?.let {
                        paint.color = android.graphics.Color.DKGRAY
                        drawText("T$it", cx, cy + textSizePx * 1.0f, paint)
                    }
                }
            }

            // ---- Robot ----
            uiState.robotPosition?.let { pose ->
                val rect = cellRect(pose.x, pose.y, g, grid)
                drawRect(
                    color = Color(0xFF80CBC4),
                    topLeft = rect.topLeft,
                    size = rect.size
                )

                val center = rect.center
                val d = min(cellW, cellH) * 0.25f
                val marker = when (pose.faceDir) {
                    FaceDir.NORTH -> Offset(center.x, center.y - d)
                    FaceDir.SOUTH -> Offset(center.x, center.y + d)
                    FaceDir.WEST -> Offset(center.x - d, center.y)
                    FaceDir.EAST -> Offset(center.x + d, center.y)
                }

                drawCircle(
                    color = Color(0xFF004D40),
                    radius = min(cellW, cellH) * 0.08f,
                    center = marker
                )
            }
        }

        // ---- Border ----
        drawRoundRect(
            color = Color.Black,
            topLeft = Offset(gridLeft, gridTop),
            size = Size(g.gridWidth, g.gridHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderStroke)
        )

        // ---- Axis labels ----
        drawContext.canvas.nativeCanvas.apply {
            val paint = Paint().apply {
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
                textSize = axisTextSizePx
                color = android.graphics.Color.DKGRAY
            }

            // X labels (0..19) along bottom
            val yText = gridBottom + axisTextSizePx * 1.6f
            for (x in 0 until grid) {
                val cx = gridLeft + (x + 0.5f) * cellW
                drawText(x.toString(), cx, yText, paint)
            }

            // Y labels (0..19) along left
            // y=0 is bottom; y=19 is top
            paint.textAlign = Paint.Align.RIGHT
            val xText = gridLeft - axisTextSizePx * 0.6f
            for (y in 0 until grid) {
                val cy = gridTop + (grid - 1 - y + 0.5f) * cellH
                drawText(y.toString(), xText, cy + axisTextSizePx * 0.35f, paint)
            }
        }
    }
}

private data class Cell(val x: Int, val y: Int)

// OLD
//private data class GridGeom(
//    val side: Float,
//    val gridLeft: Float,
//    val gridTop: Float
//)
private data class GridGeom(
    val cellW: Float,
    val cellH: Float,
    val gridLeft: Float,
    val gridTop: Float,
    val gridWidth: Float,
    val gridHeight: Float
)

/**
 * IMPORTANT: This must match how the grid is drawn.
 * - side is computed from (width - labelPad) and (height - labelPad)
 * - gridLeft includes labelPad/2 shift
 */
private fun gridGeom(size: Size, labelPadPx: Float, grid: Int): GridGeom {
    val gridWidth = (size.width - labelPadPx).coerceAtLeast(1f)
    val gridHeight = (size.height - labelPadPx).coerceAtLeast(1f)

    val gridLeft = labelPadPx
    val gridTop = 0f

    return GridGeom(
        cellW = gridWidth / grid.toFloat(),
        cellH = gridHeight / grid.toFloat(),
        gridLeft = gridLeft,
        gridTop = gridTop,
        gridWidth = gridWidth,
        gridHeight = gridHeight
    )
}

// OLD
//private fun gridGeom(size: Size, labelPadPx: Float): GridGeom {
//    val usableW = size.width - labelPadPx
//    val usableH = size.height - labelPadPx
//    val side = min(usableW, usableH)
//
//
//    val gridLeft = (size.width - side) / 2f + (labelPadPx / 2f)
//    val gridTop = (size.height - side) / 2f
//
//    return GridGeom(side = side, gridLeft = gridLeft, gridTop = gridTop)
//}

private fun screenToCell(
    pos: Offset,
    g: GridGeom,
    grid: Int
): Cell? {
    val localX = pos.x - g.gridLeft
    val localY = pos.y - g.gridTop

    if (localX < 0f || localY < 0f || localX >= g.gridWidth || localY >= g.gridHeight) return null

    val x = floor(localX / g.cellW).toInt()
    val yFromTop = floor(localY / g.cellH).toInt()
    val y = (grid - 1) - yFromTop

    if (x !in 0 until grid || y !in 0 until grid) return null
    return Cell(x, y)
}

/**
 * gridLeft/gridTop are the TOP-LEFT origin of the drawn grid square.
 */
private fun cellRect(
    x: Int,
    y: Int,
    g: GridGeom,
    grid: Int
): Rect {
    val left = g.gridLeft + x * g.cellW
    val top = g.gridTop + (grid - 1 - y) * g.cellH

    return Rect(
        left,
        top,
        left + g.cellW,
        top + g.cellH
    )
}

// OLD
//private fun cellRect(
//    x: Int,
//    y: Int,
//    cell: Float,
//    grid: Int,
//    gridLeft: Float,
//    gridTop: Float
//): Rect {
//    val top = gridTop + (grid - 1 - y) * cell
//    val left = gridLeft + x * cell
//    return Rect(left, top, left + cell, top + cell)
//}

private fun faceStripe(rect: Rect, face: FaceDir): Rect {
    val t = rect.width * 0.12f
    return when (face) {
        FaceDir.NORTH -> Rect(rect.left, rect.top, rect.right, rect.top + t)
        FaceDir.SOUTH -> Rect(rect.left, rect.bottom - t, rect.right, rect.bottom)
        FaceDir.WEST -> Rect(rect.left, rect.top, rect.left + t, rect.bottom)
        FaceDir.EAST -> Rect(rect.right - t, rect.top, rect.right, rect.bottom)
    }
}


//package com.sc2079.androidcontroller.features.map.ui
//
//import android.graphics.Paint
//import androidx.compose.foundation.BorderStroke
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.border
//import androidx.compose.foundation.gestures.detectDragGestures
//import androidx.compose.foundation.gestures.detectTapGestures
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.runtime.*
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clip
//import androidx.compose.ui.geometry.CornerRadius
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Rect
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.StrokeCap
//import androidx.compose.ui.graphics.drawscope.clipPath
//import androidx.compose.ui.graphics.nativeCanvas
//import androidx.compose.ui.input.pointer.pointerInput
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.compose.ui.unit.toSize
//import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
//import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
//import com.sc2079.androidcontroller.features.map.presentation.MapUiState
//import kotlin.math.floor
//import kotlin.math.min
//
//@Composable
//fun GridMapCanvas(
//    uiState: MapUiState,
//    modifier: Modifier = Modifier,
//    onTapCell: (x: Int, y: Int) -> Unit,
//    onTapObstacleForFace: (obstacleNo: Int) -> Unit,
//    onStartDragObstacle: (obstacleNo: Int) -> Unit,
//    onDragObstacleToCell: (obstacleNo: Int, x: Int, y: Int) -> Unit,
//    onDragOutsideRemove: (obstacleNo: Int) -> Unit,
//    onEndDrag: () -> Unit
//) {
//    val density = LocalDensity.current
//    val textSizePx = with(density) { 11.sp.toPx() }
//    val axisTextSizePx = with(density) { 10.sp.toPx() }
//
//    // Density-aware strokes
//    val minorStroke = with(density) { 0.6.dp.toPx() }
//    val majorStroke = with(density) { 1.2.dp.toPx() }
//    val borderStroke = with(density) { 2.dp.toPx() }
//
//    // Space reserved for labels (outside the grid)
//    val labelPadPx = with(density) { 18.dp.toPx() }
//
//    val grid = 20
//    val majorEvery = 5
//
//    var draggingObstacleNo by remember { mutableStateOf<Int?>(null) }
//
//    // Always read latest state inside pointerInput (avoids stale captures)
//    val latestObstacles by rememberUpdatedState(uiState.obstacles)
//    val latestEditMode by rememberUpdatedState(uiState.editMode)
//
//    LaunchedEffect(uiState.editMode) {
//        android.util.Log.e("MAP_DEBUG", "editMode=${uiState.editMode}")
//    }
//
//    Canvas(
//        modifier = modifier
//            .fillMaxWidth()
//            .pointerInput(uiState.editMode) {
//                when (uiState.editMode) {
//                    MapEditMode.DragObstacle -> {
//                        detectDragGestures(
//                            onDragStart = { pos ->
//                                // Use EXACT same geometry as drawing
//                                val g = gridGeom(size.toSize(), labelPadPx)
//
//                                val cell = screenToCell(
//                                    pos = pos,
//                                    side = g.side.toInt(),
//                                    offsetX = g.gridLeft,
//                                    offsetY = g.gridTop,
//                                    grid = grid
//                                ) ?: return@detectDragGestures
//
//                                android.util.Log.e("MAP_DEBUG", "dragStart pos=$pos cell=$cell obstacles=${latestObstacles.map{it.x to it.y}}")
//
//                                val hit = latestObstacles.firstOrNull { it.x == cell.x && it.y == cell.y }
//                                    ?: return@detectDragGestures
//
//                                draggingObstacleNo = hit.obstacleId
//                                onStartDragObstacle(hit.obstacleId)
//                            },
//                            onDrag = { change, _ ->
//                                // Consume so other recognizers don't interfere
//                                change.consume()
//
//                                if (latestEditMode != MapEditMode.DragObstacle) return@detectDragGestures
//                                val no = draggingObstacleNo ?: return@detectDragGestures
//
//                                val g = gridGeom(size.toSize(), labelPadPx)
//
//                                val cell = screenToCell(
//                                    pos = change.position,
//                                    side = g.side.toInt(),
//                                    offsetX = g.gridLeft,
//                                    offsetY = g.gridTop,
//                                    grid = grid
//                                )
//
//                                if (cell == null) {
//                                    onDragOutsideRemove(no)
//                                    draggingObstacleNo = null
//                                    return@detectDragGestures
//                                }
//
//                                onDragObstacleToCell(no, cell.x, cell.y)
//                            },
//                            onDragEnd = {
//                                draggingObstacleNo = null
//                                onEndDrag()
//                            },
//                            onDragCancel = {
//                                draggingObstacleNo = null
//                                onEndDrag()
//                            }
//                        )
//                    }
//
//                    else -> {
//                        detectTapGestures { pos ->
//                            val g = gridGeom(size.toSize(), labelPadPx)
//
//                            val cell = screenToCell(
//                                pos = pos,
//                                side = g.side.toInt(),
//                                offsetX = g.gridLeft,
//                                offsetY = g.gridTop,
//                                grid = grid
//                            ) ?: return@detectTapGestures
//
//                            when (latestEditMode) {
//                                MapEditMode.ChangeObstacleFace -> {
//                                    val hit = latestObstacles.firstOrNull { it.x == cell.x && it.y == cell.y }
//                                    if (hit != null) onTapObstacleForFace(hit.obstacleId)
//                                }
//                                else -> onTapCell(cell.x, cell.y)
//                            }
//                        }
//                    }
//                }
//            }
//    ) {
//        // ---- Compute centered square grid region ----
//        val g = gridGeom(size, labelPadPx)
//        val side = g.side
//        val gridLeft = g.gridLeft
//        val gridTop = g.gridTop
//        val gridRight = gridLeft + side
//        val gridBottom = gridTop + side
//
//        val cellSize = side / grid.toFloat()
//
//        // ---- Rounded clip path for the grid area ----
//        val cornerRadius = 18.dp.toPx()
//
//        val gridRoundRect = androidx.compose.ui.geometry.RoundRect(
//            rect = Rect(gridLeft, gridTop, gridRight, gridBottom),
//            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
//        )
//
//        val gridClipPath = androidx.compose.ui.graphics.Path().apply {
//            addRoundRect(gridRoundRect)
//        }
//
//        // ---- Draw grid clipped (so no square corners) ----
//        clipPath(gridClipPath) {
//
//            // ---- Grid lines ----
//            for (i in 0..grid) {
//                val p = i * cellSize
//                val isMajor = (i % majorEvery == 0)
//
//                val stroke = if (isMajor) majorStroke else minorStroke
//                val col = Color.Black
//
//                // vertical
//                drawLine(
//                    color = col,
//                    start = Offset(gridLeft + p, gridTop),
//                    end = Offset(gridLeft + p, gridBottom),
//                    strokeWidth = stroke,
//                    cap = StrokeCap.Round
//                )
//
//                // horizontal
//                drawLine(
//                    color = col,
//                    start = Offset(gridLeft, gridTop + p),
//                    end = Offset(gridRight, gridTop + p),
//                    strokeWidth = stroke,
//                    cap = StrokeCap.Round
//                )
//            }
//
//            // ---- Obstacles ----
//            uiState.obstacles.forEach { obs ->
//                val rect = cellRect(obs.x, obs.y, cellSize, grid, gridLeft, gridTop)
//
//                // Ensure Obstacle is Black
//                drawRect(
//                    color = Color.Black,
//                    topLeft = rect.topLeft,
//                    size = rect.size
//                )
//
//                // Strip color is Yellow
//                val stripe = faceStripe(rect, obs.faceDir)
//                drawRect(
//                    color = Color.Yellow,
//                    topLeft = stripe.topLeft,
//                    size = stripe.size
//                )
//
//                drawContext.canvas.nativeCanvas.apply {
//                    val paint = Paint().apply {
//                        isAntiAlias = true
//                        textAlign = Paint.Align.CENTER
//                        textSize = textSizePx
//                    }
//                    val cx = rect.center.x
//                    val cy = rect.center.y
//
//                    // Number the obstacle only in White Paint
//                    paint.color = android.graphics.Color.WHITE
//                    drawText("${obs.obstacleId}", cx, cy - textSizePx * 0.1f, paint)
//
//                    obs.displayedTargetId?.let {
//                        paint.color = android.graphics.Color.DKGRAY
//                        drawText("T$it", cx, cy + textSizePx * 1.0f, paint)
//                    }
//                }
//            }
//
//            // ---- Robot ----
//            uiState.robotPosition?.let { pose ->
//                val rect = cellRect(pose.x, pose.y, cellSize, grid, gridLeft, gridTop)
//                drawRect(
//                    color = Color(0xFF80CBC4),
//                    topLeft = rect.topLeft,
//                    size = rect.size
//                )
//
//                val center = rect.center
//                val d = cellSize * 0.25f
//                val marker = when (pose.faceDir) {
//                    FaceDir.NORTH -> Offset(center.x, center.y - d)
//                    FaceDir.SOUTH -> Offset(center.x, center.y + d)
//                    FaceDir.WEST -> Offset(center.x - d, center.y)
//                    FaceDir.EAST -> Offset(center.x + d, center.y)
//                }
//
//                drawCircle(
//                    color = Color(0xFF004D40),
//                    radius = cellSize * 0.08f,
//                    center = marker
//                )
//            }
//        }
//
//        // ---- Border ----
//        drawRoundRect(
//            color = Color.Black,
//            topLeft = Offset(gridLeft, gridTop),
//            size = Size(side, side),
//            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
//            style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderStroke)
//        )
//
//        // ---- Axis labels ----
//        drawContext.canvas.nativeCanvas.apply {
//            val paint = Paint().apply {
//                isAntiAlias = true
//                textAlign = Paint.Align.CENTER
//                textSize = axisTextSizePx
//                color = android.graphics.Color.DKGRAY
//            }
//
//            // X labels (0..19) along bottom
//            val yText = gridBottom + axisTextSizePx * 1.6f
//            for (x in 0 until grid) {
//                val cx = gridLeft + (x + 0.5f) * cellSize
//                drawText(x.toString(), cx, yText, paint)
//            }
//
//            // Y labels (0..19) along left
//            // y=0 is bottom; y=19 is top
//            paint.textAlign = Paint.Align.RIGHT
//            val xText = gridLeft - axisTextSizePx * 0.6f
//            for (y in 0 until grid) {
//                val cy = gridTop + (grid - 1 - y + 0.5f) * cellSize
//                drawText(y.toString(), xText, cy + axisTextSizePx * 0.35f, paint)
//            }
//        }
//    }
//}
//
//private data class Cell(val x: Int, val y: Int)
//
//private data class GridGeom(
//    val side: Float,
//    val gridLeft: Float,
//    val gridTop: Float
//)
//
///**
// * IMPORTANT: This must match how the grid is drawn.
// * - side is computed from (width - labelPad) and (height - labelPad)
// * - gridLeft includes labelPad/2 shift
// */
//private fun gridGeom(size: Size, labelPadPx: Float): GridGeom {
//    val usableW = size.width - labelPadPx
//    val usableH = size.height - labelPadPx
//    val side = min(usableW, usableH)
//
//    val gridLeft = (size.width - side) / 2f + (labelPadPx / 2f)
//    val gridTop = (size.height - side) / 2f
//
//    return GridGeom(side = side, gridLeft = gridLeft, gridTop = gridTop)
//}
//
//private fun screenToCell(
//    pos: Offset,
//    side: Int,
//    offsetX: Float,
//    offsetY: Float,
//    grid: Int
//): Cell? {
//    val cell = side / grid.toFloat()
//
//    val localX = pos.x - offsetX
//    val localY = pos.y - offsetY
//
//    if (localX < 0f || localY < 0f || localX >= side || localY >= side) return null
//
//    val x = floor(localX / cell).toInt()
//    val yFromTop = floor(localY / cell).toInt()
//    val y = (grid - 1) - yFromTop
//
//    if (x !in 0 until grid || y !in 0 until grid) return null
//    return Cell(x, y)
//}
//
///**
// * gridLeft/gridTop are the TOP-LEFT origin of the drawn grid square.
// */
//private fun cellRect(
//    x: Int,
//    y: Int,
//    cell: Float,
//    grid: Int,
//    gridLeft: Float,
//    gridTop: Float
//): Rect {
//    val top = gridTop + (grid - 1 - y) * cell
//    val left = gridLeft + x * cell
//    return Rect(left, top, left + cell, top + cell)
//}
//
//private fun faceStripe(rect: Rect, face: FaceDir): Rect {
//    val t = rect.width * 0.12f
//    return when (face) {
//        FaceDir.NORTH -> Rect(rect.left, rect.top, rect.right, rect.top + t)
//        FaceDir.SOUTH -> Rect(rect.left, rect.bottom - t, rect.right, rect.bottom)
//        FaceDir.WEST -> Rect(rect.left, rect.top, rect.left + t, rect.bottom)
//        FaceDir.EAST -> Rect(rect.right - t, rect.top, rect.right, rect.bottom)
//    }
//}