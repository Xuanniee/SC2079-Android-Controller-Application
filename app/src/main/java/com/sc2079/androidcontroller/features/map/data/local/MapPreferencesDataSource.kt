package com.sc2079.androidcontroller.features.map.data.local

import android.content.Context
import android.content.SharedPreferences

class MapPreferencesDataSource(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("mapping_snapshots", Context.MODE_PRIVATE)

    fun putJson(name: String, json: String) {
        prefs.edit().putString(name, json).apply()
    }

    fun getJson(name: String): String? = prefs.getString(name, null)

    fun keys(): List<String> = prefs.all.keys.toList()

    fun delete(name: String) {
        prefs.edit().remove(name).apply()
    }
}