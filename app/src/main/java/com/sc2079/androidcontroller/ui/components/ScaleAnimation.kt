package com.sc2079.androidcontroller.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.delay

/**
 * Reusable scale animation wrapper component for scale-in and scale-out animations
 *
 * @param visible Whether the content should be visible (true = scale in, false = scale out)
 * @param durationMillis Duration of the animation in milliseconds (default: 300ms)
 * @param delayMillis Delay before starting the animation in milliseconds (default: 0ms)
 * @param modifier Modifier to apply to the animated content
 * @param content The composable content to animate
 */
@Composable
fun ScaleAnimation(
    visible: Boolean,
    durationMillis: Int = 300,
    delayMillis: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isAnimating by remember(visible) { mutableStateOf(!visible) }
    
    // Trigger animation when visibility changes
    LaunchedEffect(visible) {
        if (visible) {
            // Scale in: start from 0, animate to 1
            isAnimating = true // Start scaled out
            delay(delayMillis.toLong())
            isAnimating = false // Animate to scaled in
        } else {
            // Scale out: animate from 1 to 0
            isAnimating = true
        }
    }
    
    val scale by animateFloatAsState(
        targetValue = if (isAnimating) 0f else 1f,
        animationSpec = tween(durationMillis = durationMillis),
        label = "scale_animation"
    )
    
    Box(
        modifier = modifier.scale(scale)
    ) {
        if (scale > 0f) { // Only render content when scale > 0 to avoid rendering invisible content
            content()
        }
    }
}

