package com.survivai.survivai.game

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import com.survivai.survivai.game.colosseum.GameDrawScope
import com.survivai.survivai.game.component.Component
import kotlin.reflect.KClass
import kotlin.reflect.cast

interface Entity {
    var x: Float
    var y: Float
    var width: Float
    var height: Float
    var direction: EntityDirection
    var state: EntityState
    val components: MutableList<Component>

    fun <T : Component> getComponent(clazz: KClass<T>): T? {
        return components
            .find { clazz.isInstance(it) }
            ?.let { clazz.cast(it) }
    }

    fun update(deltaTime: Double, viewportWidth: Float, viewportHeight: Float, world: World) {
        components.forEach { it.update(deltaTime, this, world) }
    }
    fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        components.forEach { it.render(context, this, textMeasurer, fontFamily) }
    }
}

interface EntityState

enum class EntityDirection {
    LEFT, UP, RIGHT, DOWN,
    ;

    fun isLeft() = this == LEFT
    fun isUp() = this == UP
    fun isRight() = this == RIGHT
    fun isDown() = this == DOWN
}