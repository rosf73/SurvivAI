package com.survivai.survivai

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.survivai.survivai.game.colosseum.CombatLogStore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import org.jetbrains.compose.resources.Font
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
            App()
        }

        // TODO : 전역 폰트 설정
        val fontFamily = FontFamily(Font(Res.font.NotoSansKR))
        // Log TODO : App()으로 이전
        Box(
            modifier = Modifier
                .align(if (isLandscape) Alignment.CenterEnd else Alignment.BottomCenter)
                .fillMaxWidth(if(isLandscape) 0.4f else 1.0f)
                .fillMaxHeight(if(isLandscape) 1.0f else 0.4f)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
                Text(
                    text = "LOG",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = fontFamily,
                )
                val entries = CombatLogStore.entries
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(entries) { line ->
                        Text(
                            text = line,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 2.dp),
                            fontFamily = fontFamily,
                        )
                    }
                }
            }
        }
    }
}