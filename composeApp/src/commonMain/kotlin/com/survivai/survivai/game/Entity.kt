package com.survivai.survivai.game

import androidx.compose.ui.graphics.drawscope.CanvasDrawScope

interface Entity {
    fun update(deltaTime: Double, viewportWidth: Float, viewportHeight: Float)
    fun render(context: CanvasDrawScope)
    fun setViewportHeight(height: Float)
}