package com.survivai.survivai.game.component

import androidx.annotation.CallSuper
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

abstract class MotionableCombatEntity : CombatEntity() {

    private var lastAttackAnimation: SpriteAnimation? = null

    protected open var durationAttackPrepare = 1.0f

    override fun attack() {
        if (attackState != AttackState.NONE) return

        val attackAnimation = lastAttackAnimation
        if (attackAnimation == null) {
            attackState = AttackState.PREPARING
            attackTimer = durationAttackPrepare
        } else {
            // SpriteAnimation의 데이터를 기반으로 공격 상태 시작
            // 우선은 기존과 동일하게 PREPARING 상태로 진입하되, 
            // 실제 렌더링 시 SpriteAnimation을 사용하도록 준비합니다.
            attackState = AttackState.PREPARING
            attackTimer = durationAttackPrepare
        }
    }

    @CallSuper
    open fun attack(
        sprite: SpriteAnimation,
        size: Size,
        centerOffset: Offset,
    ) {
        lastAttackAnimation = sprite
        attack()
    }
}