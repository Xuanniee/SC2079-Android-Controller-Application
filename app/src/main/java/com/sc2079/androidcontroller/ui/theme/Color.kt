package com.sc2079.androidcontroller.ui.theme

import androidx.compose.ui.graphics.Color

// Light Mode - Custom OKLCH colors
// Primary: White
val White = Color(0xFFFFFFFF)

// Secondary: oklch(92.9% 0.013 255.508) - Very light blue-gray
// Approximate RGB conversion: #EDEDF0
val LightSecondary = Color(0xFFf1f5f9)

// Tertiary: oklch(86.9% 0.022 252.894) - Light blue-gray
// Approximate RGB conversion: #DEDEE5
val LightTertiary = Color(0xFFe2e8f0)

// Text: oklch(27.9% 0.041 260.031) - Dark blue-gray
// Approximate RGB conversion: #3D3D4A
val DarkText = Color(0xFF3D3D4A)

// Error (Red): #ef4444
val CustomError = Color(0xFFef4444)

// Success (Green): oklch(79.2% 0.209 151.711)
// Approximate RGB conversion: #4CAF50
val CustomSuccess = Color(0xFF4CAF50)

// Warning (Orange): oklch(82.8% 0.189 84.429)
// Approximate RGB conversion: #FF9800
val CustomWarning = Color(0xFFFF9800)

// Legacy colors for backward compatibility
val SlateLight = Color(0xFF64748B)
val SlateMedium = Color(0xFF475569)
val SlateDark = Color(0xFF334155)
val SlateVeryDark = Color(0xFF1E293B)
val OffWhite = Color(0xFFF8FAFC)
val SlateGray100 = Color(0xFFF1F5F9)
val SlateGray200 = Color(0xFFE2E8F0)
val SlateGray300 = Color(0xFFCBD5E1)
val SlateGray400 = Color(0xFF94A3B8)

// Dark Mode Colors
val DarkBackground = Color(0xFF0F172A)
val DarkSurface = Color(0xFF1E293B)
val DarkSurfaceVariant = Color(0xFF334155)

// Dark Mode Primary: oklch(37.2% 0.044 257.287) - Dark blue-gray
// Approximate RGB conversion: #3D3F4A
val DarkPrimary = Color(0xFF3D3F4A)

// Dark Mode Secondary: oklch(55.4% 0.046 257.417) - Medium blue-gray
// Approximate RGB conversion: #6B6D7D
val DarkSecondary = Color(0xFF1e293b)

// Dark Mode Tertiary: oklch(70.4% 0.04 256.788) - Lighter blue-gray
// Approximate RGB conversion: #9A9CA8
val DarkTertiary = Color(0xFF9A9CA8)

// Contrast Mode Colors
val ContrastBlack = Color(0xFF000000)
val ContrastWhite = Color(0xFFFFFFFF)
val ContrastYellow = Color(0xFFFFFF00)
val ContrastCyan = Color(0xFF00FFFF)

// Accent colors
val AccentBlue = Color(0xFF3B82F6)

// Material3 color aliases - using custom OKLCH colors
val MaterialError = CustomError
val MaterialWarning = CustomWarning
val MaterialSuccess = CustomSuccess
