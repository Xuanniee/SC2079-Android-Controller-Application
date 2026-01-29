package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot

class MoveObstacleUseCase {
    operator fun invoke(snapshot: MapSnapshot, obstacleNo: Int, newX: Int, newY: Int): MapSnapshot {
        if (!inBounds(newX, newY)) return snapshot
        val idx = snapshot.obstacles.indexOfFirst { it.no == obstacleNo }
        if (idx < 0) return snapshot

        // disallow stacking (same as Java behaviour)
        if (snapshot.obstacles.any { it.no != obstacleNo && it.x == newX && it.y == newY }) return snapshot

        val updated = snapshot.obstacles[idx].copy(x = newX, y = newY)
        val newList = snapshot.obstacles.toMutableList().also { it[idx] = updated }
        return snapshot.copy(obstacles = newList)
    }

    private fun inBounds(x: Int, y: Int) = x in 0..19 && y in 0..19
}
