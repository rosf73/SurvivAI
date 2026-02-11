package com.survivai.survivai.game.colosseum.entity

import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.World
import com.survivai.survivai.game.colosseum.logic.ColosseumEngine
import com.survivai.survivai.game.colosseum.world.ColosseumWorld
import com.survivai.survivai.game.component.ColliderComponent
import com.survivai.survivai.game.component.Component
import com.survivai.survivai.game.component.SpriteComponent
import com.survivai.survivai.game.sprite.ActionState
import com.survivai.survivai.game.sprite.SpriteSheet
import kotlin.random.Random

class ColosseumFallingRock(
    val spriteSheet: SpriteSheet,
    val gameEngine: ColosseumEngine,
) : Entity {

    private val colliderComponent = ColliderComponent(
        width = Random.nextFloat() * 128 + 64, // 64.0 ~ 192.0
        height = Random.nextFloat() * 128 + 64, // 64.0 ~ 192.0
    )

    override var x: Float = Random.nextFloat() * 1920
    override var y: Float = -128f // ceiling
    override var width = colliderComponent.width
    override var height = colliderComponent.height
    override var imageWidth = colliderComponent.width // same with collision size
    override var imageHeight = colliderComponent.height // same with collision size
    override var direction = setOf(Entity.Direction.LEFT, Entity.Direction.RIGHT).random()
    override var state: Entity.State = ActionState.IDLE

    override val components: MutableList<Component> = mutableListOf(
        SpriteComponent(spriteSheet = spriteSheet),
        colliderComponent,
    )

    // Physics
    var velocityY = 0f
    private val gravity = 1500f
    private val restitution = 0.4f
    var hasBounced = false
    var isAlive = true

    override fun update(deltaTime: Double, world: World) {
        if (!isAlive) return
        super.update(deltaTime, world)

        val dt = deltaTime.toFloat()

        // Gravity
        velocityY += gravity * dt
        val prevY = y
        y += velocityY * dt

        // Bounce on platform
        if (!hasBounced && velocityY > 0) {
            val fallingRockBottom = y + height / 2
            val prevBottom = prevY + height / 2

            // Check platforms
            (world as? ColosseumWorld)?.getPlatforms()?.forEach { p ->
                // Horizontal overlap (Check collision with the center of the rock)
                val inHorizontalRange = x >= p.left && x <= p.right
                
                // Vertical passing through
                val passedThrough = p.top in prevBottom..fallingRockBottom
                
                if (inHorizontalRange && passedThrough) {
                    y = p.top - height / 2
                    velocityY = -velocityY * restitution
                    hasBounced = true
                }
            }
        }

        // Check if out of bounds (removal)
        if (y > world.viewportHeight + height) {
            gameEngine.destroyEntity(this)
        }
    }
}