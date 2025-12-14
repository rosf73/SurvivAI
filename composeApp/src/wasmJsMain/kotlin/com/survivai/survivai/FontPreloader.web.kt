package com.survivai.survivai

import androidx.compose.runtime.*
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.ExperimentalResourceApi
import survivai.composeapp.generated.resources.Res
import androidx.compose.ui.text.platform.Font
import kotlinx.browser.window
import kotlinx.coroutines.await
import kotlinx.coroutines.delay
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.w3c.fetch.Response

@OptIn(ExperimentalResourceApi::class, ExperimentalWasmJsInterop::class)
@Composable
actual fun preloadEmojiFontForFallback(fontFamilyResolver: FontFamily.Resolver) {
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
}

