package com.survivai.survivai.game.colosseum

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

interface GameDrawScope {
    fun drawCircle(color: Color, center: Offset, radius: Float)
    fun drawArc(color: Color, topLeft: Offset, width: Float, height: Float, startAngle: Float, sweepAngle: Float, useCenter: Boolean)
    fun drawRect(color: Color, topLeft: Offset, width: Float, height: Float)
}

interface Canvas {
    fun update(deltaTime: Double)
    fun render(context: GameDrawScope)
    fun setViewportSize(width: Float, height: Float)
}

expect fun getCanvas(): Canvas

expect fun createGameDrawScope(drawScope: DrawScope): GameDrawScope