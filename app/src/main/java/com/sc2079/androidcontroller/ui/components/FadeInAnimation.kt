package com.sc2079.androidcontroller.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import kotlinx.coroutines.delay

/**
 * Reusable fade-in animation wrapper component
 * 
 * @param durationMillis Duration of the fade-in animation in milliseconds (default: 1000ms)
 * @param delayMillis Delay before starting the animation in milliseconds (default: 0ms)
 * @param modifier Modifier to apply to the animated content
 * @param content The composable content to animate
 */
@Composable
fun FadeInAnimation(
    durationMillis: Int = 1000,
    delayMillis: Int = 0,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    // Trigger animation after delay
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        isVisible = true
    }
    
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = durationMillis),
        label = "fade_in"
    )
    
    Box(
        modifier = modifier.alpha(alpha)
    ) {
        content()
    }
}
