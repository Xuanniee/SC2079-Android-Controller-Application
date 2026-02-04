package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot

/**
 * Resets the Map to a Completely New Map again
 */
class ResetMapUseCase {
    operator fun invoke(): MapSnapshot = MapSnapshot(
        // Remove robot, reset obstacleId, and empty the obstacles
        robotPosition = null,
        obstacles = emptyList(),
//         nextObstacleId = 1
    )
}
