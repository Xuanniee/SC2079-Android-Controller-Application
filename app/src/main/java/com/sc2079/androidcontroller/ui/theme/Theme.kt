package com.sc2079.androidcontroller.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/**
 * Theme mode options
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    CONTRAST
}

/**
 * Global theme state holder
 */
object ThemeState {
    var currentTheme by mutableStateOf(ThemeMode.LIGHT)
}

// Light Mode Color Scheme - Custom OKLCH colors
// Primary: White, Secondary: oklch(92.9% 0.013 255.508), Tertiary: oklch(86.9% 0.022 252.894)
// Text: oklch(27.9% 0.041 260.031)
// Error: oklch(63.7% 0.237 25.331), Success: oklch(79.2% 0.209 151.711), Warning: oklch(82.8% 0.189 84.429)
private val LightColorScheme = lightColorScheme(
    primary = White,
    onPrimary = DarkText,
    primaryContainer = LightSecondary,
    onPrimaryContainer = DarkText,
    
    secondary = LightSecondary, // oklch(92.9% 0.013 255.508)
    onSecondary = DarkText,
    secondaryContainer = LightSecondary,
    onSecondaryContainer = DarkText,
    
    tertiary = LightTertiary, // oklch(86.9% 0.022 252.894)
    onTertiary = DarkText,
    tertiaryContainer = LightTertiary,
    onTertiaryContainer = DarkText,
    
    background = White,
    onBackground = DarkText,
    
    surface = White,
    onSurface = DarkText,
    surfaceVariant = LightTertiary,
    onSurfaceVariant = DarkText,
    
    outline = SlateGray300,
    outlineVariant = LightSecondary,
    
    error = CustomError, // Red: oklch(63.7% 0.237 25.331)
    onError = White,
    errorContainer = Color(0xFFFEE2E2),
    onErrorContainer = CustomError
)

// Dark Mode Color Scheme - Using OKLCH colors
// Primary: oklch(37.2% 0.044 257.287), Secondary: oklch(55.4% 0.046 257.417), Tertiary: oklch(70.4% 0.04 256.788)
// Error: oklch(63.7% 0.237 25.331), Success: oklch(79.2% 0.209 151.711), Warning: oklch(82.8% 0.189 84.429)
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary, // oklch(37.2% 0.044 257.287)
    onPrimary = SlateGray200,
    primaryContainer = DarkPrimary,
    onPrimaryContainer = SlateGray200,
    
    secondary = DarkSecondary, // oklch(55.4% 0.046 257.417)
    onSecondary = SlateGray200,
    secondaryContainer = DarkSecondary,
    onSecondaryContainer = SlateGray200,
    
    tertiary = DarkTertiary, // oklch(70.4% 0.04 256.788)
    onTertiary = DarkBackground,
    tertiaryContainer = DarkTertiary,
    onTertiaryContainer = SlateGray200,
    
    background = DarkBackground,
    onBackground = SlateGray200,
    
    surface = DarkSurface,
    onSurface = SlateGray200,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = SlateGray400,
    
    outline = SlateGray400,
    outlineVariant = DarkSurfaceVariant,
    
    error = CustomError, // Red: oklch(63.7% 0.237 25.331) - same OKLCH color
    onError = White,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFF6B6B) // Lighter version for dark mode
)

// High Contrast Mode Color Scheme
private val ContrastColorScheme = lightColorScheme(
    primary = ContrastBlack,
    onPrimary = ContrastWhite,
    primaryContainer = ContrastYellow,
    onPrimaryContainer = ContrastBlack,
    
    secondary = ContrastBlack,
    onSecondary = ContrastWhite,
    secondaryContainer = ContrastCyan,
    onSecondaryContainer = ContrastBlack,
    
    tertiary = ContrastBlack,
    onTertiary = ContrastWhite,
    tertiaryContainer = ContrastYellow,
    onTertiaryContainer = ContrastBlack,
    
    background = ContrastWhite,
    onBackground = ContrastBlack,
    
    surface = ContrastWhite,
    onSurface = ContrastBlack,
    surfaceVariant = ContrastYellow,
    onSurfaceVariant = ContrastBlack,
    
    outline = ContrastBlack,
    outlineVariant = ContrastBlack,
    
    error = CustomError, // Red: oklch(63.7% 0.237 25.331)
    onError = ContrastWhite,
    errorContainer = ContrastYellow,
    onErrorContainer = ContrastBlack
)

/**
 * Creates an animated ColorScheme that transitions between two schemes over 3 seconds
 */
