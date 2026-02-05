package com.sc2079.androidcontroller.features.map.presentation

import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.Obstacle
import com.sc2079.androidcontroller.features.map.domain.model.RobotPosition

object RobotProtocol {
    private const val mapUnit: Int = 1

    fun validateRobotMessage(raw: String): Boolean {
        return RobotProtocolParser.parseRobot(raw) != null
    }

    fun validateProtocolMessage(raw: String): Boolean {
        return RobotProtocolParser.parseProtocol(raw) != null
    }

    fun clear(): String = "CLEAR"

    fun upsertObstacle(obstacle: Obstacle): String =
        "STATUS, OBSTACLE PLACEMENT, ${obstacle.obstacleId}, ${obstacle.x * mapUnit}, ${obstacle.y * mapUnit}, ${obstacle.faceDir.name}"

    fun removeObstacle(obstacle: Obstacle): String =
        "STATUS, OBSTACLE DELETION, ${obstacle.obstacleId}, -1, -1, -1"

    fun changeObstacleOrientation(obstacle: Obstacle, oldFaceDir: FaceDir, newFaceDir: FaceDir): String =
        "STATUS, OBSTACLE ORIENTATION, ${obstacle.obstacleId}, ${obstacle.x * mapUnit}, ${obstacle.y * mapUnit}, ${oldFaceDir.name}, ${newFaceDir.name}"

    fun sendObstacleList(obstacles: List<Obstacle>): String {
        if (obstacles.isEmpty()) return "STATUS, OBSTACLE LIST, 0"

        val payload = obstacles.joinToString(" | ") { o ->
            "${o.obstacleId}; ${o.x * mapUnit}; ${o.y * mapUnit}; ${o.faceDir.name}"
        }
        return "STATUS, OBSTACLE LIST, ${obstacles.size}, $payload"
    }

    /**
     * From App to Robot Messages
     */
    fun placeRobot(robotPosition: RobotPosition): String =
        "STATUS, ROBOT PLACEMENT, ${robotPosition.x * mapUnit}, ${robotPosition.y * mapUnit}, ${robotPosition.faceDir.name}"

    fun moveRobot(robotPosition: RobotPosition): String =
        "STATUS, ROBOT MOVE, ${robotPosition.x * mapUnit}, ${robotPosition.y * mapUnit}, ${robotPosition.faceDir.name}"

    fun changeRobotOrientation(robotPosition: RobotPosition, newFaceDir: FaceDir): String =
        "STATUS, ROBOT ORIENTATION, ${robotPosition.x * mapUnit}, ${robotPosition.y * mapUnit}, ${robotPosition.faceDir.name}, ${newFaceDir.name}"
}


