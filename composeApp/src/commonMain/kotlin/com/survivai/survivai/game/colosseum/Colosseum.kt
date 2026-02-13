package com.survivai.survivai.game.colosseum

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.survivai.survivai.common.LocalFont
import com.survivai.survivai.game.GameDrawScope
import com.survivai.survivai.game.colosseum.components.ColosseumEndScreen
import com.survivai.survivai.game.colosseum.components.ColosseumLogArea
import com.survivai.survivai.game.colosseum.components.ColosseumStartScreen
import com.survivai.survivai.game.colosseum.components.ScoreboardPopup
import com.survivai.survivai.game.colosseum.components.MainMenuPopup
import com.survivai.survivai.game.colosseum.components.PopupType
import com.survivai.survivai.game.colosseum.components.RematchPopup
import com.survivai.survivai.game.colosseum.logic.ColosseumEngine
import com.survivai.survivai.game.colosseum.logic.ColosseumState
import com.survivai.survivai.game.sprite.SpriteLoader
import kotlinx.coroutines.Dispatchers
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

    // popup state
    var currentPopup by remember { mutableStateOf<PopupType?>(null) }

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
            .background(Color.Black) // letter box
            .onPreviewKeyEvent { event ->
                if (event.key == Key.Tab) {
                    if (event.type == KeyEventType.KeyDown) {
                        currentPopup = PopupType.SCOREBOARD
                    } else if (event.type == KeyEventType.KeyUp) {
                        currentPopup = null
                    }
                    true
                } else {
                    false
                }
            },
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
                Canvas(
                    modifier = Modifier
                        .requiredSize( // fixed logical screen
                            width = with(density) { logicalWidth.toDp() },
                            height = with(density) { logicalHeight.toDp() },
                        )
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                        )
                        .then(
                            if (gameEngine.colosseumOptions.any { it.clickable }) {
                                Modifier.pointerInput(Unit) {
                                    detectTapGestures { offset ->
                                        coroutineScope.launch(Dispatchers.Main) {
                                            gameEngine.onScreenTouch(offset.x, offset.y)
                                        }
                                    }
                                }
                            } else Modifier
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

                // match buttons
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp),
                ) {
                    MatchButton("REMATCH", onClick = { currentPopup = PopupType.REMATCH })
                    Spacer(modifier = Modifier.size(6.dp))
                    MatchButton("MAIN MENU", onClick = { currentPopup = PopupType.MAIN_MENU })
                    Spacer(modifier = Modifier.size(6.dp))
                    MatchButton("DASHBOARD", onClick = { currentPopup = PopupType.SCOREBOARD })
                }
            }
            is ColosseumState.Ended -> {
                ColosseumEndScreen(
                    modifier = Modifier.fillMaxSize(),
                    statsList = currentColosseumState.statsList,
                    titles = currentColosseumState.titleList,
                    isLandscape = isLandscape,
                    onClickRestart = gameEngine::restart,
                    onClickReset = gameEngine::reset,
                )
            }
        }

        when (currentPopup) {
            PopupType.REMATCH -> RematchPopup(
                onClickYes = { gameEngine.restart(); currentPopup = null },
                onClickNo = { currentPopup = null },
                modifier = Modifier.fillMaxSize(),
            )
            PopupType.MAIN_MENU -> MainMenuPopup(
                onClickYes = { gameEngine.reset(); currentPopup = null },
                onClickNo = { currentPopup = null },
                modifier = Modifier.fillMaxSize(),
            )
            PopupType.SCOREBOARD -> ScoreboardPopup(
                statsList = emptyList(),
                onClickOutside = { currentPopup = null },
                modifier = Modifier.fillMaxSize(),
            )
            null -> { /* not showing */ }
        }
    }
}

@Composable
private fun MatchButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        modifier = modifier,
        shape = CutCornerShape(6.dp),
        border = BorderStroke(
            2.dp,
            Brush.verticalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00838F)))
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(6.dp),
        onClick = onClick,
    ) {
        Text(
            label,
            style = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 2f)
                ),
                letterSpacing = 2.sp
            )
        )
    }
}