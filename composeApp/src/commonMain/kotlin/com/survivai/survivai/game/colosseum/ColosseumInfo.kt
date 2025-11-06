package com.survivai.survivai.game.colosseum

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.survivai.survivai.game.colosseum.entity.Player
import com.survivai.survivai.game.colosseum.entity.Player.Companion.initializePositions
import com.survivai.survivai.game.colosseum.world.ColosseumWorld

object ColosseumInfo {

    // 게임 초기화됨
    var initialized = false
        private set

    // 엔티티
    var players = emptyList<Player>()
        private set

    // 게임 셋
    var winnerAnnounced = false
        private set

    // 게임 실행 상태
    private val _isGameRunning = mutableStateOf(true)
    val isGameRunning: State<Boolean> get() = _isGameRunning

    // 월드 객체 TODO : 다른 world 유형으로 교체 가능하도록 변경
    val world = ColosseumWorld()

    // 로그 상태 추적
    private val _fullUpdateState = mutableStateOf(false)
    val fullUpdateState: State<Boolean> get() = _fullUpdateState
    private val _itemUpdateState = mutableStateOf(false)
    val itemUpdateState: State<Boolean> get() = _itemUpdateState

    // 로그 리스트
    private val _logEntries = mutableListOf<String>()
    val logEntries: List<String> get() = _logEntries

    // Viewport 크기 캐싱
    private var viewportWidth = 0f
    private var viewportHeight = 0f

    fun setViewportSize(width: Float, height: Float) {
        viewportWidth = width
        viewportHeight = height
        tryInitialize()
    }

    fun setPlayers(newList: List<Player>) {
        players = newList
        initialized = false  // 재초기화 필요
        tryInitialize()
    }

    private fun tryInitialize() {
        if (initialized) return
        if (players.isEmpty()) return
        if (viewportWidth <= 0 || viewportHeight <= 0) return

        world.buildMap(viewportWidth, viewportHeight)
        players.initializePositions(viewportWidth, viewportHeight)
        initialized = true
    }

    fun clear() {
        initialized = false
        players = emptyList()
        winnerAnnounced = false
        _isGameRunning.value = true  // 게임 재시작
        _logEntries.clear()

        // recomposition event
        _fullUpdateState.value = !_fullUpdateState.value
        _itemUpdateState.value = !_itemUpdateState.value
    }

    fun addLog(message: String) {
        _logEntries.add(0, message)
        // Keep a reasonable cap
        if (_logEntries.size > 200) {
            // remove oldest extra elements to keep list bounded
            repeat(_logEntries.size - 200) { _logEntries.removeAt(0) }
        }

        // recomposition event
        _itemUpdateState.value = !_itemUpdateState.value
    }

    fun updateGameSet() {
        winnerAnnounced = true
        _isGameRunning.value = false  // 게임 중단
    }
}