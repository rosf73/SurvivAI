package com.survivai.survivai.game.sprite

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ImageBitmap
import org.jetbrains.compose.resources.decodeToImageBitmap
import survivai.composeapp.generated.resources.Res

class SpriteAnimation(
    val image: ImageBitmap,
    val data: SpriteAnimationData,
) {
    companion object {
        suspend fun load(
            fileName: String,
            data: SpriteAnimationData,
        ): SpriteAnimation {
            val bytes = Res.readBytes("drawable/$fileName")
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
    val tintColorAlpha: Float,
    val loop: Boolean,
    val nextAction: ActionState?,
) {
    companion object {
        fun sequenced(
            totalFrame: Int,
            durationPerFrame: Double,
            frameSize: Size,
            textureOffset: Offset? = null,
            tintColorAlpha: Float = 0.0f,
            loop: Boolean = true,
            nextAction: ActionState? = null,
        ) = SpriteAnimationData(
            frame = totalFrame,
            steps = List(totalFrame) { durationPerFrame },
            frameSize = frameSize,
            textureOffset = textureOffset,
            tintColorAlpha = tintColorAlpha,
            loop = loop,
            nextAction = nextAction,
        )

        fun fixed(
            frameSize: Size,
            textureOffset: Offset? = null,
            tintColorAlpha: Float = 0.0f,
        ) = SpriteAnimationData(
            frame = 1,
            steps = emptyList(),
            frameSize = frameSize,
            textureOffset = textureOffset,
            tintColorAlpha = tintColorAlpha,
            loop = false,
            nextAction = null,
        )
    }
}