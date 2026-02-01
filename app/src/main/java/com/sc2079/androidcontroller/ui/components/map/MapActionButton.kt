package com.sc2079.androidcontroller.ui.components.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp

/**
 * Map Action Button - Rectangular button with icon and text
 */
@Composable
fun MapActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    text: String,
    contentDescription: String,
    backgroundColor: Color,
    contentColor: Color,
    hideText: Boolean = false,
    modifier: Modifier = Modifier
) {
    var showText by remember(hideText) { mutableStateOf(!hideText) }
    
    Box(
        modifier = modifier
            .height(64.dp) // Increased height to accommodate icon and text vertically
            .clip(RoundedCornerShape(16.dp)) // Rounded corners
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp),
                tint = contentColor
            )
            if (!hideText && showText) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor,
                    modifier = Modifier.padding(top = 4.dp),
                    onTextLayout = { textLayoutResult: TextLayoutResult ->
                        // Check if text overflows - if it has multiple lines or overflowed width
                        if (textLayoutResult.lineCount > 1 || textLayoutResult.didOverflowWidth) {
                            showText = false
                        }
                    },
                    maxLines = 1
                )
            }
        }
    }
}
