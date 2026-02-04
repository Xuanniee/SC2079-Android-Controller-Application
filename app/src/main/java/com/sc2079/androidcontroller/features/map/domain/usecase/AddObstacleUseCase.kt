package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot
import com.sc2079.androidcontroller.features.map.domain.model.Obstacle

/**
 * Basic Usecase of Adding a Single Obstacle to our Map
 */
class AddObstacleUseCase {
    // Invoke allows me to call the class like a Function
    operator fun invoke(mapSnapshot: MapSnapshot, x: Int, y: Int): MapSnapshot {
        // Check if the provided coordiante is OOB
        if (!inBounds(x, y)) {
            return mapSnapshot
        }
        // Check if there are existing obstacles on the intended location where we trying to place
        if (mapSnapshot.obstacles.any { it.x == x && it.y == y }) {
            return mapSnapshot
        }

        // Create an Obstacle Object with default faceDir set as UP
        val newObstacle = Obstacle(
            // Get next available obstacle id
            obstacleId = nextAvailableObstacleId(mapSnapshot.obstacles),
            // obstacleId = mapSnapshot.nextObstacleId,
            x = x,
            y = y,
            faceDir = FaceDir.NORTH,
            displayedTargetId = null
        )

        // Return the new state as a copy since it is immutable.
        return mapSnapshot.copy(
            obstacles = mapSnapshot.obstacles + newObstacle,
        )
    }

    // Helper Function to fill in missing obstacles e.g. delete obstacle id 3 when there is 9, next should be not 10
    private fun nextAvailableObstacleId(obstacles: List<Obstacle>): Int {
        // Place all the obstacles id into a hashset
        val used = obstacles.map { it.obstacleId }.toHashSet()

        // Iterate 1 by 1 until we reach one that is not used
        var candidate = 1
        while (used.contains(candidate)) {
            candidate++
        }
        return candidate
    }


    // Helper function to check if the obstacle is added within bounds of the Map and not OOB
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
