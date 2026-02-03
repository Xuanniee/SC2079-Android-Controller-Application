package com.sc2079.androidcontroller.features.controller.domain.model

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir

/**
 * Represents the current status of the robot including its position on the map
 */
data class RobotStatus(
    // Position coordinates on the map
    val x: Int,
    val y: Int,
    // Current direction the robot is facing
    val faceDir: FaceDir,
    // Optional status message (e.g., "moving", "stopped", "turning")
    val statusMessage: String = "",
    // Whether the robot is currently moving
    val isMoving: Boolean = false
) {
    /**
     * Creates a RobotStatus with just position information
     */
    companion object {
        fun fromPosition(x: Int, y: Int, faceDir: FaceDir): RobotStatus {
            return RobotStatus(
                x = x,
                y = y,
                faceDir = faceDir,
                statusMessage = "",
                isMoving = false
            )
        }
    }
}
