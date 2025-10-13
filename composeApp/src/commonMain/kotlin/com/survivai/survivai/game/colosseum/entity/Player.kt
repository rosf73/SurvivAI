package com.survivai.survivai.game.colosseum.entity

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import com.survivai.survivai.game.Entity
import kotlin.math.min

class Player(
    initialX: Float,
    initialY: Float,
    val radius: Float = 20f,
    val color: Color = Color.Blue,
) : Entity {

    // Position
    var x = initialX
    var y = initialY

    var velocityX = 0f // 수평 속도
    private val moveSpeed = 400f // 초당 이동 속도
    private val friction = 0.9f // 마찰력 계수

    var velocityY = 0f // 수직 속도
    private val gravity = 980f // 중력 가속도 (픽셀/초^2)
    private val jumpPower = -500f

    // Viewport
    private var viewportWidth = 0f
    private var viewportHeight = 0f
    private val floorY: Float
        get() = viewportHeight - radius

    override fun update(deltaTime: Double, viewportWidth: Float, viewportHeight: Float) {
        this.viewportWidth = viewportWidth
        this.viewportHeight = viewportHeight

        // frame drop 에 의한 불안정성 예방 기준 시간 (max 30ms)
        val clampedDeltaTime = min(deltaTime, 0.03).toFloat()

        // --- 수직 이동 ---
        velocityY += gravity * clampedDeltaTime
        y += velocityY * clampedDeltaTime
        // 바닥 충돌
        if (y > floorY) {
            y = floorY // Stick to the floor
            velocityY = 0f // Reset vertical speed
        }

        // --- 수평 이동 ---
        if (velocityX == 0f && y >= floorY - 1f) {
            velocityX *= friction // 공중에 떠 있을 때는 마찰력 적용 안 함
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
    }

    override fun render(context: CanvasDrawScope) {
        context.drawCircle(
            color = color,
            center = Offset(x, y),
            radius = radius
        )
    }

    override fun setViewportHeight(height: Float) {
        viewportHeight = height
    }

    fun move(direction: MoveDirection) {
        velocityX = when (direction) {
            MoveDirection.LEFT -> -moveSpeed
            MoveDirection.RIGHT -> moveSpeed
            MoveDirection.STOP -> 0f
        }
    }

    fun jump() {
        if (y >= floorY - 1f) {
            velocityY = jumpPower
        }
    }
}

enum class MoveDirection {
    LEFT, RIGHT, STOP,
    ;
}