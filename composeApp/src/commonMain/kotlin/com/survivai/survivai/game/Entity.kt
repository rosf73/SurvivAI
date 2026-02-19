package com.survivai.survivai.game

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import com.survivai.survivai.game.component.Component
import kotlin.reflect.cast

interface Entity {
    val name: String
    val signatureColor: Color

    var x: Float
    var y: Float
    var width: Float
    var imageWidth: Float
    var height: Float
    var imageHeight: Float
    val left get() = x - width / 2
    val top get() = y - height / 2
    val right get() = x + width / 2
    val bottom get() = y + height / 2

    var direction: Direction
    var state: State
    val components: MutableList<Component>

    fun update(deltaTime: Double, world: World) {
        components.forEach { it.update(deltaTime, this, world) }
    }
    fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        components.forEach { it.render(context, this, textMeasurer, fontFamily) }
    }

    interface State

    enum class Direction {
        LEFT, UP, RIGHT, DOWN,
        ;

        fun isLeft() = this == LEFT
        fun isUp() = this == UP
        fun isRight() = this == RIGHT
        fun isDown() = this == DOWN
    }
}

inline fun <reified T : Component> Entity.getComponent(): T? {
    return components
        .find { T::class.isInstance(it) }
        ?.let { T::class.cast(it) }
}