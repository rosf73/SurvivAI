package com.survivai.survivai.game.colosseum

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.survivai.survivai.common.msToMMSS
import com.survivai.survivai.game.colosseum.entity.Player
import com.survivai.survivai.game.colosseum.entity.initializePositions
import com.survivai.survivai.game.colosseum.world.ColosseumWorld
import kotlin.collections.plus
import kotlin.math.max
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

sealed interface GameState {
    data object WaitingForPlayers : GameState  // 플레이어 등록 대기
    data class Playing(val startTime: Long) : GameState  // 게임 진행 중
    data class Ended(val statsList: List<List<String>>, val titleList: List<PlayerTitle>) : GameState  // 게임 종료
}

data class PlayerTitle(
    val title: String,
    val desc: String,
    val players: String,
)

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

    // 게임이 끝났을 때만 호출
    fun updateGameSet() {
        val gameState = gameState.value as? GameState.Playing ?: return

        val statsList = calculateTotalScore(gameState)
        val titleList = calculateTitles(statsList)
        _gameState.value = GameState.Ended(statsList, titleList)
    }

    @OptIn(ExperimentalTime::class)
    private fun calculateTotalScore(playingState: GameState.Playing): List<List<String>> {
        val startTime = playingState.startTime
        val endTime = Clock.System.now().toEpochMilliseconds()
        val totalPlayTime = endTime - startTime
        val firstPlayerSurvivePoint = totalPlayTime + 60000

        val title = listOf(listOf("NAME", "ATTACK", "KILL", "SURVIVE", "COMBO", "결과"))

        // 순위 기준값 먼저 계산
        var totalAttackPoint = 0F
        var totalSurvivePoint = 0L
        for (p in players) {
            totalAttackPoint += p.attackPoint
            totalSurvivePoint += if (p.deathTime == 0L) firstPlayerSurvivePoint else p.deathTime - startTime
        }

        return title + players.map {
            val surviveTime = if (it.deathTime == 0L) firstPlayerSurvivePoint else it.deathTime - startTime
            val surviveTimeStr =
                if (it.deathTime == 0L) "${totalPlayTime.msToMMSS()}(+01:00)"
                else surviveTime.msToMMSS()
            val score = (it.attackPoint / totalAttackPoint) * 100 + (surviveTime.toFloat() / totalSurvivePoint) * 100

            listOf(
                it.name,
                it.attackPoint.toString(),
                it.killPoint.toString(),
                surviveTimeStr,
                it.maxComboPoint.toString(),
                score.toInt().toString(),
            )
        }.sortedByDescending {
            it.last().toInt()
        }
    }

    private fun calculateTitles(statsList: List<List<String>>): List<PlayerTitle> {
        if (statsList.size <= 1) return emptyList() // 헤더만 있거나 비어있음

        val titles = mutableListOf<PlayerTitle>()

        // 1등 (이미 score 기준으로 정렬되어 있으므로 첫 번째가 1등)
        val firstPlace = statsList[1][0] // NAME 컬럼
        titles.add(PlayerTitle("🏆 1등", "결국 점수 높은 게 1등이야", firstPlace))

        // 반복문으로 나머지 칭호 수집
        var maxKill = -1
        val killChampions = mutableListOf<String>()
        val quickExits = mutableListOf<String>() // 10초 이내 사망
        val pacifists = mutableListOf<String>() // 타격 0회

        for (i in 1 until statsList.size) {
            val row = statsList[i]
            val name = row[0]
            val attack = row[1].toIntOrNull() ?: 0
            val kill = row[2].toIntOrNull() ?: 0
            val surviveTime = row[3] // "MM:SS" 형식

            // GOSU (kill 최대값)
            when {
                kill > maxKill -> {
                    maxKill = kill
                    killChampions.clear()
                    killChampions.add(name)
                }
                kill == maxKill && maxKill > 0 -> {
                    killChampions.add(name)
                }
            }

            // 최단기퇴물 (10초 이내 사망)
            if (surviveTime <= "00:10") {
                quickExits.add(name)
            }

            // 평화주의자 (타격 0회)
            if (attack == 0) {
                pacifists.add(name)
            }
        }

        // GOSU 칭호 추가
        if (killChampions.isNotEmpty() && maxKill > 0) {
            titles.add(PlayerTitle("⭐️ GOSU", "해골 수집가 (최다결정타)", killChampions.joinToString(", ")))
        }

        // 최단기퇴물 칭호 추가
        if (quickExits.isNotEmpty()) {
            titles.add(PlayerTitle("⏱️ 최단기퇴물", "스폰킬도 실력 (10초 이내로 사망)", quickExits.joinToString(", ")))
        }

        // 평화주의자 칭호 추가
        if (pacifists.isNotEmpty()) {
            titles.add(PlayerTitle("🕊️ 평화주의자", "적을 못 맞힌 게 아니다… 바람을 맞힌 거다. (어택 횟수 0회)", pacifists.joinToString(", ")))
        }

        return titles
    }

    // 타격 횟수
    fun updatePlayerAttackPoint(name: String) {
        players = players.map {
            it.apply {
                if (this.name == name) {
                    attackPoint += 1
                    comboPoint += 1
                    maxComboPoint = max(maxComboPoint, comboPoint)
                }
            }
        }
    }

    // 결정타 횟수, 탈락자 생존시간
    @OptIn(ExperimentalTime::class)
    fun updatePlayerKillPoint(killerName: String, victimName: String) {
        players = players.map {
            it.apply {
                if (name == killerName) {
                    killPoint += 1
                } else if (name == victimName) {
                    deathTime = Clock.System.now().toEpochMilliseconds()
                }
            }
        }
    }

    // 최장 콤보
    fun resetPlayerComboPoint(name: String) {
        players = players.map {
            it.apply {
                if (this.name == name) {
                    comboPoint = 0
                }
            }
        }
    }
}