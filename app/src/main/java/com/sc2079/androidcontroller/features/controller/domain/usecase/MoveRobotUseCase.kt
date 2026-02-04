package com.sc2079.androidcontroller.features.controller.domain.usecase

import com.sc2079.androidcontroller.features.controller.domain.model.RobotStatus
import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapConstants
import com.sc2079.androidcontroller.features.map.domain.model.DefaultMapConstants

/**
 * Use case for moving the robot to a new position on the map.
 * Contains business logic for validating and calculating robot movement.
 * 
 * Uses MapConstants interface to get map size dynamically, so bounds checking
 * automatically updates when map size changes.
 */
class MoveRobotUseCase(
    private val mapConstants: MapConstants = DefaultMapConstants
) {
    // Get map size from MapConstants interface
    private val MAP_SIZE: Int
        get() = mapConstants.mapSize

    /**
     * Moves the robot in a specified direction
     * @param currentStatus Current robot status with position
     * @param direction Direction to move ("forward", "backward", "left", "right")
     * @return Updated RobotStatus with new position, or same status if move is invalid
     */
    operator fun invoke(
        currentStatus: RobotStatus,
        direction: String
    ): RobotStatus {
        val (newX, newY) = calculateNewPosition(
            currentStatus.x,
            currentStatus.y,
            currentStatus.faceDir,
            direction
        )

        // Validate bounds
        if (!inBounds(newX, newY)) {
            return currentStatus.copy(
                statusMessage = "Cannot move: out of bounds",
                isMoving = false
            )
        }

        return currentStatus.copy(
            x = newX,
            y = newY,
            statusMessage = "Moved $direction",
            isMoving = true
        )
    }

    /**
     * Moves the robot in an absolute direction (up, down, left, right on the map)
     * @param currentStatus Current robot status
     * @param absoluteDirection Absolute direction ("up", "down", "left", "right")
     * @return Updated RobotStatus with new position
     */
    fun moveAbsolute(
        currentStatus: RobotStatus,
        absoluteDirection: String
    ): RobotStatus {
        val (newX, newY) = when (absoluteDirection.lowercase()) {
            "up" -> Pair(currentStatus.x, currentStatus.y - 1)
            "down" -> Pair(currentStatus.x, currentStatus.y + 1)
            "left" -> Pair(currentStatus.x - 1, currentStatus.y)
            "right" -> Pair(currentStatus.x + 1, currentStatus.y)
            else -> Pair(currentStatus.x, currentStatus.y)
        }
        
        // Validate bounds
        if (!inBounds(newX, newY)) {
            return currentStatus.copy(
                statusMessage = "Cannot move: out of bounds",
                isMoving = false
            )
        }
        
        // Update facing direction to match movement direction
        val newFaceDir = when (absoluteDirection.lowercase()) {
            "north" -> FaceDir.NORTH
            "south" -> FaceDir.SOUTH
            "west" -> FaceDir.WEST
            "east" -> FaceDir.EAST
            else -> currentStatus.faceDir
        }
        
        return currentStatus.copy(
            x = newX,
            y = newY,
            faceDir = newFaceDir,
            statusMessage = "Moved $absoluteDirection",
            isMoving = false // Control buttons don't set moving - only ROBOT messages do
        )
    }

    /**
     * Updates robot position directly to specific coordinates
     * @param currentStatus Current robot status
     * @param newX New X coordinate
     * @param newY New Y coordinate
     * @param newFaceDir New facing direction (optional, keeps current if null)
     * @return Updated RobotStatus with new position
     */
    operator fun invoke(
        currentStatus: RobotStatus,
        newX: Int,
        newY: Int,
        newFaceDir: FaceDir? = null
    ): RobotStatus {
        // Validate bounds
        if (!inBounds(newX, newY)) {
            return currentStatus.copy(
                statusMessage = "Cannot move: out of bounds",
                isMoving = false
            )
        }

        return currentStatus.copy(
            x = newX,
            y = newY,
            faceDir = newFaceDir ?: currentStatus.faceDir,
            statusMessage = "Position updated",
            isMoving = false
        )
    }

    /**
     * Calculates new position based on current position, facing direction, and movement direction
     */
    private fun calculateNewPosition(
        currentX: Int,
        currentY: Int,
        faceDir: FaceDir,
        direction: String
    ): Pair<Int, Int> {
        return when (direction.lowercase()) {
            "forward" -> {
                when (faceDir) {
                    FaceDir.NORTH -> Pair(currentX, currentY - 1)
                    FaceDir.SOUTH -> Pair(currentX, currentY + 1)
                    FaceDir.WEST -> Pair(currentX - 1, currentY)
                    FaceDir.EAST -> Pair(currentX + 1, currentY)
                }
            }
            "backward" -> {
                when (faceDir) {
                    FaceDir.NORTH -> Pair(currentX, currentY + 1)
                    FaceDir.SOUTH -> Pair(currentX, currentY - 1)
                    FaceDir.WEST -> Pair(currentX + 1, currentY)
                    FaceDir.EAST -> Pair(currentX - 1, currentY)
                }
            }
            "west" -> {
                when (faceDir) {
                    FaceDir.NORTH -> Pair(currentX - 1, currentY)
                    FaceDir.SOUTH -> Pair(currentX + 1, currentY)
                    FaceDir.WEST -> Pair(currentX, currentY + 1)
                    FaceDir.EAST -> Pair(currentX, currentY - 1)
                }
            }
            "east" -> {
                when (faceDir) {
                    FaceDir.NORTH -> Pair(currentX + 1, currentY)
                    FaceDir.SOUTH -> Pair(currentX - 1, currentY)
                    FaceDir.WEST -> Pair(currentX, currentY - 1)
                    FaceDir.EAST -> Pair(currentX, currentY + 1)
                }
            }
            else -> Pair(currentX, currentY) // Invalid direction, no movement
        }
    }

    /**
     * Helper function to check if coordinates are within map bounds
     * Valid coordinates are 0 to (MAP_SIZE - 1) for both x and y
     */
    private fun inBounds(x: Int, y: Int): Boolean {
        // Explicit bounds checking to ensure coordinates are within 0 to (MAP_SIZE - 1)
        if (x < 0 || x >= MAP_SIZE) return false
        if (y < 0 || y >= MAP_SIZE) return false
        return true
    }
}