//package com.sc2079.androidcontroller.features.map.presentation
//
//import android.provider.ContactsContract.CommonDataKinds.StructuredName.PREFIX
//import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
//import com.sc2079.androidcontroller.features.map.domain.model.Obstacle
//import com.sc2079.androidcontroller.features.map.domain.model.RobotPosition
//
///**
// * Robot presently only reports status when we:
// * 1. Place an obstacle
// * 2. Drag an obstacle
// * 3. Change Face of Obstacle
// */
//object RobotProtocol {
//    private val mapUnit: Int = 1;
//
//    // Function to Parse Message
//    fun validateProtocolMessage(raw: String): Boolean {
//        return RobotProtocolParser.parse(raw) != null
//    }
//
//    // Clear the Board
//    fun clear(): String = "CLEAR"
//
//    /**
//     * Message Updates relating to Obstacles
//     */
//    // Obstacle Placement and Movement (as we will move the obstacles around)
//    fun upsertObstacle(obstacle: Obstacle): String {
//        // Returns STATUS, Type of Action, Obstacle Id, CurrX, currY, faceDirection
//        return "STATUS, OBSTACLE PLACEMENT, " +
//                "${obstacle.obstacleId}, " +
//                "${obstacle.x * mapUnit}, " +
//                "${obstacle.y * mapUnit}, " +
//                obstacle.faceDir.name
//    }
//
//    // Obstacle Deletion
//    fun removeObstacle(obstacle: Obstacle): String {
//        // Returns STATUS, Action Type. obstacleId, currX, currY, faceDir are no longer relevant
//        return "STATUS, OBSTACLE DELETION, " +
//                "${obstacle.obstacleId}, -1, -1, -1"
//        // return "STATUS, OBSTACLE DELETION, ${o.obstacleId} , ${o.x * 10}, ${o.y * 10},-1"
//    }
//
//    // Change Obstacle Face Orientation
//    fun changeObstacleOrientation(
//        obstacle: Obstacle,
//        oldFaceDir: FaceDir,
//        newFaceDir: FaceDir
//    ): String {
//        // Returns STATUS, Type of Action, Obstacle Id, currX, currY, oldFaceDir, newFaceDir
//        return "STATUS, OBSTACLE ORIENTATION, " +
//                "${obstacle.obstacleId}, " +
//                "${obstacle.x * mapUnit}, " +
//                "${obstacle.y * mapUnit}, " +
//                "${oldFaceDir.name}, " +
//                newFaceDir.name
//    }
//
//
//
//    // Send all obstacles (sync with RPI)
//    fun sendObstacleList(obstacles: List<Obstacle>): String {
//        if (obstacles.isEmpty()) {
//            // Return STATUS, Action Type, numObstacles
//            return "STATUS, OBSTACLE LIST, 0"
//        }
//
//        // Create the payload of obstacles
//        val payload = obstacles.joinToString(" | ") { o ->
//            // Each Obstacle will have ID, currX, currY, curr faceDir
//            "${o.obstacleId}; " +
//            "${o.x * mapUnit}; " +
//            "${o.y * mapUnit}; " +
//            o.faceDir.name
//        }
//
//        // Return STATUS, Action Type, numObstacles, Obstacle Payload
//        return "STATUS, OBSTACLE LIST, ${obstacles.size}, $payload"
//    }
//
//    /**
//     * Message Updates for Robot Movement
//     */
//    // Initial Robot Placement
//    fun placeRobot(robotPosition: RobotPosition): String {
//        return "STATUS, ROBOT PLACEMENT, " +
//                "${robotPosition.x * mapUnit}, " +
//                "${robotPosition.y * mapUnit}, " +
//                robotPosition.faceDir.name
//    }
//
//    // Moving the Robot
//    fun moveRobot(robotPosition: RobotPosition): String {
//        return "STATUS, ROBOT MOVE, " +
//                "${robotPosition.x * mapUnit}, " +
//                "${robotPosition.y * mapUnit}, " +
//                robotPosition.faceDir.name
//    }
//
//    // Changing the Face of the Robot
//    fun changeRobotOrientation(
//        robotPosition: RobotPosition,
//        newFaceDir: FaceDir
//    ): String {
//        return "STATUS, ROBOT ORIENTATION, " +
//                "${robotPosition.x * mapUnit}, " +
//                "${robotPosition.y * mapUnit}, " +
//                "${robotPosition.faceDir.name}, " +
//                newFaceDir.name
//    }
//}
//
///**
// * Streamed Messages can only be of the Robot Protocol Format.
// *
// * This functions helps to ensure the streamed messages cannot be
// */
//
//private val ALLOWED_OPERATIONS = setOf(
//    "OBSTACLE PLACEMENT",
//    "OBSTACLE DELETION",
//    "OBSTACLE ORIENTATION",
//    "OBSTACLE LIST",
//    "ROBOT PLACEMENT",
//    "ROBOT MOVE",
//    "ROBOT ORIENTATION"
//)
//
////fun parseProtocolMessage(raw: String): ParsedProtocolMessage? {
////    // Remove whitespaces
////    val message = raw.trim()
////
////    // Special-case CLEAR
////    if (message.equals("CLEAR", ignoreCase = true)) {
////        return ParsedProtocolMessage(
////            action = "CLEAR",
////            payload = ""
////        )
////    }
////
////    // All other operations must start with STATUS,
////    if (!message.startsWith(PREFIX, ignoreCase = true)) {
////        return null
////    }
////
////    // Remove "STATUS," and normalize spacing
////    val body = message
////        .substringAfter(PREFIX)
////        .trim()
////        .replace(Regex("\\s+"), " ")
////
////    // Split once: ACTION, payload
////    val firstComma = body.indexOf(',')
////    if (firstComma == -1) {
////        return null
////    }
////
////    val action = body.substring(0, firstComma).trim().uppercase()
////    val payload = body.substring(firstComma + 1).trim()
////
////    // Validate action
////    if (action !in ALLOWED_OPERATIONS) {
////        return null
////    }
////
////    return ParsedProtocolMessage(
////        action = action,
////        payload = payload
////    )
////}