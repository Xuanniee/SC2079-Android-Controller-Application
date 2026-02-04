package com.sc2079.androidcontroller.features.map.domain.model

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir

sealed class ProtocolMessage {
    data object Clear : ProtocolMessage()

    // Obstacles
    data class ObstaclePlacement(
        val obstacleId: Int,
        val x: Int,
        val y: Int,
        val face: FaceDir
    ) : ProtocolMessage()

    data class ObstacleDeletion(
        val obstacleId: Int
    ) : ProtocolMessage()

    data class ObstacleOrientation(
        val obstacleId: Int,
        val x: Int,
        val y: Int,
        val oldFace: FaceDir,
        val newFace: FaceDir
    ) : ProtocolMessage()

    data class ObstacleList(
        val obstacles: List<ObstacleItem>
    ) : ProtocolMessage() {
        data class ObstacleItem(
            val obstacleId: Int,
            val x: Int,
            val y: Int,
            val face: FaceDir
        )
    }

    // Robot
    data class RobotPlacement(
        val x: Int,
        val y: Int,
        val face: FaceDir
    ) : ProtocolMessage()

    data class RobotMove(
        val x: Int,
        val y: Int,
        val face: FaceDir
    ) : ProtocolMessage()

    data class RobotOrientation(
        val x: Int,
        val y: Int,
        val oldFace: FaceDir,
        val newFace: FaceDir
    ) : ProtocolMessage()
}
