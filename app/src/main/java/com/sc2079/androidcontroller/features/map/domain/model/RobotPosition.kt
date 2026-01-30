package com.sc2079.androidcontroller.features.map.domain.model

/**
 * Holds the State of the Robot's current position
 */
data class RobotPosition(
    // Coordiantes
    val x: Int,
    val y: Int,
    // Current direction the robot is facing
    val faceDir: FaceDir
)
