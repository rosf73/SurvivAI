package com.survivai.survivai

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
actual fun preloadEmojiFontForFallback(fontFamilyResolver: FontFamily.Resolver): State<Boolean> {
    val fontsLoaded = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            // 웹에서는 ByteArray로 로드해서 preload
            val emojiBytes = Res.readBytes("font/NotoEmojiColor.ttf")
            val emojiFont = FontFamily(
                Font("NotoEmojiColor", emojiBytes)
            )
            fontFamilyResolver.preload(emojiFont)
            fontsLoaded.value = true
            println("✅ Emoji font preloaded successfully")
        } catch (e: Exception) {
            println("❌ Failed to preload emoji font: ${e.message}")
            e.printStackTrace()
            fontsLoaded.value = true
        }
    }
    return fontsLoaded
}

