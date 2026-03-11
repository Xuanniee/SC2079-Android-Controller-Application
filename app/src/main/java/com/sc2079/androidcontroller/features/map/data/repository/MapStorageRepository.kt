//package com.sc2079.androidcontroller.features.map.data.repository
//
//import android.content.Context
//import com.google.gson.Gson
//import com.google.gson.reflect.TypeToken
//import com.sc2079.androidcontroller.features.map.data.model.SavedMap
//
//class MapStorageRepository(context: Context) {
//
//    private val prefs = context.getSharedPreferences("saved_maps_prefs", Context.MODE_PRIVATE)
//    private val gson = Gson()
//
//    companion object {
//        private const val KEY_MAPS = "KEY_MAPS"
//    }
//
//    fun saveMap(map: SavedMap) {
//        val allMaps = getAllMaps().toMutableMap()
//        allMaps[map.name] = map
//        persist(allMaps)
//    }
//
//    fun loadMap(name: String): SavedMap? {
//        return getAllMaps()[name]
//    }
//
//    fun deleteMap(name: String) {
//        val allMaps = getAllMaps().toMutableMap()
//        allMaps.remove(name)
//        persist(allMaps)
//    }
//
//    fun getSavedMapNames(): List<String> {
//        return getAllMaps().keys.sorted()
//    }
//
//    private fun getAllMaps(): Map<String, SavedMap> {
//        val json = prefs.getString(KEY_MAPS, null) ?: return emptyMap()
//        val type = object : TypeToken<Map<String, SavedMap>>() {}.type
//        return gson.fromJson(json, type) ?: emptyMap()
//    }
//
//    private fun persist(maps: Map<String, SavedMap>) {
//        val json = gson.toJson(maps)
//        prefs.edit().putString(KEY_MAPS, json).apply()
//    }
//}