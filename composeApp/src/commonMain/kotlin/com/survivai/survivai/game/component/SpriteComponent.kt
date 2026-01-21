package com.survivai.survivai.game.component

import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.toIntSize
import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.World
import com.survivai.survivai.game.colosseum.GameDrawScope
import com.survivai.survivai.game.sprite.AnimationAction
import com.survivai.survivai.game.sprite.SpriteSheet

class SpriteComponent(
    val spriteSheet: SpriteSheet,
    var currentAction: AnimationAction = AnimationAction.IDLE,
) : Component() {
    private var elapsedTime: Double = 0.0
    private var currentFrame: Int = 0

    override fun update(deltaTime: Double, owner: Entity, world: World) {
        val animation = spriteSheet.get(currentAction) ?: return
        val data = animation.data

        elapsedTime += deltaTime

        // check frame duration
        val stepDuration = data.steps.getOrNull(currentFrame) ?: 0.1
        if (elapsedTime >= stepDuration) {
            elapsedTime -= stepDuration
            currentFrame++

            if (currentFrame >= data.frame) {
                if (data.loop) {
                    currentFrame = 0
                } else {
                    currentFrame = data.frame - 1 // fix state to last frame
                    // auto action switch
                    data.nextAction?.let {
                        currentAction = it
                        currentFrame = 0
                    }
                }
            }
        }
    }

    override fun render(context: GameDrawScope, owner: Entity, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        val animation = spriteSheet.get(currentAction) ?: return

        // get color component
        val color = owner.getComponent(ColorComponent::class)?.tintColor

        // get entity's location
        val dstOffset = IntOffset((owner.x - owner.width / 2).toInt(), (owner.y - owner.height / 2).toInt())
        val dstSize = IntSize(owner.width, owner.height)

        context.drawImage(
            image = animation.image,
            srcSize = animation.data.frameSize.toIntSize(),
            dstOffset = dstOffset,
            dstSize = dstSize,
            alpha = 0.5f,
            colorFilter = color?.let { ColorFilter.tint(it) },
        )
    }
}