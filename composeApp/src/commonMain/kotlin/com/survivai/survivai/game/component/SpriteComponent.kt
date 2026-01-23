package com.survivai.survivai.game.component

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.toIntSize
import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.Entity.Direction
import com.survivai.survivai.game.World
import com.survivai.survivai.game.colosseum.GameDrawScope
import com.survivai.survivai.game.sprite.ActionState
import com.survivai.survivai.game.sprite.SpriteSheet

class SpriteComponent(
    val spriteSheet: SpriteSheet,
) : Component() {
    private var elapsedTime: Double = 0.0
    private var currentFrame: Int = 0

    override fun update(deltaTime: Double, owner: Entity, world: World) {
        val animations = spriteSheet.get(owner.state) ?: return
        val data = animations.first().data

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
                        owner.state = it
                        currentFrame = 0
                    }
                }
            }
        }
    }

    override fun render(context: GameDrawScope, owner: Entity, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        val animations = spriteSheet.get(owner.state)
            ?: spriteSheet.get(ActionState.IDLE)
            ?: return

        // get color component
        val color = owner.getComponent(ColorComponent::class)?.tintColor

        // get entity's location
        val dstOffset = IntOffset((owner.x - owner.imageWidth / 2).toInt(), (owner.y - owner.imageHeight / 2).toInt())
        val dstSize = Size(owner.imageWidth, owner.imageHeight).toIntSize()

        animations.forEach { animation ->
            val safeFrame = currentFrame % animation.data.frame
            val frameWidth = animation.data.frameSize.width.toInt()
            val srcOffsetX = safeFrame * frameWidth
            val srcOffsetY = 0

            context.drawScaleImage(
                scaleX = if (owner.direction.isRight()) -1f else 1f,
                scaleY = 1f,
                pivot = Offset(owner.x, owner.y),
                image = animation.image,
                srcOffset = IntOffset(srcOffsetX, srcOffsetY),
                srcSize = animation.data.frameSize.toIntSize(),
                dstOffset = dstOffset,
                dstSize = dstSize,
                colorFilter = if (animation.data.useTintColor) { color?.let { ColorFilter.tint(it) } } else null,
            )
        }
    }
}