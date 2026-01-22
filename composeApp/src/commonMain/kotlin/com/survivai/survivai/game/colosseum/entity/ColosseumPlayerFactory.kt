package com.survivai.survivai.game.colosseum.entity

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.survivai.survivai.game.sprite.ActionState
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
        color: Color,
        startHp: Double,
        ripIcons: Pair<ImageBitmap, ImageBitmap>,
    ): ColosseumPlayer = coroutineScope {
        val idleAnimation1 = async {
            loader.load(
                "sprite_colosseum_player_idle_1.png",
                SpriteAnimationData.fixed(
                    frameSize = Size(128f, 64f),
                    useTintColor = true,
                ),
            )
        }
        val idleAnimation2 = async {
            loader.load(
                "sprite_colosseum_player_idle_2.png",
                SpriteAnimationData.fixed(
                    frameSize = Size(128f, 64f),
                ),
            )
        }
        val attackAnimation = async {
            loader.load(
                "sprite_colosseum_attack_right.png", // temporary
                SpriteAnimationData.sequenced(
                    totalFrame = 3,
                    durationPerFrame = 1.2,
                    frameSize = Size(1),
                    loop = false,
                    nextAction = ActionState.IDLE,
                ),
            )
        }
        val dieAnimation = async {
            loader.load(
                "icon_r_i_p_full.png",
                SpriteAnimationData.fixed(
                    frameSize = Size(1),
                ),
            )
        }

        val sheet = SpriteSheet(mapOf(
            ActionState.IDLE to listOf(idleAnimation1.await(), idleAnimation2.await()),
            ActionState.ATTACK to listOf(attackAnimation.await()),
            ActionState.DIE to listOf(dieAnimation.await()),
        ))

        ColosseumPlayer(
            name, color, startHp, ripIcons, sheet
        )
    }
}
