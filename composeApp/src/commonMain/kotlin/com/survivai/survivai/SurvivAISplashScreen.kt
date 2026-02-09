package com.survivai.survivai

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.survivai.survivai.common.survivAIBackground

@Composable
fun SurvivAISplashScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .survivAIBackground(),
    )
}