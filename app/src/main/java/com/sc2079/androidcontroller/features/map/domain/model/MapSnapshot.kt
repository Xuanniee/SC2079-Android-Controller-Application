package com.sc2079.androidcontroller.features.map.domain.model

/**
 * Data Class to represent and hold the UiState of a saved Map that the user build
 */
data class MapSnapshot(
    // Represents the state of teh Robot in the Map. Robot might not be placed on the map, so nullable
    val robotPosition: RobotPosition?,
    // Represent the state of all the obstacles in the Map. Can be empty list of obstacles
    val obstacles: List<Obstacle>,
    // Indicates the next obstacleId to be used if user placed a new obstacle
    val nextObstacleId: Int
)
