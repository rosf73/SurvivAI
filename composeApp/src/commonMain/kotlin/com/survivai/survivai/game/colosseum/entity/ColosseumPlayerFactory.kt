package com.survivai.survivai.game.colosseum.entity

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.survivai.survivai.game.sprite.AnimationAction
import com.survivai.survivai.game.sprite.SpriteAnimationData
import com.survivai.survivai.game.sprite.SpriteLoader
import com.survivai.survivai.game.sprite.SpriteSheet
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ColosseumPlayerFactory(
    private val loader: SpriteLoader,
) {
    suspend fun createPlayer(
        name: String,
        radius: Float,
        color: Color,
        startHp: Int,
        ripIcons: Pair<ImageBitmap, ImageBitmap>,
    ): ColosseumPlayer = coroutineScope {
        val attackAnimation = async {
            loader.load(
                "sprite_colosseum_attack_right.png", // temporary
                SpriteAnimationData.sequenced(
                    totalFrame = 3,
                    durationPerFrame = 1.2,
                    frameSize = Size(1),
                    loop = false,
                    nextAction = AnimationAction.IDLE,
                ),
            )
        }
        val sheet = SpriteSheet(mapOf(
//            AnimationAction.IDLE to ...,
            AnimationAction.ATTACK to attackAnimation.await(),
        ))

        ColosseumPlayer(
            name, radius, color, startHp, ripIcons,
        )
    }
}
