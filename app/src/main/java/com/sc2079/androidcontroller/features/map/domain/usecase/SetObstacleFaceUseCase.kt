package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot

/**
 * Update the Face of the Obstacle that holds the target number for the Robot to observe
 */
class SetObstacleFaceUseCase {
    operator fun invoke(mapSnapshot: MapSnapshot, obstacleId: Int, faceDir: FaceDir): MapSnapshot {
        // Retrieve the idx of the target obstacle
        val targetIdx = mapSnapshot.obstacles.indexOfFirst { it.obstacleId == obstacleId }
        if (targetIdx < 0) {
            // Failed to find the obstacle in the list
            return mapSnapshot
        }

        // Create an updated obstacle with a different face direction and set it in the array
        val updatedObstacle = mapSnapshot.obstacles[targetIdx].copy(faceDir = faceDir)
        val newList = mapSnapshot.obstacles.toMutableList().also { it[targetIdx] = updatedObstacle }
        return mapSnapshot.copy(obstacles = newList)
    }
}
