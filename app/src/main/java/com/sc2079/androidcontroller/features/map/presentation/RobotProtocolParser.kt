package com.sc2079.androidcontroller.features.map.presentation

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.ProtocolMessage

object RobotProtocolParser {

    private val ALLOWED_OPERATIONS = setOf(
        "OBSTACLE PLACEMENT",
        "OBSTACLE DELETION",
        "OBSTACLE ORIENTATION",
        "OBSTACLE LIST",
        "ROBOT PLACEMENT",
        "ROBOT MOVE",
        "ROBOT ORIENTATION"
    )

    /**
     * Function to parse commands into one of the ALLOWED_OPERATIONS
     * Don't allow other text strings to be passed
     */
    fun parse(raw: String): ProtocolMessage? {
        val msg = raw.trim()
        if (msg.isEmpty()) return null

        if (msg.equals("CLEAR", ignoreCase = true)) return ProtocolMessage.Clear
        if (!msg.startsWith("STATUS", ignoreCase = true)) return null

        val (action, payload) = parseHeader(msg) ?: return null
        if (action !in ALLOWED_OPERATIONS) return null

        return when (action) {
            "OBSTACLE PLACEMENT" -> parseObstaclePlacement(payload)
            "OBSTACLE DELETION" -> parseObstacleDeletion(payload)
            "OBSTACLE ORIENTATION" -> parseObstacleOrientation(payload)
            "OBSTACLE LIST" -> parseObstacleList(payload)
            "ROBOT PLACEMENT" -> parseRobotPlacement(payload)
            "ROBOT MOVE" -> parseRobotMove(payload)
            "ROBOT ORIENTATION" -> parseRobotOrientation(payload)
            else -> null
        }
    }

    private fun parseHeader(msg: String): Pair<String, String>? {
        val firstComma = msg.indexOf(',')
        if (firstComma == -1) return null

        val secondComma = msg.indexOf(',', startIndex = firstComma + 1)
        if (secondComma == -1) return null

        val prefix = msg.take(firstComma).trim()
        if (!prefix.equals("STATUS", ignoreCase = true)) return null

        val action = msg.substring(firstComma + 1, secondComma).trim().uppercase()
        val payload = msg.substring(secondComma + 1).trim()
        return action to payload
    }

    private fun parseIntSafe(s: String): Int? = s.trim().toIntOrNull()

    private fun parseFaceSafe(s: String): FaceDir? = when (s.trim().uppercase()) {
        "NORTH" -> FaceDir.NORTH
        "SOUTH" -> FaceDir.SOUTH
        "WEST" -> FaceDir.WEST
        "EAST" -> FaceDir.EAST
        else -> null
    }

    private fun splitCsv(payload: String): List<String> =
        payload.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    private fun parseObstaclePlacement(payload: String): ProtocolMessage? {
        val parts = splitCsv(payload)
        if (parts.size < 4) return null
        val id = parseIntSafe(parts[0]) ?: return null
        val x = parseIntSafe(parts[1]) ?: return null
        val y = parseIntSafe(parts[2]) ?: return null
        val face = parseFaceSafe(parts[3]) ?: return null
        return ProtocolMessage.ObstaclePlacement(id, x, y, face)
    }

    private fun parseObstacleDeletion(payload: String): ProtocolMessage? {
        val parts = splitCsv(payload)
        val id = parts.firstOrNull()?.let { parseIntSafe(it) } ?: return null
        return ProtocolMessage.ObstacleDeletion(id)
    }

    private fun parseObstacleOrientation(payload: String): ProtocolMessage? {
        val parts = splitCsv(payload)
        if (parts.size < 5) return null
        val id = parseIntSafe(parts[0]) ?: return null
        val x = parseIntSafe(parts[1]) ?: return null
        val y = parseIntSafe(parts[2]) ?: return null
        val oldFace = parseFaceSafe(parts[3]) ?: return null
        val newFace = parseFaceSafe(parts[4]) ?: return null
        return ProtocolMessage.ObstacleOrientation(id, x, y, oldFace, newFace)
    }

    private fun parseObstacleList(payload: String): ProtocolMessage? {
        val trimmed = payload.trim()
        if (trimmed == "0") return ProtocolMessage.ObstacleList(emptyList())

        val firstComma = trimmed.indexOf(',')
        if (firstComma == -1) return null

        val n = parseIntSafe(trimmed.take(firstComma)) ?: return null
        val rest = trimmed.substring(firstComma + 1).trim()
        if (n == 0) return ProtocolMessage.ObstacleList(emptyList())

        val itemsRaw = rest.split("|").map { it.trim() }.filter { it.isNotEmpty() }
        val items = itemsRaw.mapNotNull { itemStr ->
            val fields = itemStr.split(";").map { it.trim() }.filter { it.isNotEmpty() }
            if (fields.size < 4) return@mapNotNull null
            val id = parseIntSafe(fields[0]) ?: return@mapNotNull null
            val x = parseIntSafe(fields[1]) ?: return@mapNotNull null
            val y = parseIntSafe(fields[2]) ?: return@mapNotNull null
            val face = parseFaceSafe(fields[3]) ?: return@mapNotNull null
            ProtocolMessage.ObstacleList.ObstacleItem(id, x, y, face)
        }

        if (items.size != n) return null
        return ProtocolMessage.ObstacleList(items)
    }

    private fun parseRobotPlacement(payload: String): ProtocolMessage? {
        val parts = splitCsv(payload)
        if (parts.size < 3) return null
        val x = parseIntSafe(parts[0]) ?: return null
        val y = parseIntSafe(parts[1]) ?: return null
        val face = parseFaceSafe(parts[2]) ?: return null
        return ProtocolMessage.RobotPlacement(x, y, face)
    }

    private fun parseRobotMove(payload: String): ProtocolMessage? {
        val parts = splitCsv(payload)
        if (parts.size < 3) return null
        val x = parseIntSafe(parts[0]) ?: return null
        val y = parseIntSafe(parts[1]) ?: return null
        val face = parseFaceSafe(parts[2]) ?: return null
        return ProtocolMessage.RobotMove(x, y, face)
    }

    private fun parseRobotOrientation(payload: String): ProtocolMessage? {
        val parts = splitCsv(payload)
        if (parts.size < 4) return null
        val x = parseIntSafe(parts[0]) ?: return null
        val y = parseIntSafe(parts[1]) ?: return null
        val oldFace = parseFaceSafe(parts[2]) ?: return null
        val newFace = parseFaceSafe(parts[3]) ?: return null
        return ProtocolMessage.RobotOrientation(x, y, oldFace, newFace)
    }
}
