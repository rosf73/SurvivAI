package com.survivai.survivai.game.colosseum

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily

class IOSCanvas : Canvas {

    override fun update(deltaTime: Double) {
        // TODO : Update all player
    }

    override fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        // TODO : Update all player
    }

    override fun setViewportSize(width: Float, height: Float) {
        // TODO : Update all player
    }
}

actual fun getCanvas(): Canvas = IOSCanvas()