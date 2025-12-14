package com.survivai.survivai.game.colosseum

import androidx.annotation.FloatRange
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize

interface GameDrawScope {
    fun drawCircle(color: Color, center: Offset, radius: Float)
    fun drawArc(color: Color, topLeft: Offset, width: Float, height: Float, startAngle: Float, sweepAngle: Float, useCenter: Boolean)
    fun drawRect(color: Color, topLeft: Offset, width: Float, height: Float)
    fun drawText(textMeasurer: TextMeasurer, text: String, topLeft: Offset, size: Size = Size.Unspecified, style: TextStyle = TextStyle.Default, softWrap: Boolean = true)
    fun drawPath(path: Path, color: Color)
    fun drawImage(
        image: ImageBitmap,
        srcOffset: IntOffset = IntOffset.Zero,
        srcSize: IntSize = IntSize(image.width, image.height),
        dstOffset: IntOffset = IntOffset.Zero,
        dstSize: IntSize = srcSize,
        @FloatRange(from = 0.0, to = 1.0) alpha: Float = 1.0f,
        style: DrawStyle = Fill,
        colorFilter: ColorFilter? = null,
    )
}

interface Canvas {
    fun update(deltaTime: Double)
    fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily)
    fun setViewportSize(width: Float, height: Float)
}

expect fun getCanvas(): Canvas

expect fun createGameDrawScope(drawScope: DrawScope): GameDrawScope