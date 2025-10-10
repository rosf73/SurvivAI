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
        TODO("Not yet implemented")
    }

    override fun render(context: GameDrawScope) {
        TODO("Not yet implemented")
    }

    override fun setViewportSize(width: Float, height: Float) {
        TODO("Not yet implemented")
    }

    override fun jump() {
        TODO("Not yet implemented")
    }
}

actual fun getCanvas(): Canvas = AndroidCanvas()

actual fun createGameDrawScope(drawScope: DrawScope): GameDrawScope = AndroidDrawScope(drawScope)