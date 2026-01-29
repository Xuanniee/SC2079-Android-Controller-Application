package com.sc2079.androidcontroller.features.map.presentation

import com.sc2079.androidcontroller.features.map.domain.model.Obstacle

object RobotProtocol {
    fun clear(): String = "CLEAR"

    fun upsertObstacle(o: Obstacle): String {
        // Java: (col-1)*10 and (19-row)*10 -> our x,y are already bottom-left => x*10,y*10
        return "OBSTACLE,${o.no},${o.x * 10},${o.y * 10},${o.face.name}"
    }

    fun removeObstacle(o: Obstacle): String {
        // Java removal used bearing -1
        return "OBSTACLE,${o.no},${o.x * 10},${o.y * 10},-1"
    }
}
