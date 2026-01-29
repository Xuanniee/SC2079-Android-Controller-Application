package com.sc2079.androidcontroller.features.map.domain.model

data class MapSnapshot(
    val robotPose: RobotPose?,
    val obstacles: List<Obstacle>,
    val nextObstacleNo: Int
)
