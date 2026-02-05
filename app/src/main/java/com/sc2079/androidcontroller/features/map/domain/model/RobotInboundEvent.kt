package com.sc2079.androidcontroller.features.map.domain.model

// Represents all possible message types that the Robot can send
sealed class RobotInboundEvent {
    /**
     * Robot's current position (position + facing direction) in map coordinates used by the app.
     */
    data class RobotPositionEvent(val x: Int, val y: Int, val faceDir: FaceDir) : RobotInboundEvent()
    /**
     * A "target" result message from the robot.
     *
     * obstacleNo: which obstacle index/number the target refers to
     * targetId: some identifier for the detected target (e.g., image ID / label)
     */
    data class TargetEvent(val obstacleId: Int, val targetId: Int?) : RobotInboundEvent()
}