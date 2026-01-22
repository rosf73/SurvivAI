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
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlin.concurrent.Volatile

class GameDrawScope private constructor(
    private val drawScope: DrawScope,
) {
    fun drawCircle(
        color: Color,
        center: Offset,
        radius: Float,
    ) {
        drawScope.drawCircle(
            color = color,
            center = center,
            radius = radius,
        )
    }

    fun drawArc(
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

    fun drawRect(
        color: Color,
        topLeft: Offset,
        width: Float,
        height: Float,
    ) {
        drawScope.drawRect(
            color = color,
            topLeft = topLeft,
            size = Size(width, height),
        )
    }

    fun drawText(
        textMeasurer: TextMeasurer,
        text: String,
        topLeft: Offset,
        size: Size = Size.Unspecified,
        style: TextStyle = TextStyle.Default,
        softWrap: Boolean = true,
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

    fun drawPath(
        path: Path,
        color: Color,
    ) {
        drawScope.drawPath(
            path = path,
            color = color,
        )
    }

    fun drawImage(
        image: ImageBitmap,
        srcOffset: IntOffset = IntOffset.Zero,
        srcSize: IntSize = IntSize(image.width, image.height),
        dstOffset: IntOffset = IntOffset.Zero,
        dstSize: IntSize = srcSize,
        @FloatRange(from = 0.0, to = 1.0) alpha: Float = 1.0f,
        style: DrawStyle = Fill,
        colorFilter: ColorFilter? = null,
    ) {
        drawScope.drawImage(
            image = image,
            srcOffset = srcOffset,
            srcSize = srcSize,
            dstOffset = dstOffset,
            dstSize = dstSize,
            alpha = alpha,
            style = style,
            colorFilter = colorFilter,
        )
    }

    companion object {
        @Volatile
        private var instance: GameDrawScope? = null
        private val lock = SynchronizedObject()

        fun getInstance(drawScope: DrawScope): GameDrawScope {
            return instance ?: synchronized(lock) {
                instance ?: GameDrawScope(drawScope).also { instance = it }
            }
        }
    }
}

interface Canvas {
    fun update(deltaTime: Double)
    fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily)
    fun setViewportSize(width: Float, height: Float)
}

expect fun getCanvas(): Canvas