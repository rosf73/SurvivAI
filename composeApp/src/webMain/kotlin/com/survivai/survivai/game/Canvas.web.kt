package com.survivai.survivai.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.survivai.survivai.game.colosseum.entity.Player
import kotlin.math.min

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
}

class WebCanvas : Canvas {

    private var viewportWidth = 0f
    private var viewportHeight = 0f

    // TODO : 임시 엔티티 리스트
    private val players = listOf(
        Player(
            initialX = 30f,
            initialY = 300f,
        ),
        Player(
            initialX = 100f,
            initialY = 300f,
            color = Color.Red,
        ),
        Player(
            initialX = 350f,
            initialY = 300f,
            color = Color.Green,
        ),
    )

    override fun update(deltaTime: Double) {
        if (viewportWidth > 0 && viewportHeight > 0) {
            players.forEach { it.update(deltaTime, viewportWidth, viewportHeight) }
        }
    }

    override fun render(context: GameDrawScope) {
        // 맵
        // context.drawRect(Color.Gray, Offset(0f, floorY), size = Size(viewportWidth, viewportHeight - floorY))

        // 엔티티
        players.forEach { it.render(context) }
    }

    override fun setViewportSize(width: Float, height: Float) {
        viewportWidth = width
        viewportHeight = height
    }
}

actual fun getCanvas(): Canvas = WebCanvas()

actual fun createGameDrawScope(drawScope: DrawScope): GameDrawScope = WebDrawScope(drawScope)