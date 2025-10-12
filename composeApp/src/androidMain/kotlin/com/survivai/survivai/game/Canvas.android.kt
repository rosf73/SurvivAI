package com.survivai.survivai.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

class AndroidDrawScope(private val drawScope: DrawScope) : GameDrawScope {
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

class AndroidCanvas : Canvas {

    override fun update(deltaTime: Double) {
        // TODO : Update all player
    }

    override fun render(context: GameDrawScope) {
        // TODO : Update all player
    }

    override fun setViewportSize(width: Float, height: Float) {
        // TODO : Update all player
    }
}

actual fun getCanvas(): Canvas = AndroidCanvas()

actual fun createGameDrawScope(drawScope: DrawScope): GameDrawScope = AndroidDrawScope(drawScope)