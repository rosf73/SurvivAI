package com.survivai.survivai.game.sprite

import androidx.compose.ui.geometry.Size
import com.survivai.survivai.game.EntityState

class SpriteSheet(
    val imageSize: Size,
    val animations: Map<EntityState, List<SpriteAnimation>>
) {
    fun get(action: EntityState): List<SpriteAnimation>? = animations[action]
}

enum class ActionState : EntityState {
    IDLE, WALK, RUN, JUMP, ATTACK, HIT, DIE,
    ;
}