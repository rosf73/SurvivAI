package com.survivai.survivai.game.colosseum

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle

interface GameDrawScope {
    fun drawCircle(color: Color, center: Offset, radius: Float)
    fun drawArc(color: Color, topLeft: Offset, width: Float, height: Float, startAngle: Float, sweepAngle: Float, useCenter: Boolean)
    fun drawRect(color: Color, topLeft: Offset, width: Float, height: Float)
    fun drawText(textMeasurer: TextMeasurer, text: String, topLeft: Offset, size: Size = Size.Unspecified, style: TextStyle = TextStyle.Default, softWrap: Boolean = true)
}

interface Canvas {
    fun update(deltaTime: Double)
    fun render(context: GameDrawScope, textMeasurer: TextMeasurer)
    fun setViewportSize(width: Float, height: Float)
}

expect fun getCanvas(): Canvas

expect fun createGameDrawScope(drawScope: DrawScope): GameDrawScope