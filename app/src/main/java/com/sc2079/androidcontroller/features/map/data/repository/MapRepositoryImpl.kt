package com.sc2079.androidcontroller.features.map.data.repository

import com.sc2079.androidcontroller.features.map.data.local.MapPreferencesDataSource
import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot
import com.sc2079.androidcontroller.features.map.domain.model.Obstacle
import com.sc2079.androidcontroller.features.map.domain.model.RobotPosition
import com.sc2079.androidcontroller.features.map.domain.repository.MapRepository
import org.json.JSONArray
import org.json.JSONObject

/**
 * Data-layer implementation of the MapRepository contract.
 *
 * Responsible for:
 * - Serializing MapSnapshot <-> JSON
 * - Delegating raw persistence to MapPreferencesDataSource
 */
class MapRepositoryImpl(
    private val localMapData: MapPreferencesDataSource
) : MapRepository {
    // Override function to save snapshot of map locally on disk as a xml file
    override fun saveMapSnapshot(snapshotName: String, snapshot: MapSnapshot) {
        // Encode and save the Map as a JSON String
        localMapData.writeMapJson(snapshotName, encodeMapToJson(snapshot).toString())
    }

    // Retrieve JSON Map string from local storage
    override fun loadMapSnapshot(snapshotName: String): MapSnapshot? {
        // Retrieve JSON String, null if doesnt exist
        val rawJsonString = localMapData.getMapJson(snapshotName) ?: return null

        // Translate back into a MapSnapshot object and return
        return decodeJsonToMap(JSONObject(rawJsonString))
    }

    // Retrieve all the names of the maps saved previously
    override fun listMapSnapshots(): List<String> {
        return localMapData.retrieveSavedMapKeys().sorted()
    }

    // Delete a particular snapshot that was saved previously
    override fun deleteMapSnapshot(snapshotName: String) {
        localMapData.deleteMapJson(snapshotName)
    }

    // Serialises the state of my MapSnapshot into a JSON object
    private fun encodeMapToJson(mapSnapshot: MapSnapshot): JSONObject {
        // Createa JSON Object to hold the data from the MapSnapshot
        val mapJsonObject = JSONObject()

        // Place all the properties of a MapSnapshot into the JSON Object
        mapJsonObject.put("nextObstacleId", mapSnapshot.nextObstacleId)
        // Store the robotPosition into a JSON object
        val robotPosition = mapSnapshot.robotPosition?.let {
            JSONObject()
                .put("x", it.x)
                .put("y", it.y)
                .put("faceDir", it.faceDir.name)
        }
        mapJsonObject.put("robotPosition", robotPosition ?: JSONObject.NULL)

        // Store each obstacle into the array of JSON Object
        val obstacleArr = JSONArray()
        mapSnapshot.obstacles.forEach { o ->
            obstacleArr.put(
                JSONObject()
                    .put("obstacleId", o.obstacleId)
                    .put("x", o.x)
                    .put("y", o.y)
                    .put("faceDir", o.faceDir.name)
                    .put("displayedTargetId", o.displayedTargetId ?: JSONObject.NULL)
            )
        }
        mapJsonObject.put("obstacles", obstacleArr)

        return mapJsonObject
    }

    // Deserialise a JSONObject back into a MapSnapshot to be used
    private fun decodeJsonToMap(obj: JSONObject): MapSnapshot {
        // Extract all the properties of a MapSnapshot from the JSONObject
        val nextObstacleId = obj.optInt("nextObstacleId", 1)

        // Extract the RobotPosition JSONObject if it exists
        val robotPosition = obj.opt("robotPosition").let { robotPositionJsonRaw ->
            if (robotPositionJsonRaw == null || robotPositionJsonRaw == JSONObject.NULL) null
            else {
                val robotPositionJson = robotPositionJsonRaw as JSONObject

                // Extract the attributes and convert it into a RobotPosition Type
                val faceDir = FaceDir.valueOf(robotPositionJson.getString("faceDir"))
                RobotPosition(robotPositionJson.getInt("x"), robotPositionJson.getInt("y"), faceDir)
            }
        }

        // Create a mutable array to store all our obstacles
        val obstacles = mutableListOf<Obstacle>()
        // Extract the JSON Array obstacles if it exists
        val obstaclesJsonArr = obj.optJSONArray("obstacles") ?: JSONArray()
        // Iterate through the JSON Arr and extract each obstacke
        for (i in 0 until obstaclesJsonArr.length()) {
            // Extract the current obstacle
            val obstacle = obstaclesJsonArr.getJSONObject(i)
            // Append the copied obstacle object from Jsonarr to an array of type Obstacle
            obstacles.add(
                Obstacle(
                    obstacleId = obstacle.getInt("obstacleId"),
                    x = obstacle.getInt("x"),
                    y = obstacle.getInt("y"),
                    faceDir = FaceDir.valueOf(obstacle.getString("faceDir")),
                    displayedTargetId = obstacle.opt("displayedTargetId")
                        .let { if (it == null || it == JSONObject.NULL) null else it.toString() }
                )
            )
        }

        // Return the decoded MapSnapshot
        return MapSnapshot(
            robotPosition = robotPosition,
            obstacles = obstacles.sortedBy { it.obstacleId },
            nextObstacleId = nextObstacleId
        )
    }
}
