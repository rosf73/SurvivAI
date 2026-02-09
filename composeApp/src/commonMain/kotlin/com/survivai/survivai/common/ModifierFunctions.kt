package com.survivai.survivai.common

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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