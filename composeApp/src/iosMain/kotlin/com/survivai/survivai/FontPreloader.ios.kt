package com.survivai.survivai

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

@Composable
actual fun preloadEmojiFontForFallback(fontFamilyResolver: FontFamily.Resolver) {
    // iOS는 시스템 이모지 폰트가 있으므로 preload 불필요
}

