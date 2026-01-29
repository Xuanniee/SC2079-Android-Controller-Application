package com.sc2079.androidcontroller.features.map.domain.model

data class RobotPose(
    val x: Int,      // 0..19
    val y: Int,      // 0..19
    val dir: FaceDir // facing (C10)
)
