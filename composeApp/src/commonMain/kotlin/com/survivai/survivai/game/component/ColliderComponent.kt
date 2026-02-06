package com.survivai.survivai.game.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import com.survivai.survivai.config.BuildConfig
import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.GameDrawScope

class ColliderComponent(
    val width: Float,
    val height: Float,
) : Component() {

    // get bounds in World
    fun getBounds(owner: Entity): Rect {
        return Rect(
            left = owner.left,
            top = owner.top,
            right = owner.left + width,
            bottom = owner.top + height
        )
    }

    override fun render(context: GameDrawScope, owner: Entity, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        // for debug
        if (BuildConfig.DEBUG) {
            context.drawRect(
                color = Color.Red.copy(alpha = 0.5f),
                topLeft = Offset(owner.left, owner.top),
                width = width,
                height = height,
            )
        }
    }
}