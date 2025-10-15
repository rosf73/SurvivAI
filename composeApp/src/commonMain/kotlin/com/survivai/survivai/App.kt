package com.survivai.survivai

import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.toSize
import com.survivai.survivai.game.colosseum.createGameDrawScope
import com.survivai.survivai.game.colosseum.getCanvas
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    MaterialTheme {
        val textMeasurer = rememberTextMeasurer()
        val canvasState = remember { getCanvas() }

        // UI update state
        var frameTick by remember { mutableStateOf(0) }

        // 1. Set game loop
        var lastTime by remember { mutableStateOf(0L) }

        LaunchedEffect(Unit) {
            // Compose의 애니메이션 프레임 루프를 사용하여 매 프레임 업데이트를 요청
            while (true) {
                withFrameMillis { currentTime ->
                    if (lastTime > 0) {
                        val deltaTime = (currentTime - lastTime) / 1000.0 // 초 단위 deltaTime 계산
                        canvasState.update(deltaTime)
                    }
                    lastTime = currentTime

                    // Force recomposition
                    frameTick++
                }
            }
        }

        // 2. Rendering
        ComposeCanvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    canvasState.setViewportSize(size.toSize().width, size.toSize().height)
                }
        ) {
            // frameTick에 의존하여 매 프레임 리렌더링하기 위함
            val currentFrame = frameTick

            // Background
            drawRect(Color.White)

            // Draw circle
            val drawScopeWrapper = createGameDrawScope(this)
            canvasState.render(drawScopeWrapper, textMeasurer)
        }
    }
}