package com.survivai.survivai.game.colosseum.state

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.survivai.survivai.common.msToMMSS
import com.survivai.survivai.game.colosseum.entity.ColosseumPlayer
import com.survivai.survivai.game.colosseum.entity.initializePositions
import com.survivai.survivai.game.colosseum.world.ColosseumWorld
import kotlin.collections.plus
import kotlin.math.max
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

sealed interface GameState {
    data object WaitingForPlayers : GameState  // 플레이어 등록 대기
    data class Playing(val startTime: Long) : GameState  // 게임 진행 중
    data class Ended(val statsList: List<List<StatCell>>, val titleList: List<MVPTitleCard>) : GameState  // 게임 종료
}

data class MVPTitleCard(
    val title: String,
    val desc: String,
    val players: List<StatCell>,
)

data class StatCell(
    val stat: String,
    val color: Color? = null,
)

object ColosseumInfo {

    // 게임 초기화됨
    var initialized = false
        private set

    // World 초기화 여부
    private var worldInitialized = false

    // 엔티티
    var players = emptyList<ColosseumPlayer>()
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
    val itemUpdateState: State<Boolean> get() = LogManager.itemUpdateState

    // 로그 리스트
    val logEntries: List<Log> get() = LogManager.logEntries

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
    fun setPlayers(newList: List<ColosseumPlayer>) {
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
            ColosseumPlayer(
                name = player.name,
                color = player.color,
                radius = player.radius,
                startHp = defaultHp,
                ripIcons = player.ripIcons,
            )
        }

        // 게임 상태 리셋
        _gameState.value = GameState.Playing(Clock.System.now().toEpochMilliseconds())
        LogManager.clear()

        // 플레이어 재설정 및 재초기화
        players = newPlayers
        initialized = false
        tryInitialize()

        // recomposition event
        LogManager.triggerItemUpdate()
    }

    fun reset() {
        initialized = false
        worldInitialized = false  // World도 재초기화 필요
        players = emptyList()
        defaultHp = 3  // HP 초기화
        LogManager.clear()

        // 게임 상태를 대기 상태로
        _gameState.value = GameState.WaitingForPlayers

        // recomposition event
        LogManager.triggerItemUpdate()
    }

    fun addLog(log: Log) {
        LogManager.addNewLog(log)

        // recomposition event
        LogManager.triggerItemUpdate()
    }

    // 게임이 끝났을 때만 호출
    fun updateGameSet() {
        val gameState = gameState.value as? GameState.Playing ?: return

        val statsList = calculateTotalScore(gameState)
        val titleList = calculateTitles(statsList)
        _gameState.value = GameState.Ended(statsList, titleList)
    }

    @OptIn(ExperimentalTime::class)
    private fun calculateTotalScore(playingState: GameState.Playing): List<List<StatCell>> {
        val startTime = playingState.startTime
        val endTime = Clock.System.now().toEpochMilliseconds()
        val totalPlayTime = endTime - startTime
        val firstPlayerSurvivePoint = totalPlayTime + 60000

        val title = listOf(listOf(
            StatCell("NAME"),
            StatCell("ATTACK"),
            StatCell("KILL"),
            StatCell("SURVIVE"),
            StatCell("COMBO"),
            StatCell("총점"),
        ))

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
                StatCell(it.name, color = it.color),
                StatCell(it.attackPoint.toString()),
                StatCell(it.killPoint.toString()),
                StatCell(surviveTimeStr),
                StatCell(it.maxComboPoint.toString()),
                StatCell(score.toInt().toString()),
            )
        }.sortedByDescending {
            it.last().stat.toInt()
        }
    }

    private fun calculateTitles(statsList: List<List<StatCell>>): List<MVPTitleCard> {
        if (statsList.size <= 1) return emptyList() // 헤더만 있거나 비어있음

        val titles = mutableListOf<MVPTitleCard>()

        // 1등 (이미 score 기준으로 정렬되어 있으므로 첫 번째가 1등)
        val firstPlace = statsList[1].take(1) // NAME 컬럼
        titles.add(MVPTitleCard("🏆 1등", "결국 점수 높은 게 1등이야", firstPlace))

        // 반복문으로 나머지 칭호 수집
        var maxKill = -1
        val killChampions = mutableListOf<StatCell>()
        val quickExits = mutableListOf<StatCell>() // 10초 이내 사망
        val pacifists = mutableListOf<StatCell>() // 타격 0회

        for (i in 1 until statsList.size) {
            val row = statsList[i]
            val name = row[0]
            val attack = row[1].stat.toIntOrNull() ?: 0
            val kill = row[2].stat.toIntOrNull() ?: 0
            val surviveTime = row[3].stat // "MM:SS" 형식

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
            titles.add(MVPTitleCard("⭐️ GOSU", "해골 수집가 (최다결정타)", killChampions ))
        }

        // 최단기퇴물 칭호 추가
        if (quickExits.isNotEmpty()) {
            titles.add(MVPTitleCard("⏱️ 최단기퇴물", "스폰킬도 실력 (10초 이내로 사망)", quickExits))
        }

        // 평화주의자 칭호 추가
        if (pacifists.isNotEmpty()) {
            titles.add(MVPTitleCard("🕊️ 평화주의자", "적을 못 맞힌 게 아니다… 바람을 맞힌 거다. (어택 횟수 0회)", pacifists))
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