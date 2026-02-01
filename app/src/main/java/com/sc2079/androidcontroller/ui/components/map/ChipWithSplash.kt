package com.sc2079.androidcontroller.ui.components.map

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sc2079.androidcontroller.R

/**
 * Chip with splash effect that triggers when a map box is tapped
 */
@Composable
fun ChipWithSplash(
    chipSplashTrigger: Int,
    modifier: Modifier = Modifier
) {
    // Animation state for splash effect
    var isAnimating by remember(chipSplashTrigger) { mutableStateOf(false) }
    
    // Trigger animation when chipSplashTrigger changes
    LaunchedEffect(chipSplashTrigger) {
        if (chipSplashTrigger > 0) {
            isAnimating = true
            kotlinx.coroutines.delay(200) // Animation duration
            isAnimating = false
        }
    }
    
    // Scale animation for splash effect - scales up then back down
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 0.97f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "chip_splash"
    )
    
    // Alpha animation for additional visual feedback
    val alpha by animateFloatAsState(
        targetValue = if (isAnimating) 0.7f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "chip_alpha"
    )
    
    AssistChip(
        onClick = { /* Non-dismissible */ },
        label = {
            Text(stringResource(R.string.double_tap_confirmation))
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = alpha),
            labelColor = MaterialTheme.colorScheme.error.copy(alpha = alpha)
        ),
        modifier = modifier
            .scale(scale)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error.copy(alpha = alpha),
                shape = RoundedCornerShape(8.dp)
            )
    )
}
