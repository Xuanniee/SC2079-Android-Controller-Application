package com.sc2079.androidcontroller.features.controller.domain.usecase

import com.sc2079.androidcontroller.features.bluetooth.domain.BluetoothConnState
import com.sc2079.androidcontroller.features.bluetooth.presentation.BluetoothViewModel
import com.sc2079.androidcontroller.features.controller.domain.model.RobotStatus
import com.sc2079.androidcontroller.features.controller.domain.usecase.MoveRobotUseCase
import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.RobotPosition
import com.sc2079.androidcontroller.features.map.presentation.RobotProtocol

/**
 * Module for handling robot control commands via Bluetooth.
 * Orchestrates robot movement logic and Bluetooth communication.
 * 
 * This module acts as a bridge between:
 * - Domain logic (MoveRobotUseCase) for movement calculations
 * - Infrastructure (BluetoothViewModel) for sending commands
 * - Protocol (RobotProtocol) for message formatting
 */
class RobotControlBluetoothModule(
    private val moveRobotUseCase: MoveRobotUseCase,
    private val bluetoothViewModel: BluetoothViewModel
) {
    // Track if robot was just rotated - reset after next up/down press
    private var wasJustRotated: Boolean = false
    /**
     * Moves robot forward in the direction it is facing and sends Bluetooth command if connected
     * @param currentStatus Current robot status
     * @return Updated RobotStatus after movement
     */
    fun moveForward(currentStatus: RobotStatus?): RobotStatus {
        val current = currentStatus ?: RobotStatus.fromPosition(0, 0, FaceDir.NORTH)
        val newStatus = moveRobotUseCase.moveForward(current)
        
        // Send Bluetooth message if position changed and connected
        sendMovementCommandIfNeeded(current, newStatus)
        
        return newStatus
    }
    
    /**
     * Moves robot backward (opposite to the direction it is facing) and sends Bluetooth command if connected
     * @param currentStatus Current robot status
     * @return Updated RobotStatus after movement
     */
    fun moveBackward(currentStatus: RobotStatus?): RobotStatus {
        val current = currentStatus ?: RobotStatus.fromPosition(0, 0, FaceDir.NORTH)
        val newStatus = moveRobotUseCase.moveBackward(current)
        
        // Send Bluetooth message if position changed and connected
        // For backward movement, send the opposite direction
        sendBackwardMovementCommandIfNeeded(current, newStatus)
        
        return newStatus
    }
    
    /**
     * Rotates robot 90 degrees left (counter-clockwise) and sends Bluetooth command if connected
     * @param currentStatus Current robot status
     * @return Updated RobotStatus after rotation
     */
    fun rotateLeft(currentStatus: RobotStatus?): RobotStatus {
        val current = currentStatus ?: RobotStatus.fromPosition(0, 0, FaceDir.NORTH)
        val newStatus = moveRobotUseCase.rotateLeft(current)
        
        // Set flag that robot was just rotated - reset after next up/down press
        wasJustRotated = true
        
        // Send Bluetooth message for rotation using AMD TOOL protocol (left → WEST)
        sendRotationCommandIfNeeded(newStatus, isLeftRotation = true)
        
        return newStatus
    }
    
    /**
     * Rotates robot 90 degrees right (clockwise) and sends Bluetooth command if connected
     * @param currentStatus Current robot status
     * @return Updated RobotStatus after rotation
     */
    fun rotateRight(currentStatus: RobotStatus?): RobotStatus {
        val current = currentStatus ?: RobotStatus.fromPosition(0, 0, FaceDir.NORTH)
        val newStatus = moveRobotUseCase.rotateRight(current)
        
        // Set flag that robot was just rotated - reset after next up/down press
        wasJustRotated = true
        
        // Send Bluetooth message for rotation using AMD TOOL protocol (right → EAST)
        sendRotationCommandIfNeeded(newStatus, isLeftRotation = false)
        
        return newStatus
    }
    
    /**
     * Moves robot up and sends Bluetooth command if connected
     * @param currentStatus Current robot status
     * @return Updated RobotStatus after movement
     */
    fun moveUp(currentStatus: RobotStatus?): RobotStatus {
        val current = currentStatus ?: RobotStatus.fromPosition(0, 0, FaceDir.NORTH)
        val newStatus = moveRobotUseCase.moveAbsolute(current, "up")
        
        // Send Bluetooth message if position changed and connected
        sendMovementCommandIfNeeded(current, newStatus)
        
        return newStatus
    }
    
    /**
     * Moves robot down and sends Bluetooth command if connected
     * @param currentStatus Current robot status
     * @return Updated RobotStatus after movement
     */
    fun moveDown(currentStatus: RobotStatus?): RobotStatus {
        val current = currentStatus ?: RobotStatus.fromPosition(0, 0, FaceDir.NORTH)
        val newStatus = moveRobotUseCase.moveAbsolute(current, "down")
        
        // Send Bluetooth message if position changed and connected
        sendMovementCommandIfNeeded(current, newStatus)
        
        return newStatus
    }
    
    /**
     * Moves robot left and sends Bluetooth command if connected
     * @param currentStatus Current robot status
     * @return Updated RobotStatus after movement
     */
    fun moveLeft(currentStatus: RobotStatus?): RobotStatus {
        val current = currentStatus ?: RobotStatus.fromPosition(0, 0, FaceDir.NORTH)
        val newStatus = moveRobotUseCase.moveAbsolute(current, "left")
        
        // Send Bluetooth message if position changed and connected
        sendMovementCommandIfNeeded(current, newStatus)
        
        return newStatus
    }
    
    /**
     * Moves robot right and sends Bluetooth command if connected
     * @param currentStatus Current robot status
     * @return Updated RobotStatus after movement
     */
    fun moveRight(currentStatus: RobotStatus?): RobotStatus {
        val current = currentStatus ?: RobotStatus.fromPosition(0, 0, FaceDir.NORTH)
        val newStatus = moveRobotUseCase.moveAbsolute(current, "right")
        
        // Send Bluetooth message if position changed and connected
        sendMovementCommandIfNeeded(current, newStatus)
        
        return newStatus
    }
    
    /**
     * Sends a custom string command via Bluetooth if connected
     * @param message The string message to send
     */
    fun sendCommand(message: String) {
        val bluetoothUiState = bluetoothViewModel.bluetoothUiState.value
        if (bluetoothUiState.bluetoothConnState is BluetoothConnState.Connected) {
            bluetoothViewModel.sendMessage(message)
        }
    }
    
    /**
     * Test command functions - Define your custom Bluetooth strings here
     */
    
    /**
     * Sends TEST_1 command with PWM percentage
     * @param pwmPercent PWM percentage (0-100)
     */
    fun test1(pwmPercent: Int = 50) {
        val message = "TEST_1, PWM, $pwmPercent"
        sendCommand(message)
    }
    
    /**
     * Example: Add more test commands here
     * fun test2(param: String) {
     *     val message = "TEST_2, $param"
     *     sendCommand(message)
     * }
     */
    
    /**
     * Checks if Bluetooth is connected
     * @return true if connected, false otherwise
     */
    fun isBluetoothConnected(): Boolean {
        val bluetoothUiState = bluetoothViewModel.bluetoothUiState.value
        return bluetoothUiState.bluetoothConnState is BluetoothConnState.Connected
    }
    
    /**
     * Sends ROBOT MOVE command via Bluetooth (same format as MappingHomeScreen)
     * Format: "STATUS, ROBOT MOVE, currX, currY, Direction"
     * @param currX Current X coordinate
     * @param currY Current Y coordinate
     * @param direction Direction the robot is facing
     */
    fun sendMoveCommand(currX: Int, currY: Int, direction: FaceDir) {
        if (isBluetoothConnected()) {
            // Use moveRobot() to match MappingHomeScreen format (STATUS, ROBOT MOVE, ...)
            val robotPosition = RobotPosition(currX, currY, direction)
            val message = RobotProtocol.moveRobot(robotPosition)
            // Only send if message is not empty (bounds check passed)
            if (message.isNotEmpty()) {
                sendCommand(message)
            }
        }
    }
    
    /**
     * Sends OBSTACLE placement command via Bluetooth
     * Format: "OBSTACLE1, currX, currY, faceDirection_OBSTACLE2, currX, currY, faceDirection"
     * @param obstacles List of obstacles, where each obstacle is:
     *                  Pair(obstacleId: Int, Triple(x: Int, y: Int, faceDir: FaceDir))
     *                  Example: listOf(Pair(1, Triple(5, 5, FaceDir.NORTH)), Pair(2, Triple(10, 10, FaceDir.SOUTH)))
     */
    fun sendObstaclePlacement(obstacles: List<Pair<Int, Triple<Int, Int, FaceDir>>>) {
        if (isBluetoothConnected() && obstacles.isNotEmpty()) {
            val message = RobotProtocol.obstaclePlacement(obstacles)
            sendCommand(message)
        }
    }
    
    /**
     * Helper function to send movement command via Bluetooth if conditions are met
     * Up arrow always sends "NORTH" regardless of robot's facing direction
     * @param oldStatus Previous robot status
     * @param newStatus New robot status after movement
     */
    private fun sendMovementCommandIfNeeded(oldStatus: RobotStatus, newStatus: RobotStatus) {
        // Only send if position actually changed and Bluetooth is connected
        val positionChanged = oldStatus.x != newStatus.x || oldStatus.y != newStatus.y
        
        if (positionChanged && isBluetoothConnected()) {
            // Up arrow always sends "NORTH" regardless of robot's facing direction
            sendCommand("NORTH")
            // Reset rotation flag after sending movement command
            wasJustRotated = false
        }
    }

    /**
     * Helper function to send backward movement command via Bluetooth if conditions are met
     * Down arrow always sends "SOUTH" regardless of robot's facing direction
     * @param oldStatus Previous robot status
     * @param newStatus New robot status after movement
     */
    private fun sendBackwardMovementCommandIfNeeded(oldStatus: RobotStatus, newStatus: RobotStatus) {
        // Only send if position actually changed and Bluetooth is connected
        val positionChanged = oldStatus.x != newStatus.x || oldStatus.y != newStatus.y
        
        if (positionChanged && isBluetoothConnected()) {
            // Down arrow always sends "SOUTH" regardless of robot's facing direction
            sendCommand("SOUTH")
            // Reset rotation flag after sending movement command
            wasJustRotated = false
        }
    }

    /**
     * Helper function to send rotation command via Bluetooth if conditions are met
     * Uses AMD TOOL protocol: left rotation sends "WEST", right rotation sends "EAST"
     * @param newStatus New robot status after rotation
     * @param isLeftRotation True if left rotation, false if right rotation
     */
    private fun sendRotationCommandIfNeeded(newStatus: RobotStatus, isLeftRotation: Boolean) {
        if (isBluetoothConnected()) {
            // Use AMD TOOL protocol for rotations
            val directionString = if (isLeftRotation) {
                "WEST"  // Left rotation → WEST
            } else {
                "EAST"  // Right rotation → EAST
            }
            sendCommand(directionString)
        }
    }
}
