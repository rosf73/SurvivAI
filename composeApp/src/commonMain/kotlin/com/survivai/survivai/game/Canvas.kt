package com.survivai.survivai.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

interface GameDrawScope {
    fun drawCircle(color: Color, center: Offset, radius: Float)
}

interface Canvas {
    fun update(deltaTime: Double)
    fun render(context: GameDrawScope)
    fun setViewportSize(width: Float, height: Float)
    fun jump() // TODO : 객체 event 로 이전
}

expect fun getCanvas(): Canvas

expect fun createGameDrawScope(drawScope: DrawScope): GameDrawScope