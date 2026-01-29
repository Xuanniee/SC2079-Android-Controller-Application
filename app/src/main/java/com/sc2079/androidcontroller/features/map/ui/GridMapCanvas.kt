package com.sc2079.androidcontroller.features.map.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    var draggingObstacleNo by remember { mutableStateOf<Int?>(null) }

    val grid = 20
    val majorEvery = 5

    Canvas(
        modifier = modifier
            .pointerInput(uiState.editMode, uiState.obstacles) {
                detectTapGestures { pos ->
                    val side = min(size.width, size.height)
                    val offsetX = (size.width - side) / 2f
                    val offsetY = (size.height - side) / 2f

                    val cell = screenToCell(
                        pos = pos,
                        side = side,
                        offsetX = offsetX,
                        offsetY = offsetY,
                        grid = grid
                    ) ?: return@detectTapGestures

                    when (uiState.editMode) {
                        MapEditMode.ChangeObstacleFace -> {
                            val hit = uiState.obstacles.firstOrNull { it.x == cell.x && it.y == cell.y }
                            if (hit != null) onTapObstacleForFace(hit.no)
                        }
                        else -> onTapCell(cell.x, cell.y)
                    }
                }
            }
            .pointerInput(uiState.editMode, uiState.obstacles) {
                detectDragGestures(
                    onDragStart = { pos ->
                        if (uiState.editMode != MapEditMode.DragObstacle) return@detectDragGestures

                        val side = min(size.width, size.height)
                        val offsetX = (size.width - side) / 2f
                        val offsetY = (size.height - side) / 2f

                        val cell = screenToCell(
                            pos = pos,
                            side = side,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            grid = grid
                        ) ?: return@detectDragGestures

                        val hit = uiState.obstacles.firstOrNull { it.x == cell.x && it.y == cell.y }
                            ?: return@detectDragGestures

                        draggingObstacleNo = hit.no
                        onStartDragObstacle(hit.no)
                    },
                    onDrag = { change, _ ->
                        if (uiState.editMode != MapEditMode.DragObstacle) return@detectDragGestures
                        val no = draggingObstacleNo ?: return@detectDragGestures

                        val side = min(size.width, size.height)
                        val offsetX = (size.width - side) / 2f
                        val offsetY = (size.height - side) / 2f

                        val cell = screenToCell(
                            pos = change.position,
                            side = side,
                            offsetX = offsetX,
                            offsetY = offsetY,
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
    ) {
        // ---- Compute centered square grid region ----
        // Reserve label padding so numbers fit without clipping
        val usableW = size.width - labelPadPx
        val usableH = size.height - labelPadPx
        val side = min(usableW, usableH)

        // Grid starts after left label padding; top padding stays symmetric
        val offsetX = (size.width - side) / 2f + (labelPadPx / 2f)
        val offsetY = (size.height - side) / 2f

        val cellSize = side / grid.toFloat()

        val gridLeft = offsetX
        val gridTop = offsetY
        val gridRight = offsetX + side
        val gridBottom = offsetY + side

        // ---- Border ----
        drawRect(
            color = Color(0xFF6B6B6B),
            topLeft = Offset(gridLeft, gridTop),
            size = androidx.compose.ui.geometry.Size(side, side),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderStroke)
        )

        // ---- Grid lines ----
        for (i in 0..grid) {
            val p = i * cellSize
            val isMajor = (i % majorEvery == 0)

            val stroke = if (isMajor) majorStroke else minorStroke
            val col = if (isMajor) Color(0xFFB0B0B0) else Color(0xFFDADADA)

            // vertical
            drawLine(
                color = col,
                start = Offset(gridLeft + p, gridTop),
                end = Offset(gridLeft + p, gridBottom),
                strokeWidth = stroke
            )
            // horizontal
            drawLine(
                color = col,
                start = Offset(gridLeft, gridTop + p),
                end = Offset(gridRight, gridTop + p),
                strokeWidth = stroke
            )
        }

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
                val cx = gridLeft + (x + 0.5f) * cellSize
                drawText(x.toString(), cx, yText, paint)
            }

            // Y labels (0..19) along left
            // y=0 is bottom; y=19 is top
            paint.textAlign = Paint.Align.RIGHT
            val xText = gridLeft - axisTextSizePx * 0.6f
            for (y in 0 until grid) {
                val cy = gridTop + (grid - 1 - y + 0.5f) * cellSize
                drawText(y.toString(), xText, cy + axisTextSizePx * 0.35f, paint)
            }
        }

        // ---- Obstacles ----
        uiState.obstacles.forEach { obs ->
            val rect = cellRect(obs.x, obs.y, cellSize, grid, gridLeft, gridTop)

            drawRect(
                color = Color(0xFFB39DDB),
                topLeft = rect.topLeft,
                size = rect.size
            )

            val stripe = faceStripe(rect, obs.face)
            drawRect(
                color = Color(0xFF5E35B1),
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

                paint.color = android.graphics.Color.BLACK
                drawText("O${obs.no}", cx, cy - textSizePx * 0.1f, paint)

                obs.targetId?.let {
                    paint.color = android.graphics.Color.DKGRAY
                    drawText("T$it", cx, cy + textSizePx * 1.0f, paint)
                }
            }
        }

        // ---- Robot ----
        uiState.robotPose?.let { pose ->
            val rect = cellRect(pose.x, pose.y, cellSize, grid, gridLeft, gridTop)
            drawRect(
                color = Color(0xFF80CBC4),
                topLeft = rect.topLeft,
                size = rect.size
            )

            val center = rect.center
            val d = cellSize * 0.25f
            val marker = when (pose.dir) {
                FaceDir.UP -> Offset(center.x, center.y - d)
                FaceDir.DOWN -> Offset(center.x, center.y + d)
                FaceDir.LEFT -> Offset(center.x - d, center.y)
                FaceDir.RIGHT -> Offset(center.x + d, center.y)
            }

            drawCircle(
                color = Color(0xFF004D40),
                radius = cellSize * 0.08f,
                center = marker
            )
        }
    }
}

private data class Cell(val x: Int, val y: Int)

private fun screenToCell(
    pos: Offset,
    side: Int,
    offsetX: Float,
    offsetY: Float,
    grid: Int
): Cell? {
    val cell = side / grid.toFloat()

    val localX = pos.x - offsetX
    val localY = pos.y - offsetY

    if (localX < 0f || localY < 0f || localX >= side || localY >= side) return null

    val x = floor(localX / cell).toInt()
    val yFromTop = floor(localY / cell).toInt()
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
    cell: Float,
    grid: Int,
    gridLeft: Float,
    gridTop: Float
): Rect {
    val top = gridTop + (grid - 1 - y) * cell
    val left = gridLeft + x * cell
    return Rect(left, top, left + cell, top + cell)
}

private fun faceStripe(rect: Rect, face: FaceDir): Rect {
    val t = rect.width * 0.12f
    return when (face) {
        FaceDir.UP -> Rect(rect.left, rect.top, rect.right, rect.top + t)
        FaceDir.DOWN -> Rect(rect.left, rect.bottom - t, rect.right, rect.bottom)
        FaceDir.LEFT -> Rect(rect.left, rect.top, rect.left + t, rect.bottom)
        FaceDir.RIGHT -> Rect(rect.right - t, rect.top, rect.right, rect.bottom)
    }
}
