package com.sc2079.androidcontroller.features.map.domain.model

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir

data class SavedMap(
    val name: String,
    val obstacles: List<SavedObstacle>,
    val robot: SavedRobotPosition? = null,
    val retryEnabled: Boolean = false
)

data class SavedObstacle(
    val obstacleId: Int,
    val x: Int,
    val y: Int,
    val faceDir: FaceDir
)

data class SavedRobotPosition(
    val x: Int,
    val y: Int,
    val faceDir: FaceDir
)