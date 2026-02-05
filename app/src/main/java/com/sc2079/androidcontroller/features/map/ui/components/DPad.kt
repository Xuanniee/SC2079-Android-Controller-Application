package com.sc2079.androidcontroller.features.map.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.features.map.ui.util.Direction
import com.sc2079.androidcontroller.ui.components.home.DirectionButton

@Composable
fun DPad(
    onDirection: (Direction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DirectionButton(
            direction = Direction.NORTH,
            onClick = { onDirection(Direction.NORTH) }
        )

        Spacer(Modifier.height(5.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            DirectionButton(Direction.WEST, onClick = { onDirection(Direction.WEST) })
            Spacer(Modifier.size(44.dp)) // empty center (like screenshot)
            DirectionButton(Direction.EAST, onClick = { onDirection(Direction.EAST) })
        }

        Spacer(Modifier.height(5.dp))

        DirectionButton(
            direction = Direction.SOUTH,
            onClick = { onDirection(Direction.SOUTH) }
        )
    }
}