package com.sc2079.androidcontroller.features.map.domain.repository

import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot

interface MapRepository {
    fun saveSnapshot(name: String, snapshot: MapSnapshot)
    fun loadSnapshot(name: String): MapSnapshot?
    fun listSnapshots(): List<String>
    fun deleteSnapshot(name: String)
}
