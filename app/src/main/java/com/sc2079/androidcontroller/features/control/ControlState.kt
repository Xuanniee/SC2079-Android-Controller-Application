package com.sc2079.androidcontroller.features.control

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Global control state holder for right-handed control preference
 */
object ControlState {
    var isRightHanded by mutableStateOf(true)
}
