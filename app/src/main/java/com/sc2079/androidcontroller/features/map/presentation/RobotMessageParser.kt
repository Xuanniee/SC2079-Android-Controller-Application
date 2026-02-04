package com.sc2079.androidcontroller.features.map.presentation

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir

/**
 * Represents all possible message types that the Robot can send
 *
 * Sealed class = closed set of subclasses, so when you handle RobotInboundEvent with `when`,
 * the compiler can help ensure you cover all event types.
 */
sealed class RobotInboundEvent {

    /**
     * Robot's current position (position + facing direction) in map coordinates used by the app.
     */
    data class RobotPoseEvent(val x: Int, val y: Int, val dir: FaceDir) : RobotInboundEvent()

    /**
     * A "target" result message from the robot.
     *
     * obstacleNo: which obstacle index/number the target refers to
     * targetId: some identifier for the detected target (e.g., image ID / label)
     */
    data class TargetEvent(val obstacleId: Int, val targetId: Int?) : RobotInboundEvent()

    /**
     * General status / debug / state message from the robot.
     */
    data class StatusEvent(val status: String) : RobotInboundEvent()
}

/**
 * Parses raw text received from the robot into strongly-typed RobotInboundEvent objects.
 *
 * Notes:
 * - Robot may send multiple lines at once (batched), so parse() returns a List.
 * - Parser is tolerant: it ignores unknown/unparseable lines instead of throwing.
 */
object RobotMessageParser {
    /**
     * Convert the raw stream of events from robot into a List of RobotInboundEvent
     */
    fun parse(raw: String): List<RobotInboundEvent> {
        // Split the raw output from robot into multiple lines/events based on newlines and \r
        val lines = raw.split('\n', '\r')
            // Clean whitespace and drop empty lines
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        // Array of Robot Events
        val robotEventsArr = mutableListOf<RobotInboundEvent>()


        /**
         * Every valid message from the Robot must start with STATUS
         * Example inputs:
         * - "STATUS: RUNNING"
         * - "status, DONE"
         */
        for (line in lines) {
            when {
                line.startsWith("STATUS", ignoreCase = true) -> {
                    // Remove the "STATUS" prefix
                    val status = line.substringAfter("STATUS").trim(':', ',', ' ')
                    robotEventsArr += RobotInboundEvent.StatusEvent(status)
                }

                /**
                 * Some robot implementations send pose like:
                 * "ROBOT|5,4,EAST"
                 *
                 * Important quirk (documented in comment):
                 * - first number is y
                 * - second number is x
                 *
                 * This branch handles that "pipe" format.
                 */
                line.contains("ROBOT|", ignoreCase = true) -> {
                    // Extract the part after the '|'
                    val payload = line.substringAfter("|")

                    // Split "5,4,EAST" -> ["5", "4", "EAST"]
                    val parts = payload.split(",").map { it.trim() }

                    // Need at least y, x, dir
                    if (parts.size >= 3) {
                        val y = parts[0].toIntOrNull()
                        val x = parts[1].toIntOrNull()

                        // Convert robot direction string into FaceDir (null if unknown)
                        val dir = FaceDir.fromRobotString(parts[2])

                        // Only emit event if all fields parsed successfully
                        if (x != null && y != null && dir != null) {
                            robotEventsArr += RobotInboundEvent.RobotPoseEvent(x, y, dir)
                        }
                    }
                }

                /**
                 * Another common pose format:
                 * "ROBOT,x,y,DIR"
                 *
                 * Example:
                 * - "ROBOT,4,5,EAST"
                 */
                line.startsWith("ROBOT", ignoreCase = true) -> {
                    val parts = line.split(",").map { it.trim() }

                    // Expect: ["ROBOT", x, y, dir]
                    if (parts.size >= 4) {
                        val x = parts[1].toIntOrNull()
                        val y = parts[2].toIntOrNull()
                        val dir = FaceDir.fromRobotString(parts[3])

                        if (x != null && y != null && dir != null) {
                            robotEventsArr += RobotInboundEvent.RobotPoseEvent(x, y, dir)
                        }
                    }
                }

                /**
                 * TARGET format:
                 * "TARGET,<obstacleNo>,<targetId>"
                 *
                 * Example:
                 * - "TARGET,3,IMG_12"
                 */
                line.startsWith("TARGET", ignoreCase = true) -> {
                    val parts = line.split(",").map { it.trim() }

                    // Expect: ["TARGET", obstacleNo, targetId]
                    if (parts.size >= 3) {
                        val obNo = parts[1].toIntOrNull()
                        val targetId = parts[2].toIntOrNull()

                        // Only emit if obstacle number is valid int
                        if (obNo != null) {
                            robotEventsArr += RobotInboundEvent.TargetEvent(obstacleId =obNo, targetId =targetId)
                        }
                    }
                }

                // Unknown line types are silently ignored.
            }
        }

        // Return all parsed events from this raw message batch.
        return robotEventsArr
    }
}


//package com.sc2079.androidcontroller.features.map.presentation
//
//import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
//
///**
// *
// */
//sealed class RobotInboundEvent {
//    data class RobotPoseEvent(val x: Int, val y: Int, val dir: FaceDir) : RobotInboundEvent()
//    data class TargetEvent(val obstacleNo: Int, val targetId: String) : RobotInboundEvent()
//    data class StatusEvent(val status: String) : RobotInboundEvent()
//}
//
//object RobotMessageParser {
//    fun parse(raw: String): List<RobotInboundEvent> {
//        val lines = raw.split('\n', '\r').map { it.trim() }.filter { it.isNotEmpty() }
//        val out = mutableListOf<RobotInboundEvent>()
//
//        for (line in lines) {
//            when {
//                line.startsWith("STATUS", ignoreCase = true) -> {
//                    val status = line.substringAfter("STATUS").trim(':', ',', ' ')
//                    out += RobotInboundEvent.StatusEvent(status)
//                }
//
//                line.contains("ROBOT|", ignoreCase = true) -> {
//                    // Java: ROBOT|5,4,EAST where first is y, second is x (algo coords)
//                    val payload = line.substringAfter("|")
//                    val parts = payload.split(",").map { it.trim() }
//                    if (parts.size >= 3) {
//                        val y = parts[0].toIntOrNull()
//                        val x = parts[1].toIntOrNull()
//                        val dir = FaceDir.fromRobotString(parts[2])
//                        if (x != null && y != null && dir != null) {
//                            out += RobotInboundEvent.RobotPoseEvent(x, y, dir)
//                        }
//                    }
//                }
//
//                line.startsWith("ROBOT", ignoreCase = true) -> {
//                    // Expected: ROBOT,x,y,DIR
//                    val parts = line.split(",").map { it.trim() }
//                    if (parts.size >= 4) {
//                        val x = parts[1].toIntOrNull()
//                        val y = parts[2].toIntOrNull()
//                        val dir = FaceDir.fromRobotString(parts[3])
//                        if (x != null && y != null && dir != null) {
//                            out += RobotInboundEvent.RobotPoseEvent(x, y, dir)
//                        }
//                    }
//                }
//
//                line.startsWith("TARGET", ignoreCase = true) -> {
//                    val parts = line.split(",").map { it.trim() }
//                    if (parts.size >= 3) {
//                        val obNo = parts[1].toIntOrNull()
//                        val targetId = parts[2]
//                        if (obNo != null) {
//                            out += RobotInboundEvent.TargetEvent(obNo, targetId)
//                        }
//                    }
//                }
//            }
//        }
//
//        return out
//    }
//}