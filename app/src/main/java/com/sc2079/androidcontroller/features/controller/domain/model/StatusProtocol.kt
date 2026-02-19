package com.sc2079.androidcontroller.features.controller.domain.model

/**
 * Protocol for parsing status messages received from the robot via Bluetooth.
 * Maps incoming Bluetooth strings to BluetoothStatus states.
 *
 * Supported status message formats:
 * - "ROBOT,scanning" → BluetoothStatus.SCANNING
 * - "ROBOT,connected" → BluetoothStatus.CONNECTED
 * - "ROBOT,stopped" → BluetoothStatus.STOPPED
 * - "ROBOT,<x>,<y>,<direction>" → BluetoothStatus.MOVING (when parsed as position update)
 */
object StatusProtocol {
    /**
     * Checks if a message indicates the robot is scanning
     * @param message Raw Bluetooth message string
     * @return true if message matches "ROBOT,scanning" pattern (case insensitive)
     */
    fun isScanningMessage(message: String): Boolean {
        val messageUpper = message.uppercase().trim()
        return messageUpper.matches(Regex("ROBOT\\s*,\\s*SCANNING"))
    }

    /**
     * Checks if a message indicates the robot is connected
     * @param message Raw Bluetooth message string
     * @return true if message matches "ROBOT,connected" pattern (case insensitive)
     */
    fun isConnectedMessage(message: String): Boolean {
        val messageUpper = message.uppercase().trim()
        return messageUpper.matches(Regex("ROBOT\\s*,\\s*CONNECTED"))
    }

    /**
     * Checks if a message indicates the robot has stopped
     * @param message Raw Bluetooth message string
     * @return true if message matches "ROBOT,stopped" pattern (case insensitive)
     */
    fun isStoppedMessage(message: String): Boolean {
        val messageUpper = message.uppercase().trim()
        return messageUpper.matches(Regex("ROBOT\\s*,\\s*STOPPED"))
    }

    /**
     * Checks if a message is a robot position update (indicates robot is moving)
     * Format: "ROBOT,<x>,<y>,<direction>"
     * @param message Raw Bluetooth message string
     * @return true if message matches robot position pattern
     */
    fun isMovingMessage(message: String): Boolean {
        val messageUpper = message.uppercase().trim()
        // Check if it starts with "ROBOT," and has 4 comma-separated parts
        if (!messageUpper.startsWith("ROBOT,")) {
            return false
        }
        val parts = messageUpper.split(",").map { it.trim() }
        // Should have: ROBOT, x, y, direction (4 parts)
        if (parts.size != 4) {
            return false
        }
        // Check if x and y are valid integers
        val x = parts[1].toIntOrNull()
        val y = parts[2].toIntOrNull()
        return x != null && y != null
    }

    /**
     * Parses a Bluetooth message and returns the corresponding BluetoothStatus if applicable.
     * This is the main function to use for parsing Bluetooth messages and getting BluetoothStatus.
     *
     * Priority order:
     * 1. SCANNING - if message is "ROBOT,scanning"
     * 2. CONNECTED - if message is "ROBOT,connected"
     * 3. STOPPED - if message is "ROBOT,stopped"
     * 4. MOVING - if message is "ROBOT,x,y,direction" (position update)
     * 5. null - if message doesn't match any known status pattern
     *
     * @param message Raw Bluetooth message string received from robot
     * @return BluetoothStatus if message matches a status pattern, null otherwise
     */
    fun parseStatusMessage(message: String): BluetoothStatus? {
        return when {
            isScanningMessage(message) -> BluetoothStatus.SCANNING
            isConnectedMessage(message) -> BluetoothStatus.CONNECTED
            isStoppedMessage(message) -> BluetoothStatus.STOPPED
            isMovingMessage(message) -> BluetoothStatus.MOVING
            else -> null
        }
    }

    /**
     * Parses a Bluetooth message and returns both BluetoothStatus and whether it's a position update.
     * This is useful when you need to know if the message contains position data.
     *
     * @param message Raw Bluetooth message string received from robot
     * @return Pair of (BluetoothStatus?, isPositionUpdate: Boolean)
     *         - BluetoothStatus is null if message doesn't match any status pattern
     *         - isPositionUpdate is true if message contains robot position data
     */
    fun parseStatusWithPosition(message: String): Pair<BluetoothStatus?, Boolean> {
        val status = parseStatusMessage(message)
        val isPositionUpdate = isMovingMessage(message)
        return Pair(status, isPositionUpdate)
    }
}
