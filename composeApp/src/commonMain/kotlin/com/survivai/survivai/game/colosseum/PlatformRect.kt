package com.survivai.survivai.game.colosseum

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.survivai.survivai.game.GameDrawScope

/**
 * Simple axis-aligned platform rectangle. y is the top edge.
 */
data class PlatformRect(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float = 16f,
    val color: Color = Color.Black,
) {
    val left: Float get() = x
    val right: Float get() = x + width
    val top: Float get() = y
    val bottom: Float get() = y + height

    fun render(context: GameDrawScope) {
        context.drawRect(color, Offset(left, top), width, height)
    }
}