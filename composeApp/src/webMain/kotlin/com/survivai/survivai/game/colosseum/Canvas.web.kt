package com.survivai.survivai.game.colosseum

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import com.survivai.survivai.game.colosseum.entity.Player
import com.survivai.survivai.game.colosseum.CombatLogStore
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

    private val platforms = mutableListOf<PlatformRect>()

    // Simple combat/event log -> delegate to shared store
    private fun log(message: String) {
        CombatLogStore.add(message)
    }

    private fun rebuildPlatforms() {
        platforms.clear()
        if (viewportWidth <= 0f || viewportHeight <= 0f) return
        val floorH = 30f
        // floor
        platforms.add(PlatformRect(0f, viewportHeight - floorH, viewportWidth, floorH))
        // middle
        platforms.add(PlatformRect(viewportWidth * 0.25f, viewportHeight * 0.65f, viewportWidth * 0.5f))
        // left upper-mid
        platforms.add(PlatformRect(viewportWidth * 0.02f, viewportHeight * 0.45f, viewportWidth * 0.28f))
        // right top
        platforms.add(PlatformRect(viewportWidth * 0.55f, viewportHeight * 0.30f, viewportWidth * 0.4f))
    }

    // TODO : 임시 엔티티 리스트
    private val players = listOf(
        Player(
            initialX = 0f,
            initialY = 0f,
        ),
        Player(
            initialX = 0f,
            initialY = 0f,
            color = Color.Red,
        ),
        Player(
            initialX = 0f,
            initialY = 0f,
            color = Color.Green,
        ),
    )

    override fun update(deltaTime: Double) {
        if (viewportWidth > 0 && viewportHeight > 0) {
            if (!initialized) {
                rebuildPlatforms()
                initializePlayerPositions()
                initialized = true
            }
            // Physics and self-updates
            players.forEach { it.updateWithPlatforms(deltaTime, viewportWidth, viewportHeight, platforms) }

            // Log jump events
            players.forEachIndexed { i, p ->
                if (p.pollJustJumped()) {
                    log("P$i jumps")
                }
            }

            // Player-player overlap resolution (simple horizontal push)
            for (i in players.indices) {
                for (j in i + 1 until players.size) {
                    val a = players[i]
                    val b = players[j]
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
            for (i in players.indices) {
                val attacker = players[i]
                if (!attacker.isAttackingNow) continue
                val reach = attacker.radius * 2.2f
                val heightTol = attacker.radius * 1.2f
                for (j in players.indices) {
                    if (i == j) continue
                    val target = players[j]
                    val dx = target.x - attacker.x
                    val dy = target.y - attacker.y
                    val inFront = if (attacker.isFacingRight) dx > 0f else dx < 0f
                    if (inFront && abs(dx) <= reach && abs(dy) <= heightTol) {
                        val key = i to j
                        if (hitThisFrame.add(key)) {
                            target.receiveHit(attacker.x, power = 700f)
                            log("P$i hits P$j (HP=${target.currentHp})")
                        }
                    }
                }
            }
        }
    }

    override fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        // 맵 (플랫폼 렌더링)
        platforms.forEach { it.render(context) }

        // 엔티티
        players.forEach { it.render(context, textMeasurer, fontFamily) }

    }

    override fun setViewportSize(width: Float, height: Float) {
        viewportWidth = width
        viewportHeight = height
        rebuildPlatforms()
    }

    private fun initializePlayerPositions() {
        // Randomize initial positions within bounds and avoid overlapping
        val margin = 10f
        val placed = mutableListOf<Pair<Float, Float>>()
        players.forEach { p ->
            val radius = p.radius
            val minX = radius + margin
            val maxX = (viewportWidth - radius - margin).coerceAtLeast(minX)
            val floorTop = platforms.firstOrNull()?.top ?: viewportHeight
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