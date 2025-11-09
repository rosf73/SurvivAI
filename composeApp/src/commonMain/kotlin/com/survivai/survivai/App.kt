package com.survivai.survivai

import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.toSize
import com.survivai.survivai.game.colosseum.ColosseumInfo
import com.survivai.survivai.game.colosseum.components.ColosseumStartScreen
import com.survivai.survivai.game.colosseum.createGameDrawScope
import com.survivai.survivai.game.colosseum.entity.Player
import com.survivai.survivai.game.colosseum.entity.generateUniqueColors
import com.survivai.survivai.game.colosseum.getCanvas
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.ui.tooling.preview.Preview
import survivai.composeapp.generated.resources.NotoEmojiColor
import survivai.composeapp.generated.resources.NotoSansKR
import survivai.composeapp.generated.resources.Res

@Composable
@Preview
fun App(
    onUpdatedViewport: (Float, Float) -> Unit = { _, _ -> },
) {
    MaterialTheme {
        val textMeasurer = rememberTextMeasurer()

        // 플랫폼별로 이모지 폰트 preload
        val fontFamilyResolver = LocalFontFamilyResolver.current
        preloadEmojiFontForFallback(fontFamilyResolver)

        val fontFamily = FontFamily(
            Font(Res.font.NotoSansKR),
            Font(Res.font.NotoEmojiColor),
        )
        val canvasState = remember { getCanvas() }

        // UI update state
        var frameTick by remember { mutableStateOf(0) }

        // 1. Set game loop
        var lastTime by remember { mutableStateOf(0L) }

        // 게임 실행 상태 추적
        val gameRestartTrigger = ColosseumInfo.fullUpdateState.value
        val isGameRunning = ColosseumInfo.isGameRunning.value

        // 게임 시작 여부 상태 (재시작 시 리셋)
        var gameStarted by remember(gameRestartTrigger) { mutableStateOf(false) }

        LaunchedEffect(gameRestartTrigger) {
            lastTime = 0L  // 재시작 시 타이머 리셋

            // Compose의 애니메이션 프레임 루프를 사용하여 매 프레임 업데이트를 요청
            while (ColosseumInfo.isGameRunning.value) {
                withFrameMillis { currentTime ->
                    if (lastTime > 0) {
                        val deltaTime = (currentTime - lastTime) / 1000.0 // 초 단위 deltaTime 계산
                        canvasState.update(deltaTime * 2)
                    }
                    lastTime = currentTime

                    // Force recomposition
                    frameTick++
                }
            }
        }

        // 2. Rendering
        Box(modifier = Modifier.fillMaxSize()) {
            // Canvas (World + Players)
            ComposeCanvas(
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged {
                        val size = it.toSize()
                        canvasState.setViewportSize(size.width, size.height)
                        onUpdatedViewport(size.width, size.height)
                    }
            ) {
                // frameTick에 의존하여 매 프레임 리렌더링하기 위함
                val currentFrame = frameTick

                // Background
                drawRect(Color.White)

                // Draw circle
                val drawScopeWrapper = createGameDrawScope(this)
                canvasState.render(drawScopeWrapper, textMeasurer, fontFamily)
            }

            // Start Screen Overlay
            if (!gameStarted) {
                ColosseumStartScreen(
                    modifier = Modifier.fillMaxSize(),
                    fontFamily = fontFamily,
                    onClickStart = { playerNames, hp ->
                        // 빈 이름 필터링 및 플레이어 생성
                        val validNames = playerNames.filter { it.isNotBlank() }
                        if (validNames.size >= 2) {
                            // HP 설정
                            ColosseumInfo.setDefaultHp(hp)
                            
                            // 중복 없는 색상 생성
                            val colors = generateUniqueColors(validNames.size)
                            val players = validNames.mapIndexed { index, name ->
                                Player(
                                    name = name,
                                    color = colors[index],
                                    startHp = hp
                                )
                            }
                            ColosseumInfo.setPlayers(players)
                            gameStarted = true
                        }
                    },
                )
            }
        }
    }
}
