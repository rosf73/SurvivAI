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
import com.survivai.survivai.game.colosseum.GameDrawScope
import com.survivai.survivai.game.colosseum.state.ColosseumInfo
import com.survivai.survivai.game.colosseum.state.GameState
import com.survivai.survivai.game.colosseum.components.ColosseumEndScreen
import com.survivai.survivai.game.colosseum.components.ColosseumStartScreen
import com.survivai.survivai.game.colosseum.entity.ColosseumPlayer
import com.survivai.survivai.game.colosseum.getCanvas
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.imageResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import survivai.composeapp.generated.resources.NotoEmojiColor
import survivai.composeapp.generated.resources.NotoSansKR
import survivai.composeapp.generated.resources.Res
import survivai.composeapp.generated.resources.icon_r_i_p_empty
import survivai.composeapp.generated.resources.icon_r_i_p_full

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

        // 게임 상태 추적
        val currentGameState = ColosseumInfo.gameState.value

        LaunchedEffect(currentGameState) {
            lastTime = 0L  // 재시작 시 타이머 리셋

            // Compose의 애니메이션 프레임 루프를 사용하여 매 프레임 업데이트를 요청
            while (ColosseumInfo.gameState.value is GameState.Playing) {
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
            val ripEmptyIcon = imageResource(Res.drawable.icon_r_i_p_empty)
            val ripFullIcon = imageResource(Res.drawable.icon_r_i_p_full)

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
                val drawScopeWrapper = GameDrawScope.getInstance(this)
                canvasState.render(drawScopeWrapper, textMeasurer, fontFamily)
            }

            // Start Screen Overlay
            if (currentGameState == GameState.WaitingForPlayers) {
                ColosseumStartScreen(
                    modifier = Modifier.fillMaxSize(),
                    fontFamily = fontFamily,
                    onClickStart = { players, hp ->
                        // Set HP
                        ColosseumInfo.setDefaultHp(hp)

                        // 중복 없는 색상 생성
                        val players = players.map { p ->
                            ColosseumPlayer(
                                name = p.name,
                                color = p.color,
                                startHp = hp,
                                ripIcons = ripEmptyIcon to ripFullIcon,
                            )
                        }
                        ColosseumInfo.setPlayers(players)
                    },
                )
            }

            // End Screen Overlay
            if (currentGameState is GameState.Ended) {
                ColosseumEndScreen(
                    modifier = Modifier.fillMaxSize(),
                    statsList = currentGameState.statsList,
                    titles = currentGameState.titleList,
                    fontFamily = fontFamily,
                    onClickRestart = {
                        // 바로 재시작 (플레이어 유지)
                        ColosseumInfo.restart()
                    },
                    onClickReset = {
                        // 경기 재설정 (처음부터)
                        ColosseumInfo.reset()
                    },
                )
            }
        }
    }
}
