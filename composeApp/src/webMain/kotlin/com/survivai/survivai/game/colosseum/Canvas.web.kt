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
    private var initialized = false

    // TODO : 게임 유형 확장성 추가
    private val world get() = ColosseumInfo.world
    private val players get() = ColosseumInfo.players

    // Simple combat/event log -> delegate to shared store
    private fun log(message: String) {
        ColosseumInfo.addLog(message)
    }

    private val eliminatedPlayers = mutableSetOf<Int>()

    override fun update(deltaTime: Double) {
        if (viewportWidth > 0 && viewportHeight > 0) {
            if (!initialized) {
                world.buildMap(viewportWidth, viewportHeight)
                initializePlayerPositions()
                initialized = true
            }

            // Get alive players
            val alivePlayers = players.filter { it.isAlive }

            // Call Entity::update
            alivePlayers.forEach { it.update(deltaTime, viewportWidth, viewportHeight, world) }

            // (중계 로그) 대사
            alivePlayers.forEachIndexed { i, p ->
                val text = p.pollJustSpeeched()
                if (text.isNotBlank()) {
                    log("P$i : \"$text\"")
                }
            }

            // (중계 로그) 탈락
            players.forEachIndexed { i, p ->
                if (!p.isAlive && !eliminatedPlayers.contains(i)) {
                    eliminatedPlayers.add(i)
                    log("P$i 탈락! ToT")
                }
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
                            target.receiveDamage(attacker.x, power = 700f)
                            log("P$i hits P$j (HP=${target.currentHp})")
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
        world.buildMap(viewportWidth, viewportHeight)
    }

    private fun initializePlayerPositions() {
        // Randomize initial positions within bounds and avoid overlapping
        val margin = 10f
        val placed = mutableListOf<Pair<Float, Float>>()
        players.forEach { p ->
            val radius = p.radius
            val minX = radius + margin
            val maxX = (viewportWidth - radius - margin).coerceAtLeast(minX)
            val floorTop = world.getFloor() ?: viewportHeight
            val y = (floorTop - radius).coerceAtLeast(radius)

            var tries = 0
            var x: Float
            do {
                x = if (maxX > minX) kotlin.random.Random.nextFloat() * (maxX - minX) + minX else minX
                tries++
                // ensure no overlap with already placed players
            } while (placed.any { (ox, _) -> kotlin.math.abs(ox - x) < (p.radius * 2 + margin) } && tries < 50)

            p.x = x
            p.y = y
            placed.add(x to y)
        }
    }
}

actual fun getCanvas(): Canvas = WebCanvas()

actual fun createGameDrawScope(drawScope: DrawScope): GameDrawScope = WebDrawScope(drawScope)