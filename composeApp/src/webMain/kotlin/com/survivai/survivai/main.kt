package com.survivai.survivai

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import com.survivai.survivai.common.createGitHubIcon
import com.survivai.survivai.config.BuildConfig
import com.survivai.survivai.game.colosseum.ColosseumInfo
import com.survivai.survivai.game.colosseum.GameState
import com.survivai.survivai.game.colosseum.components.ColosseumLogArea
import org.jetbrains.compose.resources.Font
import survivai.composeapp.generated.resources.NotoEmojiColor
import survivai.composeapp.generated.resources.NotoSansKR
import survivai.composeapp.generated.resources.Res

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        ResponsiveRoot()
    }
}

@Composable
private fun ResponsiveRoot() {
    val containerSize = LocalWindowInfo.current.containerSize
    val isLandscape = containerSize.width >= containerSize.height

    // 게임 상태 추적 (recomposition 트리거용)
    val currentGameState = ColosseumInfo.gameState.value

    Box(modifier = Modifier.fillMaxSize()) {
        // Colosseum
        Box(
            modifier = Modifier
                .align(
                    if (isLandscape) Alignment.CenterStart else Alignment.TopCenter
                )
                .fillMaxHeight(if (isLandscape) 1.0f else 0.6f)
                .fillMaxWidth(if (isLandscape) 0.6f else 1.0f)
        ) {
            App { w, h ->
                // Viewport 크기를 ColosseumInfo에 전달 (자동 초기화)
                ColosseumInfo.setViewportSize(w, h)
            }
        }

        val fontFamily = FontFamily(
            Font(Res.font.NotoSansKR),
            Font(Res.font.NotoEmojiColor),
        )

        // Log TODO : App()으로 이전
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

        // Top right buttons
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // Version
            VersionText(
                modifier = Modifier.align(Alignment.CenterVertically),
                fontFamily = fontFamily,
            )

            // GitHub Link Button
            Button(
                onClick = {
                    window.open("https://github.com/rosf73/SurvivAI", "_blank")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White,
                ),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // GitHub 아이콘
                    Icon(
                        imageVector = createGitHubIcon(),
                        contentDescription = "GitHub",
                        modifier = Modifier.size(18.dp),
                        tint = Color.White,
                    )
                    Text(
                        text = "GitHub",
                        fontFamily = fontFamily,
                    )
                }
            }

            // 바로 재시작 버튼 (게임 진행 중이거나 종료 시에만 표시)
            if (currentGameState is GameState.Playing || currentGameState is GameState.Ended) {
                Button(
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green,
                        contentColor = Color.White,
                    ),
                    onClick = {
                        ColosseumInfo.restart()
                    },
                ) {
                    Text(
                        text = "바로 재시작",
                        fontFamily = fontFamily,
                    )
                }
            }

            // 재설정 버튼
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Cyan,
                    contentColor = Color.Blue,
                ),
                onClick = {
                    ColosseumInfo.reset()
                },
            ) {
                Text(
                    text = "경기 재설정",
                    fontFamily = fontFamily,
                )
            }
        }
    }
}

@Composable
private fun VersionText(
    modifier: Modifier = Modifier,
    fontFamily: FontFamily,
) {
    Spacer(modifier = Modifier.size(5.dp))
    Text(
        modifier = modifier,
        text = "v${BuildConfig.VERSION_NAME}",
        style = TextStyle(fontSize = 12.sp, color = Color.Gray, fontFamily = fontFamily),
    )
    Spacer(modifier = Modifier.size(5.dp))
}