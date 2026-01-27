package com.survivai.survivai.game.colosseum

import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.survivai.survivai.game.colosseum.components.ColosseumEndScreen
import com.survivai.survivai.game.colosseum.components.ColosseumLogArea
import com.survivai.survivai.game.colosseum.components.ColosseumStartScreen
import com.survivai.survivai.game.colosseum.entity.ColosseumPlayerFactory
import com.survivai.survivai.game.colosseum.state.ColosseumInfo
import com.survivai.survivai.game.colosseum.state.GameState
import com.survivai.survivai.game.sprite.SpriteLoader
import com.survivai.survivai.preloadEmojiFontForFallback
import kotlinx.coroutines.launch

@Composable
fun Colosseum(
    isLandscape: Boolean,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    val textMeasurer = rememberTextMeasurer()

    // font preload
    val fontFamilyResolver = LocalFontFamilyResolver.current
    preloadEmojiFontForFallback(fontFamilyResolver)

    val canvasState = remember { getCanvas() }
    val spriteLoader = remember { SpriteLoader() }
    val coroutineScope = rememberCoroutineScope()

    // game state for recomposition
    val currentGameState = ColosseumInfo.gameState.value

    // UI update state
    var frameTick by remember { mutableStateOf(0) }

    // 1. Set game loop
    var lastTime by remember { mutableStateOf(0L) }

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
    Box(modifier = modifier) {
        when (currentGameState) {
            GameState.WaitingForPlayers -> {
                ColosseumStartScreen(
                    modifier = Modifier.fillMaxSize(),
                    fontFamily = fontFamily,
                    onClickStart = { players, hp ->
                        // Set HP
                        ColosseumInfo.setDefaultHp(hp.toDouble())

                        // Set players
                        coroutineScope.launch {
                            val players = players.map { p ->
                                ColosseumPlayerFactory(spriteLoader).createPlayer(
                                    name = p.name,
                                    color = p.color,
                                    startHp = ColosseumInfo.defaultHp,
                                )
                            }
                            ColosseumInfo.setPlayers(players)
                        }
                    },
                )
            }
            is GameState.Playing -> {
                // Canvas (World + Players)
                ComposeCanvas(
                    modifier = Modifier
                        .align(if (isLandscape) Alignment.CenterStart else Alignment.TopCenter)
                        .fillMaxHeight(if (isLandscape) 1.0f else 0.6f)
                        .fillMaxWidth(if (isLandscape) 0.6f else 1.0f)
                        .onSizeChanged {
                            val size = it.toSize()
                            canvasState.setViewportSize(size.width, size.height)
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

                // Log
                Box(
                    modifier = Modifier
                        .align(if (isLandscape) Alignment.CenterEnd else Alignment.BottomCenter)
                        .fillMaxWidth(if(isLandscape) 0.4f else 1.0f)
                        .fillMaxHeight(if(isLandscape) 1.0f else 0.4f)
                ) {
                    ColosseumLogArea(
                        fontFamily = fontFamily,
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                    )
                }
            }
            is GameState.Ended -> {
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