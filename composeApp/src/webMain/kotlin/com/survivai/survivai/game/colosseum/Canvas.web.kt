package com.survivai.survivai.game.colosseum

import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import com.survivai.survivai.game.colosseum.entity.detectAttackDamagedThisFrame
import com.survivai.survivai.game.colosseum.state.ColosseumInfo
import com.survivai.survivai.game.colosseum.logic.ColosseumState
import com.survivai.survivai.game.colosseum.state.Log
import kotlin.math.abs
import kotlin.math.max

class WebCanvas : Canvas {

    private var viewportWidth = 0f
    private var viewportHeight = 0f

    // TODO : 게임 유형 확장성 추가
    private val world get() = ColosseumInfo.world
    private val players get() = ColosseumInfo.players
    private val gameState get() = ColosseumInfo.gameState.value

    // Simple combat/event log -> delegate to shared store
    private fun log(log: Log) {
        ColosseumInfo.addLog(log)
    }

    override fun update(deltaTime: Double) {
        if (viewportWidth <= 0 || viewportHeight <= 0) {
            return
        }

        // Get alive players
        val alivePlayers = players.filter { it.isAlive }

        // Call Entity::update
        players.forEach { it.update(deltaTime, viewportWidth, viewportHeight, world) }

        // (중계 로그) 대사
        alivePlayers.forEachIndexed { _, p ->
            val text = p.pollJustSpeeched()
            if (text.isNotBlank()) {
                log(Log.Solo(p, text))
            }
        }

        // Check for winner (only once)
        if (gameState !is ColosseumState.Ended && players.isNotEmpty()) {
            if (alivePlayers.size == 1) {
                log(Log.System("🏆 ${alivePlayers[0].name} 우승! 최후의 생존자!"))
                ColosseumInfo.updateGameSet()
            } else if (alivePlayers.isEmpty()) {
                log(Log.System("💀 전원 탈락! 살아남은 플레이어가 없습니다!"))
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
                    if (b.x + b.halfWidth > viewportWidth) b.x = viewportWidth - b.halfWidth
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
                log(Log.Duo(
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
                    log(Log.Duo(
                        perpetrator = attacker,
                        victim = target,
                        interaction = "에 의해",
                        additional = "First Blood! 😭",
                    ))
                    isFirstBloodFrame = false
                } else {
                    log(Log.Duo(
                        perpetrator = attacker,
                        victim = target,
                        interaction = "에 의해",
                        additional = "탈락! 😭",
                    ))
                }
            }
        }
    }

    override fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        // 맵 (플랫폼 렌더링)
        world.render(context)

        // 엔티티
        players
            .forEach { it.render(context, textMeasurer, fontFamily) }
    }

    override fun setViewportSize(width: Float, height: Float) {
        viewportWidth = width
        viewportHeight = height
        ColosseumInfo.setViewportSize(width, height)
    }
}

actual fun getCanvas(): Canvas = WebCanvas()