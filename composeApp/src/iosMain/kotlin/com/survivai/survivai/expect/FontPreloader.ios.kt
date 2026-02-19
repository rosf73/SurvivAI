package com.survivai.survivai.expect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily

@Composable
actual fun preloadEmojiFontForFallback(fontFamilyResolver: FontFamily.Resolver): State<Boolean> {
    // iOS는 시스템 이모지 폰트가 있으므로 preload 불필요
    return remember { mutableStateOf(true) }
}

actual fun removePlatformSplashScreen() {
    // iOS doesn't use HTML splash
}
