package com.survivai.survivai.game.colosseum

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import com.survivai.survivai.game.colosseum.entity.Player

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

    override fun drawArc(
        color: Color,
        topLeft: Offset,
        width: Float,
        height: Float,
        startAngle: Float,
        sweepAngle: Float,
        useCenter: Boolean
    ) {
        drawScope.drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = useCenter,
            topLeft = topLeft,
            size = Size(width, height),
        )
    }

    override fun drawRect(color: Color, topLeft: Offset, width: Float, height: Float) {
        drawScope.drawRect(
            color = color,
            topLeft = topLeft,
            size = Size(width, height),
        )
    }

    override fun drawText(
        textMeasurer: TextMeasurer,
        text: String,
        topLeft: Offset,
        size: Size,
        style: TextStyle,
        softWrap: Boolean,
    ) {
        drawScope.drawText(
            textMeasurer = textMeasurer,
            text = text,
            topLeft = topLeft,
            style = style,
            softWrap = softWrap,
            size = size,
        )
    }
}

class WebCanvas : Canvas {

    private var viewportWidth = 0f
    private var viewportHeight = 0f

    // TODO : 임시 엔티티 리스트
    private val players = listOf(
        Player(
            initialX = 300f,
            initialY = 300f,
        ),
        Player(
            initialX = 600f,
            initialY = 300f,
            color = Color.Red,
        ),
        Player(
            initialX = 950f,
            initialY = 300f,
            color = Color.Green,
        ),
    )

    override fun update(deltaTime: Double) {
        if (viewportWidth > 0 && viewportHeight > 0) {
            players.forEach { it.update(deltaTime, viewportWidth, viewportHeight) }
        }
    }

    override fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        // 맵
        // context.drawRect(Color.Gray, Offset(0f, floorY), size = Size(viewportWidth, viewportHeight - floorY))

        // 엔티티
        players.forEach { it.render(context, textMeasurer, fontFamily) }
    }

    override fun setViewportSize(width: Float, height: Float) {
        viewportWidth = width
        viewportHeight = height
    }
}

actual fun getCanvas(): Canvas = WebCanvas()

actual fun createGameDrawScope(drawScope: DrawScope): GameDrawScope = WebDrawScope(drawScope)