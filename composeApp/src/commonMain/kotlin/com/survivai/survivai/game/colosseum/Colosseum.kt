package com.survivai.survivai.game.colosseum

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.survivai.survivai.common.LocalFont
import com.survivai.survivai.game.GameDrawScope
import com.survivai.survivai.game.colosseum.components.ColosseumEndScreen
import com.survivai.survivai.game.colosseum.components.ColosseumLogArea
import com.survivai.survivai.game.colosseum.components.ColosseumStartScreen
import com.survivai.survivai.game.colosseum.logic.ColosseumEngine
import com.survivai.survivai.game.colosseum.logic.ColosseumState
import com.survivai.survivai.game.sprite.SpriteLoader
import kotlinx.coroutines.launch

@Composable
fun Colosseum(
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()
    val font = LocalFont.current

    val spriteLoader = remember { SpriteLoader() }
    val gameEngine = remember { ColosseumEngine(spriteLoader) }
    val coroutineScope = rememberCoroutineScope()

    // game state for recomposition
    val currentColosseumState = gameEngine.gameState.value

    // UI update state
    var frameTick by remember { mutableStateOf(0) }

    // 1. Set game loop
    var lastTime by remember { mutableStateOf(0L) }

    LaunchedEffect(currentColosseumState) {
        lastTime = 0L  // 재시작 시 타이머 리셋

        // Compose의 애니메이션 프레임 루프를 사용하여 매 프레임 업데이트를 요청
        while (gameEngine.gameState.value is ColosseumState.Playing) {
            withFrameMillis { currentTime ->
                if (lastTime > 0) {
                    val deltaTime = (currentTime - lastTime) / 1000.0 // 초 단위 deltaTime 계산
                    gameEngine.update(deltaTime * 2)
                }
                lastTime = currentTime

                // Force recomposition
                frameTick++
            }
        }
    }

    // 2. Rendering
    BoxWithConstraints(
        modifier = modifier
            .background(Color.Black), // letter box
        contentAlignment = Alignment.Center,
    ) {
        val density = LocalDensity.current

        val screenWidthPx = with(density) { maxWidth.toPx() }
        val screenHeightPx = with(density) { maxHeight.toPx() }

        val logicalWidth = 1920f
        val logicalHeight = 1080f
        val targetRatio = logicalWidth / logicalHeight

        val isWide = screenWidthPx / screenHeightPx > targetRatio

        val scale = if (isWide) {
            screenHeightPx / logicalHeight
        } else {
            screenWidthPx / logicalWidth
        }

        when (currentColosseumState) {
            ColosseumState.WaitingForPlayers -> {
                ColosseumStartScreen(
                    modifier = Modifier.fillMaxSize(),
                    isLandscape = isLandscape,
                    onClickStart = { players, hp, options ->
                        coroutineScope.launch {
                            gameEngine.playGame(
                                playerInitList = players,
                                startHp = hp.toDouble(),
                                options = options,
                            )
                        }
                    },
                )
            }
            is ColosseumState.Playing -> {
                // Canvas (World + Players)
                ComposeCanvas(
                    modifier = Modifier
                        .requiredSize( // fixed logical screen
                            width = with(density) { logicalWidth.toDp() },
                            height = with(density) { logicalHeight.toDp() },
                        )
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                        )
                        .onSizeChanged {
                            val size = it.toSize()
                            gameEngine.setViewportSize(size.width, size.height)
                        }
                ) {
                    // frameTick에 의존하여 매 프레임 리렌더링하기 위함
                    val currentFrame = frameTick

                    // Background
                    drawRect(Color.White)

                    // Draw circle
                    val drawScopeWrapper = GameDrawScope.getInstance(this)
                    gameEngine.render(drawScopeWrapper, textMeasurer, fontFamily = font)
                }

                // Log
                Box(
                    modifier = Modifier
                        .align(if (isLandscape) Alignment.TopEnd else Alignment.BottomCenter)
                        .fillMaxWidth(if(isLandscape) 0.4f else 1.0f)
                        .fillMaxHeight(if(isLandscape) 0.5f else 0.4f)
                ) {
                    ColosseumLogArea(
                        gameEngine = gameEngine,
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                    )
                }
            }
            is ColosseumState.Ended -> {
                ColosseumEndScreen(
                    modifier = Modifier.fillMaxSize(),
                    statsList = currentColosseumState.statsList,
                    titles = currentColosseumState.titleList,
                    isLandscape = isLandscape,
                    onClickRestart = {
                        // 바로 재시작 (플레이어 유지)
                        gameEngine.restart()
                        gameEngine.clearLog()
                    },
                    onClickReset = {
                        // 경기 재설정 (처음부터)
                        gameEngine.reset()
                        gameEngine.clearLog()
                    },
                )
            }
        }
    }
}