package com.sc2079.androidcontroller.features.map.domain.model

/**
 * Enum Class defining a finite valid set of directions the robot can face
 */
enum class FaceDir {
    // Possible Directions (4 Ways for now)
    NORTH, SOUTH, WEST, EAST;
//    UP, DOWN, LEFT, RIGHT;

    // Allow for input flexibility from Robot by mapping variable input to 1 of 4 cardinal dirns
    companion object {
        fun fromRobotString(raw: String): FaceDir? {
            return when (raw.trim().uppercase()) {
                "UP", "N", "NORTH" -> NORTH
                "DOWN", "S", "SOUTH" -> SOUTH
                "LEFT", "W", "WEST" -> WEST
                "RIGHT", "E", "EAST" -> EAST
                else -> null
            }
        }
    }
}