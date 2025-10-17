package com.survivai.survivai.game

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import com.survivai.survivai.game.colosseum.GameDrawScope

interface Entity {
    fun update(deltaTime: Double, viewportWidth: Float, viewportHeight: Float)
    fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily)
    fun setViewportHeight(height: Float)
}