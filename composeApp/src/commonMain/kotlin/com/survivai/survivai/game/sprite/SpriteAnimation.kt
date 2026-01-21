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
            val bytes = Res.readBytes("composeApp/src/commonMain/composeResources/drawable/$fileName")
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
    val nextAction: AnimationAction?,
) {
    companion object {
        fun sequenced(
            totalFrame: Int,
            durationPerFrame: Double,
            frameSize: Size,
            textureOffset: Offset? = null,
            loop: Boolean = true,
            nextAction: AnimationAction? = null,
        ) = SpriteAnimationData(
            frame = totalFrame,
            steps = List(totalFrame) { durationPerFrame },
            frameSize = frameSize,
            textureOffset = textureOffset,
            loop = loop,
            nextAction = nextAction,
        )
    }
}