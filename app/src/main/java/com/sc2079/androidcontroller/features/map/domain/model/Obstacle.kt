package com.sc2079.androidcontroller.features.map.domain.model

data class Obstacle(
    val no: Int,
    val x: Int,           // 0..19 (bottom-left origin)
    val y: Int,           // 0..19 (bottom-left origin)
    val face: FaceDir,    // which side has the image/target face (C7)
    val targetId: String? // displayed target id (C9)
)
