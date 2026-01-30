package com.sc2079.androidcontroller.features.map.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.ui.theme.SC2079AndroidControllerApplicationTheme

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
                    
                    GridCell(
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
 * Individual grid cell component with rounded corners
 */
@Composable
private fun GridCell(
    cellState: CellState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cornerRadius = 4.dp
    val shape = RoundedCornerShape(cornerRadius)
    
    val backgroundColor = when {
        cellState.isSelected -> MaterialTheme.colorScheme.primaryContainer
        else -> getCellColor(cellState.cellType)
    }
    
    val borderColor = when {
        cellState.isSelected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .background(backgroundColor, shape)
            .border(
                width = if (cellState.isSelected) 2.dp else 0.5.dp,
                color = borderColor,
                shape = shape
            )
            .clickable { onClick() }
    )
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
