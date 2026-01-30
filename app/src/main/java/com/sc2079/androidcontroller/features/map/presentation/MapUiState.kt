package com.sc2079.androidcontroller.features.map.presentation

import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import com.sc2079.androidcontroller.features.map.domain.model.Obstacle
import com.sc2079.androidcontroller.features.map.domain.model.RobotPosition

/**
 * Holds the UiState of the Map at a point in time
 */
data class MapUiState(
    // Holds the curent mode the user is in for editing the Map
    val editMode: MapEditMode = MapEditMode.Cursor,
    // Status of Robot, either set or the location
    val robotStatus: String = "",
    // Where the robot is located at
    val robotPosition: RobotPosition? = null,
    // List of Obstacles and Maps saved in the Map
    val obstacles: List<Obstacle> = emptyList(),
    val savedMaps: List<String> = emptyList()
)
