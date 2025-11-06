package com.survivai.survivai.game.colosseum

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import com.survivai.survivai.game.colosseum.entity.Player
import com.survivai.survivai.game.colosseum.world.ColosseumWorld
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random

class WebDrawScope(private val drawScope: DrawScope) : GameDrawScope {
    override fun drawCircle(
        color: Color,
        center: Offset,
        radius: Float
    ) {
        drawScope.drawCircle(
            color = color,
            center = center,
            radius = radius,
        )
    }

    override fun drawArc(
        color: Color,
        topLeft: Offset,
        width: Float,
        height: Float,
        startAngle: Float,
        sweepAngle: Float,
        useCenter: Boolean
    ) {
        drawScope.drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = useCenter,
            topLeft = topLeft,
            size = Size(width, height),
        )
    }

    override fun drawRect(color: Color, topLeft: Offset, width: Float, height: Float) {
        drawScope.drawRect(
            color = color,
            topLeft = topLeft,
            size = Size(width, height),
        )
    }

    override fun drawText(
        textMeasurer: TextMeasurer,
        text: String,
        topLeft: Offset,
        size: Size,
        style: TextStyle,
        softWrap: Boolean,
    ) {
        drawScope.drawText(
            textMeasurer = textMeasurer,
            text = text,
            topLeft = topLeft,
            style = style,
            softWrap = softWrap,
            size = size,
        )
    }
}

class WebCanvas : Canvas {

    private var viewportWidth = 0f
    private var viewportHeight = 0f

    // TODO : 게임 유형 확장성 추가
    private val world get() = ColosseumInfo.world
    private val players get() = ColosseumInfo.players
    private val winnerAnnounced get() = ColosseumInfo.winnerAnnounced

    // Simple combat/event log -> delegate to shared store
    private fun log(message: String) {
        ColosseumInfo.addLog(message)
    }

    override fun update(deltaTime: Double) {
        if (viewportWidth > 0 && viewportHeight > 0) {
            // Get alive players
            val alivePlayers = players.filter { it.isAlive }

            // Call Entity::update
            alivePlayers.forEach { it.update(deltaTime, viewportWidth, viewportHeight, world) }

            // (중계 로그) 대사
            alivePlayers.forEachIndexed { i, p ->
                val text = p.pollJustSpeeched()
                if (text.isNotBlank()) {
                    log("${p.name} : \"$text\"")
                }
            }

            // Check for winner (only once)
            if (!winnerAnnounced && alivePlayers.size == 1) {
                log("        🏆 ${alivePlayers[0].name} 우승! 최후의 생존자!")
                ColosseumInfo.updateGameSet()
            }

            // Player-player overlap resolution (simple horizontal push)
            for (i in alivePlayers.indices) {
                for (j in i + 1 until alivePlayers.size) {
                    val a = alivePlayers[i]
                    val b = alivePlayers[j]
                    val rSum = a.radius + b.radius
                    val dx = b.x - a.x
                    val dy = b.y - a.y
                    if (abs(dy) < max(a.radius, b.radius) * 1.2f && abs(dx) < rSum) {
                        val overlap = rSum - abs(dx)
                        val dir = if (dx >= 0f) 1f else -1f
                        val push = overlap / 2f
                        a.x -= push * dir
                        b.x += push * dir
                        // Clamp to viewport bounds
                        if (a.x - a.radius < 0f) a.x = a.radius
                        if (b.x + b.radius > viewportWidth) b.x = viewportWidth - b.radius
                    }
                }
            }

            // Attack detection and damage/knockback
            val hitThisFrame = mutableSetOf<Pair<Int, Int>>()
            for (i in alivePlayers.indices) {
                val attacker = alivePlayers[i]
                if (!attacker.isAttackingNow) continue
                val reach = attacker.radius * 2.2f
                val heightTol = attacker.radius * 1.2f
                for (j in alivePlayers.indices) {
                    if (i == j) continue
                    val target = alivePlayers[j]
                    val dx = target.x - attacker.x
                    val dy = target.y - attacker.y
                    val inFront = if (attacker.isFacingRight) dx > 0f else dx < 0f
                    if (inFront && abs(dx) <= reach && abs(dy) <= heightTol) {
                        val key = i to j
                        if (hitThisFrame.add(key)) {
                            val damaged = target.receiveDamage(attacker.x, power = 700f)
                            if (damaged) {
                                if (target.currentHp > 0) {
                                    log("        ${alivePlayers[i].name} 🤜 ${target.name} (HP=${target.currentHp})")
                                } else {
                                    log("        ${alivePlayers[i].name} 에 의해 ${target.name} 탈락! 😭")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        // 맵 (플랫폼 렌더링)
        world.render(context)

        // 엔티티
        players
            .filter { it.isAlive }
            .forEach { it.render(context, textMeasurer, fontFamily) }
    }

    override fun setViewportSize(width: Float, height: Float) {
        viewportWidth = width
        viewportHeight = height
        ColosseumInfo.setViewportSize(width, height)
    }
}

actual fun getCanvas(): Canvas = WebCanvas()

actual fun createGameDrawScope(drawScope: DrawScope): GameDrawScope = WebDrawScope(drawScope)