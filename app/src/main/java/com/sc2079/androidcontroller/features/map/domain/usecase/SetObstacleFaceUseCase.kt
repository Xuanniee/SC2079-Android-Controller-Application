package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot

class SetObstacleFaceUseCase {
    operator fun invoke(snapshot: MapSnapshot, obstacleNo: Int, face: FaceDir): MapSnapshot {
        val idx = snapshot.obstacles.indexOfFirst { it.no == obstacleNo }
        if (idx < 0) return snapshot

        val updated = snapshot.obstacles[idx].copy(face = face)
        val newList = snapshot.obstacles.toMutableList().also { it[idx] = updated }
        return snapshot.copy(obstacles = newList)
    }
}
