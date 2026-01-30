package com.sc2079.androidcontroller.features.map.domain.model

/**
 * Represents the state of the Obstacle on the map
 */
data class Obstacle(
    // Uniquely identifies the obstacle
    val obstacleId: Int,
    // Direction that holds the number
    val faceDir: FaceDir,
    // Target value on the obstacle
    val displayedTargetId: String?,
    // Coordinates
    val x: Int,
    val y: Int
)
