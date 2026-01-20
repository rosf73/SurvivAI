package com.survivai.survivai.game.component

import com.survivai.survivai.game.Entity

abstract class CombatEntity : Entity {

    protected var attackState = AttackState.NONE
    protected var attackTimer = 0f

    abstract fun attack()
}

enum class AttackState {
    NONE,       // do nothing
    PREPARING,  // prepare delay
    EXECUTING   // attack
}