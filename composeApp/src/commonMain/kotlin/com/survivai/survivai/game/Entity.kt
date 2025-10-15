package com.survivai.survivai.game

import androidx.compose.ui.text.TextMeasurer
import com.survivai.survivai.game.colosseum.GameDrawScope

interface Entity {
    fun update(deltaTime: Double, viewportWidth: Float, viewportHeight: Float)
    fun render(context: GameDrawScope, textMeasurer: TextMeasurer)
    fun setViewportHeight(height: Float)
}