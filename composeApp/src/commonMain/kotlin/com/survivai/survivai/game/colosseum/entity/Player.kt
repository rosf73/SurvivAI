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
    var velocityY = 0f

    private val gravity = 980f // 중력 가속도 (픽셀/초^2)
    private val jumpPower = -500f

    // From platform
    private var viewportHeight = 0f
    private val floorY: Float
        get() = viewportHeight - radius

    override fun update(deltaTime: Double, viewportHeight: Float) {
        // Clamp deltaTime to prevent instability in case of major frame drops (max 30ms)
        val clampedDeltaTime = min(deltaTime, 0.03).toFloat()

        // 1. Apply Gravity (Acceleration)
        velocityY += gravity * clampedDeltaTime
        y += velocityY * clampedDeltaTime

        // 2. Floor Collision Handling
        if (y > floorY) {
            y = floorY // Stick to the floor
            velocityY = 0f // Reset vertical speed
        }
    }

    override fun render(context: CanvasDrawScope) {
        // Draw the floor (Optional)
        // context.drawRect(Color.Gray, Offset(0f, floorY), size = Size(viewportWidth, viewportHeight - floorY))

        // Draw the ball
        context.drawCircle(
            color = Color.Blue,
            center = Offset(x, y),
            radius = radius
        )
    }

    override fun setViewportHeight(height: Float) {
        viewportHeight = height
    }

    fun jump() {
        if (y >= floorY - 1f) {
            velocityY = jumpPower
        }
    }
}