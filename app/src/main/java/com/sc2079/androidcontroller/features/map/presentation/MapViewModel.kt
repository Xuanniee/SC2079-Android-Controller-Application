package com.sc2079.androidcontroller.features.map.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sc2079.androidcontroller.features.map.domain.model.FaceDir
import com.sc2079.androidcontroller.features.map.domain.model.MapEditMode
import com.sc2079.androidcontroller.features.map.domain.model.Obstacle
import com.sc2079.androidcontroller.features.map.domain.repository.MapRepository
import com.sc2079.androidcontroller.features.map.domain.model.MapSnapshot
import com.sc2079.androidcontroller.features.map.domain.usecase.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel Layer for the Map Module
 *
 * - Hold the Ui facing state for Map
 * - Translate domains state to uistaet and expose it for UI to see
 */
class MapViewModel(
    private val mapRepository: MapRepository,
    // UseCase Functions
    private val resetMap: ResetMapUseCase = ResetMapUseCase(),
    private val addObstacle: AddObstacleUseCase = AddObstacleUseCase(),
    private val moveObstacle: MoveObstacleUseCase = MoveObstacleUseCase(),
    private val removeObstacle: RemoveObstacleUseCase = RemoveObstacleUseCase(),
    private val setObstacleFace: SetObstacleFaceUseCase = SetObstacleFaceUseCase(),
    private val setRobotPosition: SetRobotPositionUseCase = SetRobotPositionUseCase(),
    private val updateObstacleTargetId: UpdateObstacleTargetIdUseCase = UpdateObstacleTargetIdUseCase()
) : ViewModel() {
    // SSOT for Map's data model e.g. list of obstacles
    private var mapSnapshot: MapSnapshot = resetMap()
    // StateFlow for UI to observe to rerender
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    init {
        refreshSavedMaps()
        publish()
    }

    /**
     *
     */
    fun setEditMode(mode: MapEditMode) {
        _uiState.update { it.copy(editMode = mode) }
    }

    fun resetAll() {
        // Reset the entire Map
        mapSnapshot = resetMap()
        // Update UI and Propagate
        _uiState.update {
            it.copy(robotStatus = "", editMode = MapEditMode.Cursor, robotPosition = null)
        }
        publish()
    }

    fun onTapCell(x: Int, y: Int): Obstacle? {
        return when (_uiState.value.editMode) {
            MapEditMode.SetStart -> {
                val dir = mapSnapshot.robotPosition?.faceDir ?: FaceDir.NORTH
                mapSnapshot = setRobotPosition(mapSnapshot, x, y, dir)
                publish()
                null
            }
            MapEditMode.PlaceObstacle -> {
                // Store existing obstacles id into a hashset
                val existingObstacleIds = mapSnapshot.obstacles.map { it.obstacleId }.toSet()

                // Try adding the obstacle and update UI
                mapSnapshot = addObstacle(mapSnapshot, x, y)
                publish()

                // return newly added obstacle if added else null
                mapSnapshot.obstacles.firstOrNull { it.obstacleId !in existingObstacleIds }
//                mapSnapshot.obstacles.firstOrNull { it.obstacleId == mapSnapshot.nextObstacleId - 1 && before.nextObstacleId != mapSnapshot.nextObstacleId }
            }
            MapEditMode.ChangeObstacleFace,
            MapEditMode.DragObstacle,
            MapEditMode.Cursor -> null
        }
    }

    fun onDragObstacle(obstacleNo: Int, x: Int, y: Int) : Obstacle? {
        val before = mapSnapshot
        mapSnapshot = moveObstacle(mapSnapshot, obstacleNo, x, y)
        publish()
        return if (before != mapSnapshot) mapSnapshot.obstacles.firstOrNull { it.obstacleId == obstacleNo } else null
    }

    fun removeObstacleByNo(obstacleNo: Int): Obstacle? {
        val existing = mapSnapshot.obstacles.firstOrNull { it.obstacleId == obstacleNo } ?: return null
        mapSnapshot = removeObstacle(mapSnapshot, obstacleNo)
        publish()
        return existing
    }

    fun clearRobotPosition() {
        mapSnapshot = mapSnapshot.copy(robotPosition = null)
        publish()
    }

    fun setObstacleFace(obstacleNo: Int, face: FaceDir): Obstacle? {
        val before = mapSnapshot
        mapSnapshot = setObstacleFace(mapSnapshot, obstacleNo, face)
        publish()
        return if (before != mapSnapshot) mapSnapshot.obstacles.firstOrNull { it.obstacleId == obstacleNo } else null
    }

    fun applyRobotMessage(raw: String) {
        val events = RobotMessageParser.parse(raw)
        if (events.isEmpty()) return

        var s = mapSnapshot
        var status: String? = null

        for (e in events) {
            when (e) {
                is RobotInboundEvent.StatusEvent -> status = e.status
                is RobotInboundEvent.RobotPoseEvent -> {
                    s = setRobotPosition(s, e.x, e.y, e.dir)
                }
                is RobotInboundEvent.TargetEvent -> {
                    // Java used cmd[1]-1 for internal; checklist uses obstacle number directly.
                    // We follow checklist: obstacleNo matches displayed obstacle number.
                    s = updateObstacleTargetId(s, e.obstacleId, e.targetId)
                }
            }
        }

        mapSnapshot = s
        status?.let { _uiState.update { it.copy(robotStatus = it.robotStatus.ifBlank { "" }.let { _ -> status }) } }
        publish()
    }

    fun saveCurrentMap(name: String) {
        viewModelScope.launch {
            mapRepository.saveMapSnapshot(name.trim(), mapSnapshot)
            refreshSavedMaps()
        }
    }

    fun loadMap(name: String) {
        viewModelScope.launch {
            val loaded = mapRepository.loadMapSnapshot(name) ?: return@launch
            mapSnapshot = loaded
            publish()
        }
    }

    fun deleteMap(name: String) {
        viewModelScope.launch {
            mapRepository.deleteMapSnapshot(name)
            refreshSavedMaps()
        }
    }

    private fun refreshSavedMaps() {
        viewModelScope.launch {
            val names = mapRepository.listMapSnapshots()
            _uiState.update { it.copy(savedMaps = names) }
        }
    }

    private fun publish() {
        _uiState.update {
            it.copy(
                robotPosition = mapSnapshot.robotPosition,
                obstacles = mapSnapshot.obstacles.sortedBy { obstacle -> obstacle.obstacleId }
            )
        }
    }
}
