package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot

class ResetMapUseCase {
    operator fun invoke(): MapSnapshot = MapSnapshot(
        robotPose = null,
        obstacles = emptyList(),
        nextObstacleNo = 1
    )
}
