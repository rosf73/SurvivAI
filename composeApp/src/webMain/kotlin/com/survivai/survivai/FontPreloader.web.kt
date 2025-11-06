package com.survivai.survivai

import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.ExperimentalResourceApi
import survivai.composeapp.generated.resources.Res
import androidx.compose.ui.text.platform.Font

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun preloadEmojiFontForFallback(fontFamilyResolver: FontFamily.Resolver) {
    var fontsLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            // 웹에서는 ByteArray로 로드해서 preload
            val emojiBytes = Res.readBytes("font/NotoEmojiColor.ttf")
            val emojiFont = FontFamily(
                Font("NotoEmojiColor", emojiBytes)
            )
            fontFamilyResolver.preload(emojiFont)
            fontsLoaded = true
            println("✅ Emoji font preloaded successfully")
        } catch (e: Exception) {
            println("❌ Failed to preload emoji font: ${e.message}")
        }
    }
}

