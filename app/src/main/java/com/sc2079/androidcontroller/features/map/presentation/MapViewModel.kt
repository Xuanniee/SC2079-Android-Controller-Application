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
    /**
     *
     */
    private var snapshot: MapSnapshot = resetMap()

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
        snapshot = resetMap()
        _uiState.update { it.copy(robotStatus = "", editMode = MapEditMode.Cursor) }
        publish()
    }

    fun onTapCell(x: Int, y: Int): Obstacle? {
        return when (_uiState.value.editMode) {
            MapEditMode.SetStart -> {
                val dir = snapshot.robotPosition?.faceDir ?: FaceDir.UP
                snapshot = setRobotPosition(snapshot, x, y, dir)
                publish()
                null
            }
            MapEditMode.PlaceObstacle -> {
                val before = snapshot
                snapshot = addObstacle(snapshot, x, y)
                publish()
                // return newly added (if any)
                snapshot.obstacles.firstOrNull { it.obstacleId == snapshot.nextObstacleId - 1 && before.nextObstacleId != snapshot.nextObstacleId }
            }
            MapEditMode.ChangeObstacleFace,
            MapEditMode.DragObstacle,
            MapEditMode.Cursor -> null
        }
    }

    fun onDragObstacle(obstacleNo: Int, x: Int, y: Int) : Obstacle? {
        val before = snapshot
        snapshot = moveObstacle(snapshot, obstacleNo, x, y)
        publish()
        return if (before != snapshot) snapshot.obstacles.firstOrNull { it.obstacleId == obstacleNo } else null
    }

    fun removeObstacleByNo(obstacleNo: Int): Obstacle? {
        val existing = snapshot.obstacles.firstOrNull { it.obstacleId == obstacleNo } ?: return null
        snapshot = removeObstacle(snapshot, obstacleNo)
        publish()
        return existing
    }

    fun setObstacleFace(obstacleNo: Int, face: FaceDir): Obstacle? {
        val before = snapshot
        snapshot = setObstacleFace(snapshot, obstacleNo, face)
        publish()
        return if (before != snapshot) snapshot.obstacles.firstOrNull { it.obstacleId == obstacleNo } else null
    }

    fun applyRobotMessage(raw: String) {
        val events = RobotMessageParser.parse(raw)
        if (events.isEmpty()) return

        var s = snapshot
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
                    s = updateObstacleTargetId(s, e.obstacleNo, e.targetId)
                }
            }
        }

        snapshot = s
        status?.let { _uiState.update { it.copy(robotStatus = it.robotStatus.ifBlank { "" }.let { _ -> status }) } }
        publish()
    }

    fun saveCurrentMap(name: String) {
        viewModelScope.launch {
            mapRepository.saveMapSnapshot(name.trim(), snapshot)
            refreshSavedMaps()
        }
    }

    fun loadMap(name: String) {
        viewModelScope.launch {
            val loaded = mapRepository.loadMapSnapshot(name) ?: return@launch
            snapshot = loaded
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
                robotPosition = snapshot.robotPosition,
                obstacles = snapshot.obstacles.sortedBy { o -> o.obstacleId }
            )
        }
    }
}
