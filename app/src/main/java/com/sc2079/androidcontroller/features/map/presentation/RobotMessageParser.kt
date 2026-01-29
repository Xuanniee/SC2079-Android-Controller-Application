package com.sc2079.androidcontroller.features.map.presentation

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir

sealed class RobotInboundEvent {
    data class RobotPoseEvent(val x: Int, val y: Int, val dir: FaceDir) : RobotInboundEvent()
    data class TargetEvent(val obstacleNo: Int, val targetId: String) : RobotInboundEvent()
    data class StatusEvent(val status: String) : RobotInboundEvent()
}

object RobotMessageParser {

    fun parse(raw: String): List<RobotInboundEvent> {
        val lines = raw.split('\n', '\r').map { it.trim() }.filter { it.isNotEmpty() }
        val out = mutableListOf<RobotInboundEvent>()

        for (line in lines) {
            when {
                line.startsWith("STATUS", ignoreCase = true) -> {
                    val status = line.substringAfter("STATUS").trim(':', ',', ' ')
                    out += RobotInboundEvent.StatusEvent(status)
                }

                line.contains("ROBOT|", ignoreCase = true) -> {
                    // Java: ROBOT|5,4,EAST where first is y, second is x (algo coords)
                    val payload = line.substringAfter("|")
                    val parts = payload.split(",").map { it.trim() }
                    if (parts.size >= 3) {
                        val y = parts[0].toIntOrNull()
                        val x = parts[1].toIntOrNull()
                        val dir = FaceDir.fromRobotString(parts[2])
                        if (x != null && y != null && dir != null) {
                            out += RobotInboundEvent.RobotPoseEvent(x, y, dir)
                        }
                    }
                }

                line.startsWith("ROBOT", ignoreCase = true) -> {
                    // Expected: ROBOT,x,y,DIR
                    val parts = line.split(",").map { it.trim() }
                    if (parts.size >= 4) {
                        val x = parts[1].toIntOrNull()
                        val y = parts[2].toIntOrNull()
                        val dir = FaceDir.fromRobotString(parts[3])
                        if (x != null && y != null && dir != null) {
                            out += RobotInboundEvent.RobotPoseEvent(x, y, dir)
                        }
                    }
                }

                line.startsWith("TARGET", ignoreCase = true) -> {
                    val parts = line.split(",").map { it.trim() }
                    if (parts.size >= 3) {
                        val obNo = parts[1].toIntOrNull()
                        val targetId = parts[2]
                        if (obNo != null) {
                            out += RobotInboundEvent.TargetEvent(obNo, targetId)
                        }
                    }
                }
            }
        }

        return out
    }
}