package com.survivai.survivai.game.component

import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.World
import kotlin.math.min

class DamageableComponent(
    var hp: Double,
    val invincibilityTime: Double = 0.0,
    val onDeath: (() -> Unit)? = null,
) : Component() {
    val isAlive get() = hp > 0
    private var isInvincible: Boolean = false
    private var invincibilityDuration: Double = 0.0

    fun takeDamage(amount: Double): Boolean {
        if (!isAlive || isInvincible) return false

        hp = (hp - amount).coerceAtLeast(0.0)
        if (isAlive) {
            // invincible!
            if (invincibilityTime > 0) {
                isInvincible = true
                invincibilityDuration = 0.0
            }
        } else {
            onDeath?.invoke()
        }
        return true
    }

    override fun update(deltaTime: Double, owner: Entity, world: World) {
        super.update(deltaTime, owner, world)

        val clampedDeltaTime = min(deltaTime, 0.03).toFloat()

        if (isInvincible) {
            invincibilityDuration += clampedDeltaTime

            if (invincibilityDuration >= invincibilityTime) {
                isInvincible = false
            }
        }
    }
}