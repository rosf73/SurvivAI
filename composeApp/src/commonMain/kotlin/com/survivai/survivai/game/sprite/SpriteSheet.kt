package com.survivai.survivai.game.sprite

class SpriteSheet(
    val animations: Map<AnimationAction, SpriteAnimation>
) {
    fun get(action: AnimationAction): SpriteAnimation? = animations[action]
}

enum class AnimationAction {
    IDLE, WALK, RUN, JUMP, ATTACK, HIT, DIE,
    ;
}