package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot
import com.sc2079.androidcontroller.features.map.domain.model.RobotPosition

/**
 * Define the Robot's Starting Position
 */
class SetRobotPositionUseCase {
    operator fun invoke(mapSnapshot: MapSnapshot, x: Int, y: Int, faceDir: FaceDir): MapSnapshot {
        // Check if OOB
        if (!inBounds(x, y)) {
            return mapSnapshot
        }

        // Create a new Robot Position
        val robotPosition = RobotPosition(x, y, faceDir)
        // Copy the MapSnapshot with an updated robotPosition
        return mapSnapshot.copy(robotPosition = robotPosition)
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
