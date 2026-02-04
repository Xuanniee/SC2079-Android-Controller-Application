package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot

/**
 * Updates the TargetID of an Obstacle in the Map
 */
class UpdateObstacleTargetIdUseCase {
    operator fun invoke(mapSnapshot: MapSnapshot, obstacleId: Int, displayedTargetId: Int?): MapSnapshot {
        // Find the index of the obstacle that needs to be updated
        val targetIdx = mapSnapshot.obstacles.indexOfFirst { it.obstacleId == obstacleId }
        if (targetIdx < 0) {
            // Cannot find the obstacle
            return mapSnapshot
        }

        // Create a copy of the obstqacle with the new int
        val updated = mapSnapshot.obstacles[targetIdx].copy(displayedTargetId = displayedTargetId)
        // Create a copy of the obstacle lists and update the obstacle with idx
        val newList = mapSnapshot.obstacles.toMutableList().also { it[targetIdx] = updated }
        // Retrun a copy of the map with the new obstacle
        return mapSnapshot.copy(obstacles = newList)
    }
}
