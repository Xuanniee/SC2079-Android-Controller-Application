package com.sc2079.androidcontroller.features.map.ui.util

import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode

fun editModeLabel(mode: MapEditMode): String = when (mode) {
    MapEditMode.Cursor -> "None"
    MapEditMode.SetStart -> "Place Robot"
    MapEditMode.PlaceObstacle -> "Add Obstacle"
    MapEditMode.DragObstacle -> "Drag Obstacle"
    MapEditMode.ChangeObstacleFace -> "Change Face"
}