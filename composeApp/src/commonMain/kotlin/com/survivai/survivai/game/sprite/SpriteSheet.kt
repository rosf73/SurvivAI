package com.survivai.survivai.game.sprite

import com.survivai.survivai.game.EntityState

class SpriteSheet(
    val animations: Map<EntityState, List<SpriteAnimation>>
) {
    fun get(action: EntityState): List<SpriteAnimation>? = animations[action]
}

enum class ActionState : EntityState {
    IDLE, WALK, RUN, JUMP, ATTACK, HIT, DIE,
    ;
}