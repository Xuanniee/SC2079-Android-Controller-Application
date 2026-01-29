package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot
import com.sc2079.androidcontroller.features.map.domain.model.Obstacle

class AddObstacleUseCase {
    operator fun invoke(snapshot: MapSnapshot, x: Int, y: Int): MapSnapshot {
        if (!inBounds(x, y)) return snapshot
        if (snapshot.obstacles.any { it.x == x && it.y == y }) return snapshot

        val obs = Obstacle(
            no = snapshot.nextObstacleNo,
            x = x,
            y = y,
            face = FaceDir.UP,      // Java default bearing behaviour
            targetId = null
        )

        return snapshot.copy(
            obstacles = snapshot.obstacles + obs,
            nextObstacleNo = snapshot.nextObstacleNo + 1
        )
    }

    private fun inBounds(x: Int, y: Int) = x in 0..19 && y in 0..19
}
