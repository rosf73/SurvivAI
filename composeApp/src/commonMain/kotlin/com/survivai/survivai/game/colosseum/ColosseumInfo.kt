package com.survivai.survivai.game.colosseum

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.survivai.survivai.game.colosseum.entity.Player
import com.survivai.survivai.game.colosseum.world.ColosseumWorld

object ColosseumInfo {

    // 엔티티
    var players = emptyList<Player>()
        private set

    // 게임 셋
    var winnerAnnounced = false
        private set

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

    fun clear() {
        players = emptyList()
        winnerAnnounced = false
        _logEntries.clear()

        // recomposition event
        _fullUpdateState.value = !_fullUpdateState.value
    }

    fun setPlayers(newList: List<Player>) {
        players = newList
    }

    fun addLog(message: String) {
        _logEntries.add(message)
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
    }
}