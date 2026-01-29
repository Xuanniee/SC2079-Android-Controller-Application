package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot

class RemoveObstacleUseCase {
    operator fun invoke(snapshot: MapSnapshot, obstacleNo: Int): MapSnapshot {
        return snapshot.copy(obstacles = snapshot.obstacles.filterNot { it.no == obstacleNo })
    }
}
