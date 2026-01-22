package com.survivai.survivai.game.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import com.survivai.survivai.config.BuildConfig
import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.colosseum.GameDrawScope

class ColliderComponent(
    val width: Float,
    val height: Float,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
) : Component() {

    // get bounds in World
    fun getBounds(owner: Entity): Rect {
        return Rect(
            left = owner.x + offsetX,
            top = owner.y + offsetY,
            right = owner.x + offsetX + width,
            bottom = owner.y + offsetY + height
        )
    }

    override fun render(context: GameDrawScope, owner: Entity, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        // for debug
        if (BuildConfig.DEBUG) {
            context.drawRect(
                color = Color.Red,
                topLeft = Offset(owner.x + offsetX, owner.y + offsetY),
                width = width,
                height = height,
            )
        }
    }
}