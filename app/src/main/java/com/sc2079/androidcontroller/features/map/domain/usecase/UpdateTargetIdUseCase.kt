package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot

class UpdateTargetIdUseCase {
    operator fun invoke(snapshot: MapSnapshot, obstacleNo: Int, targetId: String): MapSnapshot {
        val idx = snapshot.obstacles.indexOfFirst { it.no == obstacleNo }
        if (idx < 0) return snapshot

        val updated = snapshot.obstacles[idx].copy(targetId = targetId)
        val newList = snapshot.obstacles.toMutableList().also { it[idx] = updated }
        return snapshot.copy(obstacles = newList)
    }
}
