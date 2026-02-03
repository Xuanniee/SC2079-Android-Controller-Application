package com.sc2079.androidcontroller.features.map.domain.model

/**
 * Constants for map dimensions and configuration.
 * Centralized location for map size to ensure consistency across all use cases.
 */
interface MapConstants {
    /**
     * The size of the map grid (map is MAP_SIZE x MAP_SIZE)
     * Valid coordinates are 0 to (MAP_SIZE - 1) for both x and y
     */
    val mapSize: Int
        get() = 20
}

/**
 * Default implementation of MapConstants
 */
object DefaultMapConstants : MapConstants
