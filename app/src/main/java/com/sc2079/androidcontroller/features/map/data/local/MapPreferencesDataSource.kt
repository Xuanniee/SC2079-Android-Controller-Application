package com.sc2079.androidcontroller.features.map.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * Android dependent data source
 *
 * Using sharedpreferences as a key-value store to store simple data locally on tablet.
 * Requires a context to the app to get local storage access
 */
class MapPreferencesDataSource(context: Context) {
    // Initialise and retrieve the sharedpreferences object
    private val prefs: SharedPreferences =
        context.getSharedPreferences("map_snapshots", Context.MODE_PRIVATE)

    // Saves a copy of the user map locally
    fun writeMapJson(mapName: String, mapJson: String) {
        // Opens a SP transaction to save the key value pair, where key is name of map
        prefs.edit {
            putString(mapName, mapJson)
        }
    }

    // Retreive a particular Map JSON Object
    fun getMapJson(mapName: String): String? {
        return prefs.getString(mapName, null)
    }

    // Retrieve all the names of all the maps saved previously
    fun retrieveSavedMapKeys(): List<String> {
        return prefs.all.keys.toList()
    }

    // Delete a particular map json objkect
    fun deleteMapJson(mapName: String) {
        // SP Transaction
        prefs.edit {
            remove(mapName)
        }
    }
}