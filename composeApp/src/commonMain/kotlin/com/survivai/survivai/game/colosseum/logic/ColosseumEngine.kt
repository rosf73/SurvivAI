package com.survivai.survivai.game.colosseum.logic

import androidx.compose.runtime.State
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import com.survivai.survivai.game.GameDrawScope
import com.survivai.survivai.game.colosseum.entity.detectAttackDamagedThisFrame
import com.survivai.survivai.game.colosseum.state.ColosseumInfo
import kotlin.math.abs
import kotlin.math.max

class ColosseumEngine {

    // TODO : 게임 유형 확장성 추가
    private val world get() = ColosseumInfo.world
    private val players get() = ColosseumInfo.players
    private val gameState get() = ColosseumInfo.gameState.value

    // 로그 상태 추적
    val logUpdateState: State<Boolean> get() = LogManager.itemUpdateState

    // 로그 리스트
    val logEntries: List<Log> get() = LogManager.logEntries

    fun update(deltaTime: Double) {
        if (world.viewportWidth <= 0 || world.viewportHeight <= 0) {
            return
        }

        // Get alive players
        val alivePlayers = players.filter { it.isAlive }

        // Call Entity::update
        players.forEach { it.update(deltaTime, world) }

        // (중계 로그) 대사
        alivePlayers.forEachIndexed { _, p ->
            val text = p.pollJustSpeeched()
            if (text.isNotBlank()) {
                addLog(Log.Solo(p, text))
            }
        }

        // Check for winner (only once)
        if (gameState !is ColosseumState.Ended && players.isNotEmpty()) {
            if (alivePlayers.size == 1) {
                addLog(Log.System("🏆 ${alivePlayers[0].name} 우승! 최후의 생존자!"))
                ColosseumInfo.updateGameSet()
            } else if (alivePlayers.isEmpty()) {
                addLog(Log.System("💀 전원 탈락! 살아남은 플레이어가 없습니다!"))
                ColosseumInfo.updateGameSet()
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

        // first blood 체크 (race condition 방지)
        var isFirstBloodFrame = (alivePlayers.size == players.size)

        // Attack detection
        alivePlayers.detectAttackDamagedThisFrame { attacker, target ->
            // 스탯 업데이트
            ColosseumInfo.updatePlayerAttackPoint(attacker.name)

            if (target.hp > 0) {
                addLog(Log.Duo(
                    perpetrator = attacker,
                    victim = target,
                    interaction = "🤜",
                    additional = "(HP=${target.hp})",
                ))
            } else {
                // 스탯 업데이트
                ColosseumInfo.updatePlayerKillPoint(
                    killerName = attacker.name,
                    victimName = target.name,
                )

                if (isFirstBloodFrame) { // first blood
                    addLog(Log.Duo(
                        perpetrator = attacker,
                        victim = target,
                        interaction = "에 의해",
                        additional = "First Blood! 😭",
                    ))
                    isFirstBloodFrame = false
                } else {
                    addLog(Log.Duo(
                        perpetrator = attacker,
                        victim = target,
                        interaction = "에 의해",
                        additional = "탈락! 😭",
                    ))
                }
            }
        }
    }

    fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        // 맵 (플랫폼 렌더링)
        world.render(context)

        // 엔티티
        players
            .forEach { it.render(context, textMeasurer, fontFamily) }
    }

    fun addLog(log: Log) {
        LogManager.addNewLog(log)

        // recomposition event
        LogManager.triggerItemUpdate()
    }

    fun clearLog() {
        LogManager.clear()
        // recomposition event
        LogManager.triggerItemUpdate()
    }
}