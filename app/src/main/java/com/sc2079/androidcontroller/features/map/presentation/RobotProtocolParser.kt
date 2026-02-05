package com.sc2079.androidcontroller.features.map.presentation

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.ProtocolMessage
import com.sc2079.androidcontroller.features.map.domain.model.RobotInboundEvent

/**
 * Single source of truth for message exchange between Robot and Application
 *
 * - Returns ONLY:
 *   1) ProtocolMessage (existing STATUS/CLEAR protocol)
 *   2) RobotInboundEvent (only RobotPositionEvent + TargetEvent for now)
 *
 * - Returns null for everything else (e.g. "hello world")
 */
object RobotProtocolParser {
    // From Application to RObot
    private val ALLOWED_OPERATIONS = setOf(
        "OBSTACLE PLACEMENT",
        "OBSTACLE DELETION",
        "OBSTACLE ORIENTATION",
        "OBSTACLE LIST",
        "ROBOT PLACEMENT",
        "ROBOT MOVE",
        "ROBOT ORIENTATION",
    )

    /**
     * Function to parse Protocol Messages from App to Robot into one of the ALLOWED_OPERATIONS
     * Don't allow other text strings to be passed
     */
    fun parseProtocol(raw: String): ProtocolMessage? {
        // Remove whitespaces and return null if no messages
        val msg = raw.trim()

        if (msg.isEmpty()) {
            return null
        }
        // Special Message 1: Return if this is a CLEAR Message
        if (msg.equals("CLEAR", ignoreCase = true)) {
            return ProtocolMessage.Clear
        }
        // Ensure all messages start with STATUS
        if (!msg.startsWith("STATUS", ignoreCase = true)) {
            return null
        }

        // Do not process if it is not an allowed operation
        val (action, payload) = parseHeader(msg) ?: return null
        if (action !in ALLOWED_OPERATIONS) {
            return null
        }

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

    private const val ROBOT_PREFIX = "ROBOT"
    private const val TARGET_PREFIX = "TARGET"

    /**
     * Parse a raw inbound Bluetooth line into one of the allowed RobotInboundEvent(s).
     * Returns null if the message is not one of the allowed formats.
     */
    fun parseRobot(raw: String): RobotInboundEvent? {
        val msg = raw.trim()
        if (msg.isEmpty()) return null

        // Robot -> app events ONLY
        parseRobotPositionEvent(msg)?.let { return it }
        parseTargetEvent(msg)?.let { return it }

        // Reject everything else e.g. "hello world"
        return null
    }

    /**
     * Parse a raw inbound Bluetooth chunk that may contain multiple lines.
     * Returns a list of RobotInboundEvent(s) parsed from each non-empty line.
     */
    fun parseRobotBatch(raw: String): List<RobotInboundEvent> {
        val lines = raw.split('\n', '\r')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        val out = mutableListOf<RobotInboundEvent>()
        for (line in lines) {
            parseRobot(line)?.let { out += it }
        }
        return out
    }


    // Parses the 2nd type of Robot Events which is to update their postiion
    private fun parseRobotPositionEvent(msg: String): RobotInboundEvent.RobotPositionEvent? {
        // Accept:
        // "ROBOT,<x>,<y>,<faceDir>"
        if (!msg.startsWith(ROBOT_PREFIX, ignoreCase = true)) {
            return null
        }

        // Split into the remaining parts
        val parts = msg.split(",").map { it.trim() }
        if (parts.size != 4) {
            return null
        }

        // Retrieve the 3 key variables
        val x = parts[1].toIntOrNull() ?: return null
        val y = parts[2].toIntOrNull() ?: return null
        val faceDir = parseFaceSafe(parts[3]) ?: return null

        return RobotInboundEvent.RobotPositionEvent(x = x, y = y, faceDir = faceDir)
    }

//    // Parses Messages from the Robot
//    fun parseRobot(raw: String): ParsedInbound? {
//        // Trim and check for empty messages
//        val msg = raw.trim()
//        if (msg.isEmpty()) {
//            return null
//        }
//
//        // Robots can either send a Protocol Message or Robot Event
//        // Robot Events
//        parseTargetEvent(msg)?.let { return ParsedInbound.Event(it) }
//
//        // CLEAR / STATUS protocol - unlikely from
//        parseProtocol(msg)?.let { protocol ->
//            // Convert robot position-related STATUS messages into RobotPositionEvent
//            protocolToRobotPositionEvent(protocol)?.let { ev ->
//                return ParsedInbound.Event(ev)
//            }
//            // Otherwise keep it as protocol message (obstacle list/placement etc.)
//            return ParsedInbound.Protocol(protocol)
//        }
//
//        // 3) Everything else is ignored (e.g. "hello world")
//        return null
//    }

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

    /**
     * Parse the Face Direction
     */
    private fun parseFaceSafe(s: String): FaceDir? = when (s.trim().uppercase()) {
        "NORTH" -> FaceDir.NORTH
        "SOUTH" -> FaceDir.SOUTH
        "WEST" -> FaceDir.WEST
        "EAST" -> FaceDir.EAST
        else -> null
    }

    private fun splitCsv(payload: String): List<String> =
        payload.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    /**
     * Function to parse a BT message for placing an Obstacle
     *
     * Full Message Fields: STATUS, OBSTACLE PLACEMENT, obstacleId, currX, currY, faceDir
     * STATUS & ACTION TYPE is removed earlier in parent function, so only last 4 are present
     */
    private fun parseObstaclePlacement(payload: String): ProtocolMessage? {
        // Split the Messags into multiple parts
        val parts = splitCsv(payload)
        // Ensure all fields are present for valid protocl message
        if (parts.size < 4) {
            return null
        }

        // Extract the 4 fields
        val id = parseIntSafe(parts[0]) ?: return null
        val x = parseIntSafe(parts[1]) ?: return null
        val y = parseIntSafe(parts[2]) ?: return null
        val faceDir = parseFaceSafe(parts[3]) ?: return null

        return ProtocolMessage.ObstaclePlacement(id, x, y, faceDir)
    }

    /**
     *
     */
    private fun parseObstacleDeletion(payload: String): ProtocolMessage? {
        val parts = splitCsv(payload)
        val id = parts.firstOrNull()?.let { parseIntSafe(it) } ?: return null

        return ProtocolMessage.ObstacleDeletion(id)
    }

    private fun parseObstacleOrientation(payload: String): ProtocolMessage? {
        val parts = splitCsv(payload)
        if (parts.size < 5) {
            return null
        }

        // Extract necessary fields
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

    /**
     * Responsible for parsing the Target Events from the Robot
     *
     * Accept:
     * "TARGET,<obstacleId>,<targetId>"
     * where <targetId> is:
     * - Int
     * - "NULL"
     */
    private fun parseTargetEvent(msg: String): RobotInboundEvent.TargetEvent? {
        // Ensure sufficient information is provided
        if (!msg.startsWith(TARGET_PREFIX, ignoreCase = true)) {
            return null
        }
        val parts = msg.split(",").map { it.trim() }
        if (parts.size != 3) {
            return null
        }

        // Retrieve keys
        val obstacleId = parts[1].toIntOrNull() ?: return null
        val rawId = parts[2]
        val targetId = when (rawId.uppercase()) {
            "NULL" -> null
            else -> if (rawId == "-1") null else rawId.toIntOrNull()
        } ?: run {
            // If it wasn't NULL/-1 and isn't an int -> reject message
            if (rawId.uppercase() == "NULL" || rawId == "-1") {
                null
            } else {
                return null
            }
        }

        return RobotInboundEvent.TargetEvent(obstacleId = obstacleId, targetId = targetId)
    }
//    private fun parseTargetEvent(msg: String): RobotInboundEvent.TargetEvent? {
//        // Ensure Target Event starts with Target
//        if (!msg.startsWith("TARGET", ignoreCase = true)) {
//            return null
//        }
//
//        // Delimit the RobotEvent based on commas
//        val parts = msg.split(",").map { it.trim() }
//        if (parts.size != 3) {
//            // Exact 3 parts
//            return null
//        }
//
//        // Retrieve the 2 fields, ignore Target
//        val obstacleId = parts[1].toIntOrNull() ?: return null
//        // TODO do not allow -1 for now
//        val targetId = when (parts[2].uppercase()) {
//            "NULL" -> null
//            else -> parts[2].toIntOrNull() ?: return null
//        }
//
//        return RobotInboundEvent.TargetEvent(
//            obstacleId = obstacleId,
//            targetId = targetId
//        )
//    }

}
