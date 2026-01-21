package com.survivai.survivai.game.component

class CombatComponent(
    var hp: Double,
) : Component() {
    fun takeDamage(amount: Double) { hp -= amount }
}