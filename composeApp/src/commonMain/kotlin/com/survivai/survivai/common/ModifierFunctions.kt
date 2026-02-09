package com.survivai.survivai.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Modifier.squareVerticalScrollbar(
    state: ScrollState,
    width: Dp = 4.dp,
    color: Color = Color.Gray.copy(alpha = 0.5f),
    padding: Dp = 2.dp,
): Modifier {
    return drawWithContent {
        drawContent()

        val viewportHeight = size.height
        val totalHeight = state.maxValue + viewportHeight
        // not scrollable
        if (totalHeight <= viewportHeight) return@drawWithContent

        val thumbHeight = viewportHeight * (viewportHeight / totalHeight)
        val thumbOffsetY = state.value * (viewportHeight / totalHeight)

        drawRect(
            color = color,
            topLeft = Offset(this.size.width - width.toPx()- padding.toPx(), thumbOffsetY),
            size = Size(width.toPx(), thumbHeight),
        )
    }
}

@Composable
fun Modifier.survivAIBackground(): Modifier {
    // dark vibe
    return background(Color(0xFF0A0A0A))
        .drawBehind {
            // fine grid pattern
            val gridSize = 20.dp.toPx()
            for (x in 0..size.width.toInt() step gridSize.toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(x.toFloat(), 0f),
                    end = Offset(x.toFloat(), size.height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..size.height.toInt() step gridSize.toInt()) {
                drawLine(
                    color = Color.White.copy(alpha = 0.05f),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(size.width, y.toFloat()),
                    strokeWidth = 1f
                )
            }
        }
}