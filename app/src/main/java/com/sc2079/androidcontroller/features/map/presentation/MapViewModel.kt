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

class MapViewModel(
    private val repo: MapRepository,
    private val resetMap: ResetMapUseCase = ResetMapUseCase(),
    private val addObstacle: AddObstacleUseCase = AddObstacleUseCase(),
    private val moveObstacle: MoveObstacleUseCase = MoveObstacleUseCase(),
    private val removeObstacle: RemoveObstacleUseCase = RemoveObstacleUseCase(),
    private val setObstacleFace: SetObstacleFaceUseCase = SetObstacleFaceUseCase(),
    private val setRobotPose: SetRobotPoseUseCase = SetRobotPoseUseCase(),
    private val updateTargetId: UpdateTargetIdUseCase = UpdateTargetIdUseCase()
) : ViewModel() {

    private var snapshot: MapSnapshot = resetMap()

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState

    init {
        refreshSavedMaps()
        publish()
    }

    fun setEditMode(mode: MapEditMode) {
        _uiState.update { it.copy(editMode = mode) }
    }

    fun resetAll() {
        snapshot = resetMap()
        _uiState.update { it.copy(robotStatus = "", editMode = MapEditMode.Default) }
        publish()
    }

    fun onTapCell(x: Int, y: Int): Obstacle? {
        return when (_uiState.value.editMode) {
            MapEditMode.SetStart -> {
                val dir = snapshot.robotPose?.dir ?: FaceDir.UP
                snapshot = setRobotPose(snapshot, x, y, dir)
                publish()
                null
            }
            MapEditMode.PlaceObstacle -> {
                val before = snapshot
                snapshot = addObstacle(snapshot, x, y)
                publish()
                // return newly added (if any)
                snapshot.obstacles.firstOrNull { it.no == snapshot.nextObstacleNo - 1 && before.nextObstacleNo != snapshot.nextObstacleNo }
            }
            MapEditMode.ChangeObstacleFace,
            MapEditMode.DragObstacle,
            MapEditMode.Default -> null
        }
    }

    fun onDragObstacle(obstacleNo: Int, x: Int, y: Int) : Obstacle? {
        val before = snapshot
        snapshot = moveObstacle(snapshot, obstacleNo, x, y)
        publish()
        return if (before != snapshot) snapshot.obstacles.firstOrNull { it.no == obstacleNo } else null
    }

    fun removeObstacleByNo(obstacleNo: Int): Obstacle? {
        val existing = snapshot.obstacles.firstOrNull { it.no == obstacleNo } ?: return null
        snapshot = removeObstacle(snapshot, obstacleNo)
        publish()
        return existing
    }

    fun setObstacleFace(obstacleNo: Int, face: FaceDir): Obstacle? {
        val before = snapshot
        snapshot = setObstacleFace(snapshot, obstacleNo, face)
        publish()
        return if (before != snapshot) snapshot.obstacles.firstOrNull { it.no == obstacleNo } else null
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
                    s = setRobotPose(s, e.x, e.y, e.dir)
                }
                is RobotInboundEvent.TargetEvent -> {
                    // Java used cmd[1]-1 for internal; checklist uses obstacle number directly.
                    // We follow checklist: obstacleNo matches displayed obstacle number.
                    s = updateTargetId(s, e.obstacleNo, e.targetId)
                }
            }
        }

        snapshot = s
        status?.let { _uiState.update { it.copy(robotStatus = it.robotStatus.ifBlank { "" }.let { _ -> status }) } }
        publish()
    }

    fun saveCurrentMap(name: String) {
        viewModelScope.launch {
            repo.saveSnapshot(name.trim(), snapshot)
            refreshSavedMaps()
        }
    }

    fun loadMap(name: String) {
        viewModelScope.launch {
            val loaded = repo.loadSnapshot(name) ?: return@launch
            snapshot = loaded
            publish()
        }
    }

    fun deleteMap(name: String) {
        viewModelScope.launch {
            repo.deleteSnapshot(name)
            refreshSavedMaps()
        }
    }

    private fun refreshSavedMaps() {
        viewModelScope.launch {
            val names = repo.listSnapshots()
            _uiState.update { it.copy(savedMaps = names) }
        }
    }

    private fun publish() {
        _uiState.update {
            it.copy(
                robotPose = snapshot.robotPose,
                obstacles = snapshot.obstacles.sortedBy { o -> o.no }
            )
        }
    }
}