@Composable
private fun animatedColorScheme(
    targetScheme: ColorScheme,
    durationMillis: Int = 3000
): ColorScheme {
    val animatedPrimary by animateColorAsState(
        targetValue = targetScheme.primary,
        animationSpec = tween(durationMillis = durationMillis),
        label = "primary"
    )
    val animatedOnPrimary by animateColorAsState(
        targetValue = targetScheme.onPrimary,
        animationSpec = tween(durationMillis = durationMillis),
        label = "onPrimary"
    )
    val animatedPrimaryContainer by animateColorAsState(
        targetValue = targetScheme.primaryContainer,
        animationSpec = tween(durationMillis = durationMillis),
        label = "primaryContainer"
    )
    val animatedOnPrimaryContainer by animateColorAsState(
        targetValue = targetScheme.onPrimaryContainer,
        animationSpec = tween(durationMillis = durationMillis),
        label = "onPrimaryContainer"
    )
    
    val animatedSecondary by animateColorAsState(
        targetValue = targetScheme.secondary,
        animationSpec = tween(durationMillis = durationMillis),
        label = "secondary"
    )
    val animatedOnSecondary by animateColorAsState(
        targetValue = targetScheme.onSecondary,
        animationSpec = tween(durationMillis = durationMillis),
        label = "onSecondary"
    )
    val animatedSecondaryContainer by animateColorAsState(
        targetValue = targetScheme.secondaryContainer,
        animationSpec = tween(durationMillis = durationMillis),
        label = "secondaryContainer"
    )
    val animatedOnSecondaryContainer by animateColorAsState(
        targetValue = targetScheme.onSecondaryContainer,
        animationSpec = tween(durationMillis = durationMillis),
        label = "onSecondaryContainer"
    )
    
    val animatedTertiary by animateColorAsState(
        targetValue = targetScheme.tertiary,
        animationSpec = tween(durationMillis = durationMillis),
        label = "tertiary"
    )
    val animatedOnTertiary by animateColorAsState(
        targetValue = targetScheme.onTertiary,
        animationSpec = tween(durationMillis = durationMillis),
        label = "onTertiary"
    )
    val animatedTertiaryContainer by animateColorAsState(
        targetValue = targetScheme.tertiaryContainer,
        animationSpec = tween(durationMillis = durationMillis),
        label = "tertiaryContainer"
    )
    val animatedOnTertiaryContainer by animateColorAsState(
        targetValue = targetScheme.onTertiaryContainer,
        animationSpec = tween(durationMillis = durationMillis),
        label = "onTertiaryContainer"
    )
    
    val animatedBackground by animateColorAsState(
        targetValue = targetScheme.background,
        animationSpec = tween(durationMillis = durationMillis),
        label = "background"
    )
    val animatedOnBackground by animateColorAsState(
        targetValue = targetScheme.onBackground,
        animationSpec = tween(durationMillis = durationMillis),
        label = "onBackground"
    )
    
    val animatedSurface by animateColorAsState(
        targetValue = targetScheme.surface,
        animationSpec = tween(durationMillis = durationMillis),
        label = "surface"
    )
    val animatedOnSurface by animateColorAsState(
        targetValue = targetScheme.onSurface,
        animationSpec = tween(durationMillis = durationMillis),
        label = "onSurface"
    )
    val animatedSurfaceVariant by animateColorAsState(
        targetValue = targetScheme.surfaceVariant,
        animationSpec = tween(durationMillis = durationMillis),
        label = "surfaceVariant"
    )
    val animatedOnSurfaceVariant by animateColorAsState(
        targetValue = targetScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = durationMillis),
        label = "onSurfaceVariant"
    )
    
    val animatedOutline by animateColorAsState(
        targetValue = targetScheme.outline,
        animationSpec = tween(durationMillis = durationMillis),
        label = "outline"
    )
    val animatedOutlineVariant by animateColorAsState(
        targetValue = targetScheme.outlineVariant,
        animationSpec = tween(durationMillis = durationMillis),
        label = "outlineVariant"
    )
    
    val animatedError by animateColorAsState(
        targetValue = targetScheme.error,
        animationSpec = tween(durationMillis = durationMillis),
        label = "error"
    )
    val animatedOnError by animateColorAsState(
        targetValue = targetScheme.onError,
        animationSpec = tween(durationMillis = durationMillis),
        label = "onError"
    )
    val animatedErrorContainer by animateColorAsState(
        targetValue = targetScheme.errorContainer,
        animationSpec = tween(durationMillis = durationMillis),
        label = "errorContainer"
    )
    val animatedOnErrorContainer by animateColorAsState(
        targetValue = targetScheme.onErrorContainer,
        animationSpec = tween(durationMillis = durationMillis),
        label = "onErrorContainer"
    )
    
    return targetScheme.copy(
        primary = animatedPrimary,
        onPrimary = animatedOnPrimary,
        primaryContainer = animatedPrimaryContainer,
        onPrimaryContainer = animatedOnPrimaryContainer,
        secondary = animatedSecondary,
        onSecondary = animatedOnSecondary,
        secondaryContainer = animatedSecondaryContainer,
        onSecondaryContainer = animatedOnSecondaryContainer,
        tertiary = animatedTertiary,
        onTertiary = animatedOnTertiary,
        tertiaryContainer = animatedTertiaryContainer,
        onTertiaryContainer = animatedOnTertiaryContainer,
        background = animatedBackground,
        onBackground = animatedOnBackground,
        surface = animatedSurface,
        onSurface = animatedOnSurface,
        surfaceVariant = animatedSurfaceVariant,
        onSurfaceVariant = animatedOnSurfaceVariant,
        outline = animatedOutline,
        outlineVariant = animatedOutlineVariant,
        error = animatedError,
        onError = animatedOnError,
        errorContainer = animatedErrorContainer,
        onErrorContainer = animatedOnErrorContainer
    )
}

@Composable
fun SC2079AndroidControllerApplicationTheme(
    themeMode: ThemeMode = ThemeState.currentTheme,
    content: @Composable () -> Unit
) {
    val targetColorScheme = when (themeMode) {
        ThemeMode.LIGHT -> LightColorScheme
        ThemeMode.DARK -> DarkColorScheme
        ThemeMode.CONTRAST -> ContrastColorScheme
    }
    
    // Animate color scheme transition over 3 seconds
    val animatedColorScheme = animatedColorScheme(
        targetScheme = targetColorScheme,
        durationMillis = 3000
    )

    MaterialTheme(
        colorScheme = animatedColorScheme,
        typography = Typography,
        content = content
    )
}
