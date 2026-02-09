package com.survivai.survivai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.ExperimentalResourceApi
import kotlinx.browser.window
import kotlinx.browser.document

@OptIn(ExperimentalResourceApi::class, ExperimentalWasmJsInterop::class)
@Composable
actual fun preloadEmojiFontForFallback(fontFamilyResolver: FontFamily.Resolver): State<Boolean> {
//    var fontsLoaded by remember { mutableStateOf(false) }
//
//    LaunchedEffect(Unit) {
//        delay(5000) // 5초 대기
//        try {
//            // 웹에서는 ByteArray로 로드해서 preload
//            val emojiBytes = Res.readBytes("files/NotoEmojiColor.ttf")
//            val emojiFont = FontFamily(
//                Font("NotoEmojiColor", emojiBytes)
//            )
//            fontFamilyResolver.preload(emojiFont)
//            fontsLoaded = true
//            println("✅ Emoji font preloaded successfully")
//        } catch (e: Exception) {
//            println("❌ Failed to preload emoji font: ${e.message}")
//            e.printStackTrace()
//        }
//    }
    return remember { mutableStateOf(true) }
}

@OptIn(ExperimentalWasmJsInterop::class)
actual fun removePlatformSplashScreen() {
    val splashScreen = document.getElementById("splash-screen")
    splashScreen?.let {
        it.setAttribute("style", "opacity: 0; transition: opacity 0.5s ease-out;")
        window.setTimeout({
            it.remove()
            null
        }, 500)
    }
}
