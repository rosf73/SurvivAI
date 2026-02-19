package com.survivai.survivai.game.colosseum.logic

import com.survivai.survivai.game.Entity

sealed interface ColosseumEvent {
    data class Attack(val attacker: Entity, val victim: Entity) : ColosseumEvent
    data class Kill(val killer: Entity, val victim: Entity) : ColosseumEvent
    data class Accident(val disaster: Entity, val victim: Entity) : ColosseumEvent
}