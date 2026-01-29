package com.sc2079.androidcontroller.features.map.domain.usecase

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot
import com.sc2079.androidcontroller.features.map.domain.model.RobotPose

class SetRobotPoseUseCase {
    operator fun invoke(snapshot: MapSnapshot, x: Int, y: Int, dir: FaceDir): MapSnapshot {
        if (!inBounds(x, y)) return snapshot
        return snapshot.copy(robotPose = RobotPose(x, y, dir))
    }

    private fun inBounds(x: Int, y: Int) = x in 0..19 && y in 0..19
}
