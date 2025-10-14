package com.survivai.survivai.game.colosseum.entity

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.colosseum.GameDrawScope
import kotlin.math.min
import kotlin.random.Random

class Player(
    initialX: Float,
    initialY: Float,
    val radius: Float = 30f,
    val color: Color = Color.Blue,
) : Entity {

    // Position
    var x = initialX
    var y = initialY

    var velocityX = 0f // 수평 속도

    var velocityY = 0f // 수직 속도
    private val gravity = 980f // 중력 가속도 (픽셀/초^2)

    // Viewport
    private var viewportWidth = 0f
    private var viewportHeight = 0f
    private val floorY: Float
        get() = viewportHeight - radius

    // Behavior
    private var facingRight = true // 바라보는 방향
    private var isAttacking = false
    private var attackTimer = 0f

    // Random
    private var idleTime = 1f // 1초 후 시작
    private var inAction = false

    // TODO : HP
    private var hp = START_HP

    override fun update(deltaTime: Double, viewportWidth: Float, viewportHeight: Float) {
        this.viewportWidth = viewportWidth
        this.viewportHeight = viewportHeight

        // frame drop 에 의한 불안정성 예방 기준 시간 (max 30ms)
        val clampedDeltaTime = min(deltaTime, 0.03).toFloat()

        // 공격 타이머 처리
        if (isAttacking) {
            attackTimer -= clampedDeltaTime
            if (attackTimer <= 0f) {
                isAttacking = false // 0.3초 경과, 공격 종료
            }
        }

        // --- 수직 이동 ---
        velocityY += gravity * clampedDeltaTime
        y += velocityY * clampedDeltaTime
        // 바닥 충돌
        val onGround = y >= floorY - 1f
        if (y > floorY) {
            y = floorY // Stick to the floor
            velocityY = 0f // Reset vertical speed
        }

        // --- 수평 이동 ---
        if (velocityX != 0f && y >= floorY - 1f) {
            velocityX *= FRICTION // 공중에 떠 있을 때는 마찰력 적용 안 함
            if (velocityX in -1f..1f) {
                velocityX = 0f
            }
        }
        x += velocityX * clampedDeltaTime
        // 벽 충돌
        if (x - radius < 0) {
            x = radius
            velocityX = 0f
        } else if (x + radius > viewportWidth) {
            x = viewportWidth - radius
            velocityX = 0f
        }

        val wasInAction = inAction
        if (inAction) {
            // 공격 종료: isAttacking 플래그에 의존
            val attackFinished = !isAttacking

            // 이동 종료: 속도가 0이 되었을 때
            val moveFinished = velocityX == 0f

            // 점프 종료: 지면에 닿았고 수직 속도가 0일 때 (다시 점프 가능 상태)
            val jumpFinished = onGround && velocityY == 0f

            // 모든 동작이 완료되었을 경우 inAction을 false로 설정
            if (attackFinished && moveFinished && jumpFinished) {
                inAction = false
            }
        }

        // 행위 이벤트
        if (!inAction) {
            if (wasInAction) {
                // 직전 동작이 끝났을 때만 idleTime 재설정
                idleTime = Random.nextFloat()
            }

            if (idleTime > 0f) {
                idleTime -= clampedDeltaTime
            } else {
                when (Random.nextInt(3)) {
                    0 -> move(MoveDirection.entries.random())
                    1 -> jump()
                    2 -> attack()
                }
            }
        }
    }

    override fun render(context: GameDrawScope) {
        context.drawCircle(
            color = color,
            center = Offset(x, y),
            radius = radius
        )

        // Attack effect
        if (isAttacking) {
            // arc size
            val attackRadius = radius
            // arc offset
            val offsetDistance = radius + 5f
            val top = y
            val left = x + if (facingRight) offsetDistance else -offsetDistance
            // arc angle
            val startAngle = if (facingRight) -90f else 90f
            val sweepAngle = 180f

            context.drawArc(
                color = Color.Black,
                topLeft = Offset(left - attackRadius, top - attackRadius),
                width = attackRadius * 2f,
                height = attackRadius * 2f,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
            )
        }
    }

    override fun setViewportHeight(height: Float) {
        viewportHeight = height
    }

    private fun setAction() {
        inAction = true
    }

    fun move(
        direction: MoveDirection,
        power: Float = Random.nextFloat() * 1500 + 500f, // 500f ~ 2000f
    ) {
        if (inAction) return
        setAction()

        when (direction) {
            MoveDirection.LEFT -> {
                velocityX = (velocityX - power).coerceAtLeast(-MAX_SPEED)
                facingRight = false
            }
            MoveDirection.RIGHT -> {
                velocityX = (velocityX + power).coerceAtMost(MAX_SPEED)
                facingRight = true
            }
        }
    }

    fun jump() {
        if (inAction || y < floorY - 1f) return
        setAction()

        if (y >= floorY - 1f) {
            velocityY = Random.nextFloat() * -500 - 500f // -500f ~ -1000f
        }
    }

    fun attack() {
        if (inAction) return
        setAction()

        if (!isAttacking) {
            isAttacking = true
            attackTimer = ATTACK_DURATION
        }
    }

    companion object {
        private const val ATTACK_DURATION = 0.3f
        private const val MAX_SPEED = 2000f
        private const val FRICTION = 0.95f // 마찰력 계수
        private const val START_HP = 3
    }
}

enum class MoveDirection {
    LEFT, RIGHT,
    ;
}