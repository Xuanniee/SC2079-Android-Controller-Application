package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot
import com.sc2079.androidcontroller.features.map.domain.model.Obstacle

/**
 * Removes an existing obstacle from the Map
 */
class RemoveObstacleUseCase {
    operator fun invoke(mapSnapshot: MapSnapshot, obstacleId: Int): MapSnapshot {
        val updatedObstacles = mutableListOf<Obstacle>()

        for (obstacle in mapSnapshot.obstacles) {
            // Keep all obstacles except the one we want to remove
            if (obstacle.obstacleId != obstacleId) {
                updatedObstacles.add(obstacle)
            }
        }

        return mapSnapshot.copy(
            obstacles = updatedObstacles
        )
    }
}
