package com.survivai.survivai.game.colosseum

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.survivai.survivai.game.colosseum.entity.Player
import com.survivai.survivai.game.colosseum.entity.initializePositions
import com.survivai.survivai.game.colosseum.world.ColosseumWorld
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

sealed interface GameState {
    data object WaitingForPlayers : GameState  // 플레이어 등록 대기
    data class Playing(val startTime: Long) : GameState  // 게임 진행 중
    data object Ended : GameState  // 게임 종료
}

object ColosseumInfo {

    // 게임 초기화됨
    var initialized = false
        private set

    // World 초기화 여부
    private var worldInitialized = false

    // 엔티티
    var players = emptyList<Player>()
        private set

    // 기본 HP 설정 (1~10)
    var defaultHp = 3
        private set

    // 게임 상태
    private val _gameState = mutableStateOf<GameState>(GameState.WaitingForPlayers)
    val gameState: State<GameState> get() = _gameState

    // 월드 객체 TODO : 다른 world 유형으로 교체 가능하도록 변경
    val world = ColosseumWorld()

    // 로그 상태 추적
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
        initializeWorld()
        tryInitialize()
    }

    @OptIn(ExperimentalTime::class)
    fun setPlayers(newList: List<Player>) {
        players = newList
        initialized = false  // 재초기화 필요
        _gameState.value = GameState.Playing(Clock.System.now().toEpochMilliseconds())
        tryInitialize()
    }

    fun setDefaultHp(hp: Int) {
        defaultHp = hp.coerceIn(1, 10)
    }

    private fun initializeWorld() {
        if (worldInitialized) return
        if (viewportWidth <= 0 || viewportHeight <= 0) return

        world.buildMap(viewportWidth, viewportHeight)
        worldInitialized = true
    }

    private fun tryInitialize() {
        if (initialized) return
        if (players.isEmpty()) return
        if (viewportWidth <= 0 || viewportHeight <= 0) return

        players.initializePositions(viewportWidth, viewportHeight)
        initialized = true
    }

    @OptIn(ExperimentalTime::class)
    fun restart() {
        // 현재 플레이어 정보로 새 플레이어 생성 (HP 초기화)
        val newPlayers = players.map { player ->
            Player(
                name = player.name,
                color = player.color,
                radius = player.radius,
                startHp = defaultHp
            )
        }

        // 게임 상태 리셋
        _gameState.value = GameState.Playing(Clock.System.now().toEpochMilliseconds())
        _logEntries.clear()

        // 플레이어 재설정 및 재초기화
        players = newPlayers
        initialized = false
        tryInitialize()

        // recomposition event
        _itemUpdateState.value = !_itemUpdateState.value
    }

    fun reset() {
        initialized = false
        worldInitialized = false  // World도 재초기화 필요
        players = emptyList()
        defaultHp = 3  // HP 초기화
        _logEntries.clear()

        // 게임 상태를 대기 상태로
        _gameState.value = GameState.WaitingForPlayers

        // recomposition event
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
        _gameState.value = GameState.Ended
    }

    // 타격 횟수
    fun updatePlayerAttackPoint(name: String) {
        players = players.map {
            it.apply {
                if (this.name == name) {
                    attackPoint += 1
                }
            }
        }
    }

    // 결정타 횟수
    fun updatePlayerKillPoint(name: String) {
        players = players.map {
            it.apply {
                if (this.name == name) {
                    killPoint += 1
                }
            }
        }
    }

    // 생존시간
    fun updatePlayerDeathTime(name: String, deathTime: Long) {
        val gameState = gameState.value as? GameState.Playing ?: return

        players = players.map {
            it.apply {
                if (this.name == name) {
                    this.deathTime = deathTime - gameState.startTime
                }
            }
        }
    }

    // 최장 콤보
    fun updatePlayerComboPoint(name: String, damaged: Boolean) {
        players = players.map {
            it.apply {
                if (this.name == name) {
                    val temp = comboPoint
                    comboPoint = if (damaged) { // 얘가 맞음
                        0
                    } else { // 얘가 때림
                        comboPoint + 1
                    }
                    maxComboPoint = arrayOf(maxComboPoint, temp, comboPoint).max()
                }
            }
        }
    }
}