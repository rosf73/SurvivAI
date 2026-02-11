package com.survivai.survivai.game.colosseum.entity

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.survivai.survivai.game.colosseum.logic.ColosseumEngine
import com.survivai.survivai.game.sprite.ActionState
import com.survivai.survivai.game.sprite.SpriteAnimationData
import com.survivai.survivai.game.sprite.SpriteLoader
import com.survivai.survivai.game.sprite.SpriteSheet
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class ColosseumEntityFactory(
    private val loader: SpriteLoader,
    private val gameEngine: ColosseumEngine,
) {
    suspend fun createPlayer(
        name: String,
        color: Color,
        startHp: Double,
    ): ColosseumPlayer = coroutineScope {
        val idleAnimation1 = async {
            loader.load(
                "sprite_colosseum_player_idle_1.png",
                SpriteAnimationData.fixed(
                    frameSize = Size(128f, 64f),
                    tintColorAlpha = 1.0f,
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

        val attackAnimation1 = async {
            loader.load(
                "sprite_colosseum_player_attack_1.png",
                SpriteAnimationData.sequenced(
                    totalFrame = 6,
                    durationPerFrame = 1.3 / 6, // TODO : temporary, refactor order between preparing and attacking
                    frameSize = Size(128f, 64f),
                    tintColorAlpha = 1.0f,
                    loop = false,
                    nextAction = ActionState.IDLE,
                )
            )
        }
        val attackAnimation2 = async {
            loader.load(
                "sprite_colosseum_player_attack_2.png",
                SpriteAnimationData.sequenced(
                    totalFrame = 6,
                    durationPerFrame = 1.3 / 6,
                    frameSize = Size(128f, 64f),
                    loop = false,
                    nextAction = ActionState.IDLE,
                ),
            )
        }
        val dieAnimation1 = async {
            loader.load(
                "sprite_colosseum_player_die_full.png",
                SpriteAnimationData.fixed(
                    frameSize = Size(128f, 64f),
                    tintColorAlpha = 1.0f,
                ),
            )
        }
        val dieAnimation2 = async {
            loader.load(
                "sprite_colosseum_player_die_empty.png",
                SpriteAnimationData.fixed(
                    frameSize = Size(128f, 64f),
                    tintColorAlpha = 0.5f,
                ),
            )
        }

        val sheet = SpriteSheet(
            imageSize = Size(128f, 64f),
            animations = mapOf(
                ActionState.IDLE to listOf(idleAnimation1.await(), idleAnimation2.await()),
                ActionState.ATTACK to listOf(attackAnimation1.await(), attackAnimation2.await()),
                ActionState.DIE to listOf(dieAnimation1.await(), dieAnimation2.await()),
            )
        )

        ColosseumPlayer(
            name, color, startHp, sheet, gameEngine,
        )
    }
}
