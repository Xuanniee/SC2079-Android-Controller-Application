package com.sc2079.androidcontroller.features.map.presentation

import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import com.sc2079.androidcontroller.features.map.domain.model.Obstacle
import com.sc2079.androidcontroller.features.map.domain.model.RobotPose

data class MapUiState(
    val editMode: MapEditMode = MapEditMode.Default,
    val robotStatus: String = "",
    val robotPose: RobotPose? = null,
    val obstacles: List<Obstacle> = emptyList(),
    val savedMaps: List<String> = emptyList()
)
