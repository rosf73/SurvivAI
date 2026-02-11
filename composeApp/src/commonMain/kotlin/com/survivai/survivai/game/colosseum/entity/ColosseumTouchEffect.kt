package com.survivai.survivai.game.colosseum.entity

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.GameDrawScope
import com.survivai.survivai.game.World
import com.survivai.survivai.game.colosseum.logic.ColosseumEngine
import com.survivai.survivai.game.component.Component
import com.survivai.survivai.game.sprite.ActionState

class ColosseumTouchEffect(
    startX: Float,
    startY: Float,
    private val gameEngine: ColosseumEngine,
) : Entity {

    override val name = "TouchEffect"
    override val signatureColor = Color.Black

    override var x = startX
    override var y = startY
    override var width = 0f
    override var height = 0f
    override var imageWidth = 0f
    override var imageHeight = 0f
    override var direction = Entity.Direction.LEFT
    override var state: Entity.State = ActionState.IDLE
    override val components: MutableList<Component> = mutableListOf()

    private var currentRadius = 10f
    private val maxRadius = 60f
    private val speed = 150f // pixels per second

    override fun update(deltaTime: Double, world: World) {
        val dt = deltaTime.toFloat()
        currentRadius += speed * dt

        if (currentRadius > maxRadius) {
            gameEngine.destroyEntity(this)
        }
    }

    override fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        val alpha = 1f - (currentRadius / maxRadius).coerceIn(0f, 1f)

        context.drawCircle(
            color = signatureColor.copy(alpha = alpha),
            radius = currentRadius,
            center = Offset(x, y),
            style = Stroke(width = 10f)
        )
    }
}
