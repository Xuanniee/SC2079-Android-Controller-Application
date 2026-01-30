package com.sc2079.androidcontroller.features.map.domain.repository

import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot

/**
 * Repository as an abstraction for data access
 */
interface MapRepository {
    // Saves the complete map state of a map defined by the user
    fun saveMapSnapshot(snapshotName: String, snapshot: MapSnapshot)
    // Restore a previously saved map
    fun loadMapSnapshot(snapshotName: String): MapSnapshot?
    // List all the maps saved previously
    fun listMapSnapshots(): List<String>
    // Delete one of the saved maps previously
    fun deleteMapSnapshot(snapshotName: String)
}
