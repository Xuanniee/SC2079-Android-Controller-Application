package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot

/**
 * Moving an existing obstacle from 1 coordiante to another location.
 */
class MoveObstacleUseCase {
    operator fun invoke(mapSnapshot: MapSnapshot, obstacleId: Int, newX: Int, newY: Int): MapSnapshot {
        // Check OOB
        if (!inBounds(newX, newY)) {
            return mapSnapshot
        }
        // Identify the index of the target obstacle
        val targetIdx = mapSnapshot.obstacles.indexOfFirst { it.obstacleId == obstacleId }
        if (targetIdx < 0) {
            // Obstacle does not exist
            return mapSnapshot
        }
        // Ensure we do not move the obstacle to a location with an existing obstacle
        // Cannot collide with itself ofc
        if (mapSnapshot.obstacles.any { it.obstacleId != obstacleId && it.x == newX && it.y == newY }) {
            return mapSnapshot
        }

        // Create a new Obstacle with the update coordinates
        val updatedObstacle = mapSnapshot.obstacles[targetIdx].copy(x = newX, y = newY)
        // Replace it in the copied obstacles aray
        val newList = mapSnapshot.obstacles.toMutableList().also { it[targetIdx] = updatedObstacle }
        return mapSnapshot.copy(obstacles = newList)
    }

    // Helper function to check if the coordinates are within the bounds of the map
    private fun inBounds(x: Int, y: Int): Boolean {
        if (x < 0) {
            return false
        }
        if (x > 19) {
            return false
        }
        if (y < 0) {
            return false
        }
        if (y > 19) {
            return false
        }
        return true
    }
}
