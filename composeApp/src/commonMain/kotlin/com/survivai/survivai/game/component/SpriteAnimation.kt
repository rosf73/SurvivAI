package com.survivai.survivai.game.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.decodeToImageBitmap
import survivai.composeapp.generated.resources.Res

class SpriteAnimation(
    val image: ImageBitmap,
    val data: SpriteAnimationData,
) {
    companion object {
        @OptIn(ExperimentalResourceApi::class)
        suspend fun load(
            path: String,
            data: SpriteAnimationData,
        ): SpriteAnimation {
            val bytes = Res.readBytes(path)
            val bitmap = bytes.decodeToImageBitmap()
            return SpriteAnimation(bitmap, data)
        }
    }
}

data class SpriteAnimationData(
    val frame: Int,
    val steps: List<Double>,
    val frameSize: Size,
    val textureOffset: Offset?,
    val loop: Boolean,
) {
    companion object {
        fun sequenced(
            totalFrame: Int,
            durationPerFrame: Double,
            frameSize: Size,
            textureOffset: Offset?,
            loop: Boolean = true,
        ) = SpriteAnimationData(
            frame = totalFrame,
            steps = List(totalFrame) { durationPerFrame },
            frameSize = frameSize,
            textureOffset = textureOffset,
            loop = loop,
        )
    }
}