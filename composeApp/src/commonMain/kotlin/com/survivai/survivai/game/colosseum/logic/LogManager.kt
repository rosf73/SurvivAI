package com.survivai.survivai.game.colosseum.logic

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.survivai.survivai.game.Entity

sealed interface Log {
    data class System(val msg: String) : Log

    data class Attack(val attacker: Entity, val victim: Entity) : Log
    data class Kill(val killer: Entity, val victim: Entity) : Log
    data class FirstBlood(val killer: Entity, val victim: Entity) : Log
    data class Speech(val actor: Entity, val msg: String) : Log
}

object LogManager {

    // 상수
    private const val COUNT_LOG_MAX = 200

    // 로그 상태 추적
    private val _itemUpdateState = mutableStateOf(false)
    val itemUpdateState: State<Boolean> get() = _itemUpdateState

    // 로그 리스트
    private val _logEntries = mutableListOf<Log>()
    val logEntries: List<Log> get() = _logEntries

    fun clear() {
        _logEntries.clear()
    }

    fun triggerItemUpdate() {
        _itemUpdateState.value = !_itemUpdateState.value
    }

    fun addNewLog(log: Log) {
        _logEntries.add(0, log)
        // Keep a reasonable cap
        if (logEntries.size > COUNT_LOG_MAX) {
            // remove oldest extra elements to keep list bounded
            repeat(logEntries.size - COUNT_LOG_MAX) {
                _logEntries.removeAt(_logEntries.size - 1)
            }
        }
    }
}