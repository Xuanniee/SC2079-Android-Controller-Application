package com.sc2079.androidcontroller.features.map.data.repository

import com.sc2079.androidcontroller.features.map.data.local.MapPreferencesDataSource
import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot
import com.sc2079.androidcontroller.features.map.domain.model.Obstacle
import com.sc2079.androidcontroller.features.map.domain.model.RobotPose
import com.sc2079.androidcontroller.features.map.domain.repository.MapRepository
import org.json.JSONArray
import org.json.JSONObject

class MapRepositoryImpl(
    private val local: MapPreferencesDataSource
) : MapRepository {

    override fun saveSnapshot(name: String, snapshot: MapSnapshot) {
        local.putJson(name, encode(snapshot).toString())
    }

    override fun loadSnapshot(name: String): MapSnapshot? {
        val raw = local.getJson(name) ?: return null
        return decode(JSONObject(raw))
    }

    override fun listSnapshots(): List<String> = local.keys().sorted()

    override fun deleteSnapshot(name: String) = local.delete(name)

    private fun encode(s: MapSnapshot): JSONObject {
        val root = JSONObject()
        root.put("nextObstacleNo", s.nextObstacleNo)

        val robot = s.robotPose?.let {
            JSONObject()
                .put("x", it.x)
                .put("y", it.y)
                .put("dir", it.dir.name)
        }
        root.put("robotPose", robot ?: JSONObject.NULL)

        val obsArr = JSONArray()
        s.obstacles.forEach { o ->
            obsArr.put(
                JSONObject()
                    .put("no", o.no)
                    .put("x", o.x)
                    .put("y", o.y)
                    .put("face", o.face.name)
                    .put("targetId", o.targetId ?: JSONObject.NULL)
            )
        }
        root.put("obstacles", obsArr)
        return root
    }

    private fun decode(obj: JSONObject): MapSnapshot {
        val next = obj.optInt("nextObstacleNo", 1)

        val robotPose = obj.opt("robotPose").let { v ->
            if (v == null || v == JSONObject.NULL) null
            else {
                val r = v as JSONObject
                val dir = FaceDir.valueOf(r.getString("dir"))
                RobotPose(r.getInt("x"), r.getInt("y"), dir)
            }
        }

        val obstacles = mutableListOf<Obstacle>()
        val arr = obj.optJSONArray("obstacles") ?: JSONArray()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            obstacles.add(
                Obstacle(
                    no = o.getInt("no"),
                    x = o.getInt("x"),
                    y = o.getInt("y"),
                    face = FaceDir.valueOf(o.getString("face")),
                    targetId = o.opt("targetId").let { if (it == null || it == JSONObject.NULL) null else it.toString() }
                )
            )
        }

        return MapSnapshot(
            robotPose = robotPose,
            obstacles = obstacles.sortedBy { it.no },
            nextObstacleNo = next
        )
    }
}
