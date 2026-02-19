package com.survivai.survivai.game.colosseum.logic

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import com.survivai.survivai.common.msToMMSS
import com.survivai.survivai.game.Engine
import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.GameDrawScope
import com.survivai.survivai.game.colosseum.entity.ColosseumPlayer
import com.survivai.survivai.game.colosseum.entity.ColosseumEntityFactory
import com.survivai.survivai.game.colosseum.entity.ColosseumTouchEffect
import com.survivai.survivai.game.colosseum.entity.PlayerInitPair
import com.survivai.survivai.game.colosseum.entity.initializePositions
import com.survivai.survivai.game.colosseum.world.ColosseumWorld
import com.survivai.survivai.game.sprite.SpriteLoader
import kotlin.math.abs
import kotlin.math.max
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.survivai.survivai.game.colosseum.entity.ColosseumRunningCar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class ColosseumEngine(
    spriteLoader: SpriteLoader,
) : Engine {

    var initialized = false
        private set
    private val worldInitialized get() = world.viewportWidth > 0 && world.viewportHeight > 0

    override var entities = emptyList<Entity>()
        set(value) {
            field = value
            colosseumPlayers = value.filterIsInstance<ColosseumPlayer>()
        }
    var colosseumPlayers = emptyList<ColosseumPlayer>()
        private set

    override val world = ColosseumWorld()

    var colosseumOptions = setOf<DisasterOption>()
        private set

    private val _gameState = mutableStateOf<ColosseumState>(ColosseumState.WaitingForPlayers)
    val gameState: State<ColosseumState> get() = _gameState

    var scoreTable = listOf(emptyList<StatCell>())
        private set

    private var pendingWinnerWaitTime = 0.0

    val logUpdateState: State<Boolean> get() = LogManager.itemUpdateState

    val logEntries: List<Log> get() = LogManager.logEntries

    private val entityFactory = ColosseumEntityFactory(spriteLoader, this)

    private val spawnScope = CoroutineScope(Dispatchers.Main)
    private var carSpawnTimer = 0.0
    private var nextCarSpawnInterval = 0.0
    private var isSpawningCar = false

    fun setViewportSize(width: Float, height: Float) {
        initializeWorld(width, height)
        tryInitialize()
    }

    @OptIn(ExperimentalTime::class)
    suspend fun playGame(
        playerInitList: List<PlayerInitPair>,
        startHp: Double,
        options: Set<DisasterOption>,
    ) {
        entities = playerInitList.map { p ->
            entityFactory.createPlayer(
                name = p.name,
                color = p.color,
                startHp = startHp,
            )
        }
        colosseumOptions = options

        initialized = false  // 재초기화 필요
        _gameState.value = ColosseumState.Playing(Clock.System.now().toEpochMilliseconds())
        tryInitialize()
    }

    private fun initializeWorld(width: Float, height: Float) {
        if (worldInitialized) return
        if (width <= 0 || height <= 0) return

        world.buildMap(width, height)
    }

    private fun tryInitialize() {
        if (initialized) return
        if (colosseumPlayers.isEmpty()) return
        if (!worldInitialized) return

        colosseumPlayers.initializePositions(world)
        initialized = true
    }

    @OptIn(ExperimentalTime::class)
    fun restart() {
        // Remain only entities (HP reset)
        val newPlayers = colosseumPlayers.map { player ->
            ColosseumPlayer(
                name = player.name,
                signatureColor = player.signatureColor,
                startHp = player.startHp,
                spriteSheet = player.spriteSheet,
                gameEngine = this,
            )
        }

        // 게임 상태 리셋
        _gameState.value = ColosseumState.Playing(Clock.System.now().toEpochMilliseconds())

        // 플레이어 재설정 및 재초기화
        entities = newPlayers
        initialized = false
        tryInitialize()

        clearLog()
    }

    fun reset() {
        initialized = false
        world.buildMap(0f, 0f) // World 초기화
        entities = emptyList()

        // 게임 상태를 대기 상태로
        _gameState.value = ColosseumState.WaitingForPlayers

        clearLog()
    }

    fun onGameEvent(event: ColosseumEvent) {
        val gameState = gameState.value as? ColosseumState.Playing ?: return

        // 1. Update game stats
        when (event) {
            is ColosseumEvent.Attack -> {
                if (event.attacker is ColosseumPlayer) {
                    event.attacker.attackPoint++
                }
            }
            is ColosseumEvent.Kill -> {
                if (event.killer is ColosseumPlayer) {
                    event.killer.killPoint++
                }
            }
            is ColosseumEvent.Accident -> {

            }
        }

        // 2. Update total score
        refreshTotalScore(gameState)

        // 3. Update latest log
        val log = when (event) {
            is ColosseumEvent.Attack -> Log.Attack(event.attacker, event.victim)
            is ColosseumEvent.Kill -> {
                val isFirstBlood = colosseumPlayers.count { !it.isAlive } == 1
                if (isFirstBlood)
                    Log.FirstBlood(event.killer, event.victim)
                else
                    Log.Kill(event.killer, event.victim)
            }
            is ColosseumEvent.Accident -> Log.Speech(event.victim, "아이고야!")
        }
        addLog(log)
    }

    // 게임이 끝났을 때만 호출
    fun updateGameSet() {
        val titleList = calculateTitles(scoreTable)
        _gameState.value = ColosseumState.Ended(scoreTable, titleList)
    }

    @OptIn(ExperimentalTime::class)
    private fun refreshTotalScore(playingState: ColosseumState.Playing) {
        val startTime = playingState.startTime
        val endTime = Clock.System.now().toEpochMilliseconds()
        val totalPlayTime = endTime - startTime
        val firstPlayerSurvivePoint = totalPlayTime + 60000

        val title = listOf(listOf(
            StatCell.rowTitle("NAME"),
            StatCell.rowTitle("ATTACK"),
            StatCell.rowTitle("KILL"),
            StatCell.rowTitle("SURVIVE"),
            StatCell.rowTitle("SCORE"),
        ))

        // 순위 기준값 먼저 계산
        var totalAttackPoint = 0F
        var totalSurvivePoint = 0L
        for (p in colosseumPlayers) {
            totalAttackPoint += p.attackPoint
            totalSurvivePoint += if (p.isAlive) firstPlayerSurvivePoint else p.deathTime - startTime
        }
        if (totalAttackPoint == 0f) {
            totalAttackPoint = 1f
        }
        if (totalSurvivePoint == 0L) {
            totalSurvivePoint = 1
        }

        scoreTable = title + colosseumPlayers.map {
            val surviveTime = if (it.isAlive) firstPlayerSurvivePoint else it.deathTime - startTime
            val surviveTimeStr =
                if (it.isAlive) "${totalPlayTime.msToMMSS()}(+01:00)"
                else surviveTime.msToMMSS()
            val score = (it.attackPoint / totalAttackPoint) * 100 + (surviveTime.toFloat() / totalSurvivePoint) * 100
            val statColor = if (it.isAlive) Color.Yellow else Color.White

            listOf(
                StatCell.colLabel(it.name, color = it.signatureColor),
                StatCell(it.attackPoint.toString(), color = statColor),
                StatCell(it.killPoint.toString(), color = statColor),
                StatCell(surviveTimeStr, color = statColor),
                StatCell(score.toInt().toString(), color = statColor),
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

            // 최단기퇴물 (10초 이내 사망, 1등 제외)
            if (i > 1 && surviveTime <= "00:10") {
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

    // Touch or Click
    suspend fun onScreenTouch(x: Float, y: Float) {
        if (!initialized || gameState.value !is ColosseumState.Playing) return

        if (colosseumOptions.contains(DisasterOption.FALLING_ROCKS)) {
            entities += entityFactory.createFallingRock()
            entities += ColosseumTouchEffect(x, y, gameEngine = this)
        }
    }

    override fun update(deltaTime: Double) {
        if (world.viewportWidth <= 0 || world.viewportHeight <= 0) {
            return
        }

        // Get alive players
        val alivePlayers = colosseumPlayers.filter { it.isAlive }

        // Update all entities
        entities.forEach { it.update(deltaTime, world) }

        // Car spawning logic
        if (colosseumOptions.contains(DisasterOption.CAR_HIT_AND_RUN)) {
            val hasCar = entities.any { it is ColosseumRunningCar }
            if (!hasCar && !isSpawningCar) {
                if (nextCarSpawnInterval <= 0) {
                    // Schedule next spawn (3s ~ 20s)
                    nextCarSpawnInterval = Random.nextDouble(3.0, 20.0)
                    carSpawnTimer = 0.0
                }

                carSpawnTimer += deltaTime
                if (carSpawnTimer >= nextCarSpawnInterval) {
                    spawnCar()
                }
            } else {
                // Reset schedule when car exists or is spawning
                nextCarSpawnInterval = 0.0
                carSpawnTimer = 0.0
            }
        }

        // Add log to speech
        alivePlayers.forEachIndexed { _, p ->
            val text = p.pollJustSpeeched()
            if (text.isNotBlank()) {
                addLog(Log.Speech(p, text))
            }
        }

        // Check for winner (only once)
        if (colosseumPlayers.isNotEmpty()) {
            if (alivePlayers.size == 1) {
                if (pendingWinnerWaitTime < WINNER_CONFIRMATION_DELAY) {
                    pendingWinnerWaitTime += deltaTime
                    // not yet win
                } else {
                    // Hold for 1 second
                    addLog(Log.System("🏆 ${alivePlayers[0].name} 우승! 최후의 생존자!"))
                    updateGameSet()
                }
            } else {
                pendingWinnerWaitTime = 0.0

                if (alivePlayers.isEmpty()) {
                    addLog(Log.System("💀 전원 탈락! 살아남은 플레이어가 없습니다!"))
                    updateGameSet()
                }
            }
        }

        // Player-player overlap resolution (simple horizontal push)
        for (i in alivePlayers.indices) {
            for (j in i + 1 until alivePlayers.size) {
                val a = alivePlayers[i]
                val b = alivePlayers[j]
                val rSum = a.halfWidth + b.halfWidth
                val dx = b.x - a.x
                val dy = b.y - a.y
                if (abs(dy) < max(a.halfHeight, b.halfHeight) * 1.2f && abs(dx) < rSum) {
                    val overlap = rSum - abs(dx)
                    val dir = if (dx >= 0f) 1f else -1f
                    val push = overlap / 2f
                    a.x -= push * dir
                    b.x += push * dir
                    // Clamp to viewport bounds
                    if (a.x - a.halfWidth < 0f) a.x = a.halfWidth
                    if (b.x + b.halfWidth > world.viewportWidth) b.x = world.viewportWidth - b.halfWidth
                }
            }
        }

        // Attack detection
        val hitThisFrame = mutableSetOf<Pair<Int, Int>>()
        for (i in alivePlayers.indices) {
            val attacker = alivePlayers[i]
            if (!attacker.isAttackingNow) continue
            val reach = attacker.attackReach
            val heightTol = attacker.height * 0.6f
            for (j in alivePlayers.indices) {
                if (i == j) continue
                val target = alivePlayers[j]
                val dx = target.x - attacker.x
                val dy = target.y - attacker.y
                val inFront = if (attacker.isFacingRight) dx > 0f else dx < 0f
                if (inFront && abs(dx) <= reach && abs(dy) <= heightTol) {
                    val key = i to j
                    if (hitThisFrame.add(key)) {
                        target.receiveDamage(attacker, power = 700f)
                    }
                }
            }
        }
    }

    private fun spawnCar() {
        isSpawningCar = true
        spawnScope.launch {
            val car = entityFactory.createRunningCar()
            addLog(Log.Speech(car, "지나갑니다"))
            entities += car
            isSpawningCar = false
        }
    }

    fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        // 맵 (플랫폼 렌더링)
        world.render(context)

        // 엔티티
        entities
            .forEach { it.render(context, textMeasurer, fontFamily) }
    }

    fun destroyEntity(entity: Entity) {
        entities -= entity
    }

    private fun addLog(log: Log) {
        LogManager.addNewLog(log)

        // recomposition event
        LogManager.triggerItemUpdate()
    }

    private fun clearLog() {
        LogManager.clear()
        // recomposition event
        LogManager.triggerItemUpdate()
    }

    companion object {
        private const val WINNER_CONFIRMATION_DELAY = 1.0 // 1초 대기
    }
}