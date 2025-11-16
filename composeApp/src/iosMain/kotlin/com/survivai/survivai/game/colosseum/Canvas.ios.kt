package com.survivai.survivai.game.colosseum

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily

class IOSDrawScope(private val drawScope: DrawScope) : GameDrawScope {
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

    override fun drawPath(path: Path, color: Color) {
        drawScope.drawPath(
            path = path,
            color = color,
        )
    }
}

class IOSCanvas : Canvas {

    override fun update(deltaTime: Double) {
        // TODO : Update all player
    }

    override fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        // TODO : Update all player
    }

    override fun setViewportSize(width: Float, height: Float) {
        // TODO : Update all player
    }
}

actual fun getCanvas(): Canvas = IOSCanvas()

actual fun createGameDrawScope(drawScope: DrawScope): GameDrawScope = IOSDrawScope(drawScope)