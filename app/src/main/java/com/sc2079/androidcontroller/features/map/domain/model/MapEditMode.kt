package com.sc2079.androidcontroller.features.map.domain.model

/**
 * Enum Class indicating the various modes the Map Screen can take
 *
 * - Set start location of robot
 * - Placing an obstacle mode
 * - Changing the face of the obstacle
 * - Dragging Obstacle
 */
enum class MapEditMode {
    Default,
    SetStart,
    PlaceObstacle,
    DragObstacle,
    ChangeObstacleFace
}