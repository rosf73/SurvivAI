package com.survivai.survivai

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeViewport
import org.jetbrains.compose.ui.tooling.preview.Preview

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
        // Keep a single App() composition and size it to share space with the Log panel
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

        Box(modifier = Modifier
            .align(if (isLandscape) Alignment.CenterEnd else Alignment.BottomCenter)
            .fillMaxWidth(if(isLandscape) 0.4f else 1.0f)
            .fillMaxHeight(if(isLandscape) 1.0f else 0.4f)) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    "하이루",
                    modifier = Modifier.fillMaxWidth().padding(all = 20.dp),
                )
            }
        }
    }
}