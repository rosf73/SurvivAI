package com.survivai.survivai.game

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.min

class WebDrawScope(private val drawScope: DrawScope) : GameDrawScope {
    override fun drawCircle(
        color: Color,
        center: Offset,
        radius: Float
    ) {
        drawScope.drawCircle(
            color = color,
            center = center,
            radius = radius,
        )
    }
}

class WebCanvas : Canvas {
    // TODO : 객체 state 로 이전
    // Game State
    var circleX = 50f
    var circleY = 50f
    val radius = 20f

    // Physics Variables
    var velocityY = 0f
    val gravity = 980f // Gravity acceleration (pixels/second^2)
    val jumpPower = -500f

    // Viewport bounds
    private var viewportHeight = 600f

    // Calculated floor position
    private val floorY: Float
        get() = viewportHeight - radius

    override fun update(deltaTime: Double) {
        // Clamp deltaTime to prevent instability in case of major frame drops (max 30ms)
        val clampedDeltaTime = min(deltaTime, 0.03).toFloat()

        // 1. Apply Gravity (Acceleration)
        velocityY += gravity * clampedDeltaTime
        circleY += velocityY * clampedDeltaTime

        // 2. Floor Collision Handling
        if (circleY > floorY) {
            circleY = floorY // Stick to the floor
            velocityY = 0f // Reset vertical speed
        }
    }

    override fun render(context: GameDrawScope) {
        // Draw the floor (Optional)
        // context.drawRect(Color.Gray, Offset(0f, floorY), size = Size(viewportWidth, viewportHeight - floorY))

        // Draw the ball
        context.drawCircle(
            color = Color.Blue,
            center = Offset(circleX, circleY),
            radius = radius
        )
    }

    override fun setViewportSize(width: Float, height: Float) {
        // Update canvas height for floor calculation
        viewportHeight = height
        // Ensure the ball doesn't fall through if the height changes
        if (circleY > floorY) {
            circleY = floorY
            velocityY = 0f
        }
    }

    override fun jump() {
        // Only allow jumping when on the floor
        if (circleY >= floorY - 1f) { // Use a small tolerance
            velocityY = jumpPower
        }
    }
}

actual fun getCanvas(): Canvas = WebCanvas()

actual fun createGameDrawScope(drawScope: DrawScope): GameDrawScope = WebDrawScope(drawScope)