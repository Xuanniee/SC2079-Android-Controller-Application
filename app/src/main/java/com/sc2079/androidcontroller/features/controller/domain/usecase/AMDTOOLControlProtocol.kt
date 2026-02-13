package com.sc2079.androidcontroller.features.controller.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir

/**
 * Temporary protocol converter for AMD TOOL testing.
 * Converts movement commands from RobotControlBluetoothModule to simple direction strings.
 * 
 * Mapping:
 * - Up movement → "NORTH"
 * - Down movement → "SOUTH"
 * - Left movement → "EAST"
 * - Right movement → "WEST"
 */
object AMDTOOLControlProtocol {
    /**
     * Converts a movement direction to a simple direction string for AMD TOOL.
     * @param movementDirection The movement direction ("up", "down", "left", "right")
     * @return Simple direction string: "NORTH", "SOUTH", "EAST", or "WEST"
     */
    fun convertMovementToDirection(movementDirection: String): String {
        return when (movementDirection.lowercase().trim()) {
            "up" -> "NORTH"
            "down" -> "SOUTH"
            "left" -> "EAST"
            "right" -> "WEST"
            else -> ""
        }
    }

    /**
     * Converts a FaceDir enum to a simple direction string for AMD TOOL.
     * Note: This converts based on the direction the robot is facing after movement.
     * @param faceDir The direction the robot is facing
     * @return Simple direction string: "NORTH", "SOUTH", "EAST", or "WEST"
     */
    fun convertFaceDirToDirection(faceDir: FaceDir): String {
        return when (faceDir) {
            FaceDir.NORTH -> "NORTH"
            FaceDir.SOUTH -> "SOUTH"
            FaceDir.EAST -> "EAST"
            FaceDir.WEST -> "WEST"
        }
    }

    /**
     * Converts a movement command string from RobotControlBluetoothModule to a simple direction string.
     * Parses the command and extracts the movement direction.
     * 
     * Expected input formats:
     * - "STATUS, ROBOT MOVE, x, y, direction" → extracts direction and converts based on movement
     * - "MOVE, x, y, direction" → extracts direction and converts based on movement
     * 
     * @param commandString The full command string from RobotControlBluetoothModule
     * @param movementDirection The actual movement direction ("up", "down", "left", "right")
     * @return Simple direction string: "NORTH", "SOUTH", "EAST", or "WEST", or empty string if invalid
     */
    fun convertCommandToDirection(commandString: String, movementDirection: String): String {
        // Use the movement direction directly for conversion
        return convertMovementToDirection(movementDirection)
    }

    /**
     * Gets the direction string for a specific movement action.
     * This is the main function to use for converting movement commands.
     * 
     * @param movementAction The movement action: "up", "down", "left", or "right"
     * @return Simple direction string: "NORTH", "SOUTH", "EAST", or "WEST"
     */
    fun getDirectionString(movementAction: String): String {
        return convertMovementToDirection(movementAction)
    }
}
