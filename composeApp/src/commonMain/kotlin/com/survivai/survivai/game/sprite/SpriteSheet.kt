package com.survivai.survivai.game.sprite

import androidx.compose.ui.geometry.Size
import com.survivai.survivai.game.Entity

class SpriteSheet(
    val imageSize: Size,
    val animations: Map<Entity.State, List<SpriteAnimation>>
) {
    fun get(action: Entity.State): List<SpriteAnimation>? = animations[action]
}

enum class ActionState : Entity.State {
    IDLE, WALK, RUN, JUMP, ATTACK, HIT, DIE,
    ;
}