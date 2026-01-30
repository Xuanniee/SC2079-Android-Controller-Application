package com.sc2079.androidcontroller.features.map.presentation

import com.sc2079.androidcontroller.features.map.domain.model.Obstacle

object RobotProtocol {

    fun clear(): String = "CLEAR"

    fun upsertObstacle(o: Obstacle): String {
        return "OBSTACLE,${o.obstacleId},${o.x * 10},${o.y * 10},${o.faceDir.name}"
    }

    fun removeObstacle(o: Obstacle): String {
        return "OBSTACLE,${o.obstacleId},${o.x * 10},${o.y * 10},-1"
    }
}
