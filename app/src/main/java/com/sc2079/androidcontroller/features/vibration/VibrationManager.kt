package com.sc2079.androidcontroller.features.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat

/**
 * Vibration Manager for providing haptic feedback
 */
class VibrationManager(
    private val context: Context
) {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ uses VibratorManager
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            // Older Android versions
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    /**
     * Check if vibration is supported on this device
     */
    fun hasVibrator(): Boolean {
        return vibrator?.hasVibrator() ?: false
    }

    /**
     * Vibrate with a default pattern for Bluetooth disconnection
     * Pattern: Short vibration, pause, short vibration, pause, long vibration
     */
    fun vibrateForDisconnection() {
        if (!hasVibrator()) return

        val pattern = longArrayOf(0, 200, 100, 200, 100, 400) // delays and durations in ms

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Android 8.0+ uses VibrationEffect
                val vibrationEffect = VibrationEffect.createWaveform(
                    pattern,
                    -1 // Don't repeat
                )
                vibrator?.vibrate(vibrationEffect)
            } else {
                // Older Android versions
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, -1)
            }
        } catch (e: Exception) {
            // Ignore vibration errors (e.g., permission issues)
        }
    }

    /**
     * Vibrate with a custom pattern
     * @param pattern Array of durations in milliseconds. First value is delay, then alternating on/off durations
     * @param repeat Index in pattern to repeat from, or -1 to not repeat
     */
    fun vibrate(pattern: LongArray, repeat: Int = -1) {
        if (!hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val vibrationEffect = VibrationEffect.createWaveform(pattern, repeat)
                vibrator?.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, repeat)
            }
        } catch (e: Exception) {
            // Ignore vibration errors
        }
    }

    /**
     * Cancel any ongoing vibration
     */
    fun cancel() {
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // Ignore cancellation errors
        }
    }
}
