package com.survivai.survivai.game.colosseum.entity

import androidx.compose.ui.graphics.Color
import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.World
import com.survivai.survivai.game.colosseum.logic.ColosseumEngine
import com.survivai.survivai.game.component.ColliderComponent
import com.survivai.survivai.game.component.Component
import com.survivai.survivai.game.component.SpriteComponent
import com.survivai.survivai.game.sprite.ActionState
import com.survivai.survivai.game.sprite.SpriteSheet
import kotlin.random.Random

class ColosseumRunningCar(
    val spriteSheet: SpriteSheet,
    val gameEngine: ColosseumEngine,
) : Entity {

    private val initialSize = (Random.nextFloat() + 0.5f) * spriteSheet.imageSize.width // 128.0 ~ 384.0
    private val colliderComponent = ColliderComponent(
        width = initialSize,
        height = initialSize,
    )
    private val spriteComponent = SpriteComponent(spriteSheet = spriteSheet).apply {
        alpha = INITIAL_ALPHA // start alpha
    }

    override val name = "뺑소니"
    override val signatureColor = Color.Red

    override var x: Float = Random.nextFloat() * (gameEngine.world.viewportWidth - colliderComponent.width)
    override var y: Float = gameEngine.world.viewportHeight - colliderComponent.height
    override var width = colliderComponent.width
    override var height = colliderComponent.height
    override var imageWidth = colliderComponent.width // same with collision size
    override var imageHeight = colliderComponent.height // same with collision size
    override var direction = setOf(Entity.Direction.LEFT, Entity.Direction.RIGHT).random()
    override var state: Entity.State = ActionState.IDLE

    override val components: MutableList<Component> = mutableListOf(
        spriteComponent,
        colliderComponent,
    )

    private val initialWidth = width
    private val aspectRatio = width / height
    // We want to reach FINAL_WIDTH in about 3 seconds
    private val growthSpeed = (FINAL_WIDTH - initialWidth) / 3f

    private var hasCrashed = false
    private var postCrashTimer = 0.0

    override fun update(deltaTime: Double, world: World) {
        super.update(deltaTime, world)

        // Growth logic
        val growth = (growthSpeed * deltaTime).toFloat()
        val newWidth = width + growth

        width = newWidth
        height = width / aspectRatio
        imageWidth = width
        imageHeight = height

        // Update alpha
        val progress = ((width - initialWidth) / (FINAL_WIDTH - initialWidth)).coerceIn(0f, 1f)
        spriteComponent.alpha = INITIAL_ALPHA + (0.7f * progress)

        // Check crash condition
        if (!hasCrashed && width >= FINAL_WIDTH) {
            hasCrashed = true
            onCrashed()
        }

        // Post crash logic
        if (hasCrashed) {
            postCrashTimer += deltaTime
            if (postCrashTimer >= END_LAG) {
                gameEngine.destroyEntity(this) // TODO : pooling
            }
        }
    }

    private fun onCrashed() {

    }

    companion object {
        private const val INITIAL_ALPHA = 0.3f
        private const val END_LAG = 3 // post delay (sec)
        private const val FINAL_WIDTH = 960f
    }
}