package com.survivai.survivai.game.colosseum.entity

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.World
import com.survivai.survivai.game.GameDrawScope
import com.survivai.survivai.game.colosseum.logic.ColosseumEngine
import com.survivai.survivai.game.colosseum.world.ColosseumWorld
import com.survivai.survivai.game.component.ColliderComponent
import com.survivai.survivai.game.component.ColorComponent
import com.survivai.survivai.game.component.CombatComponent
import com.survivai.survivai.game.component.Component
import com.survivai.survivai.game.component.SpriteComponent
import com.survivai.survivai.game.sprite.ActionState
import com.survivai.survivai.game.sprite.SpriteSheet
import kotlin.enums.EnumEntries
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

data class ColosseumPlayer(
    val name: String,
    val color: Color,
    private val startHp: Double,
    val spriteSheet: SpriteSheet,
    val gameEngine: ColosseumEngine,
) : Entity {

    private val combatComponent = CombatComponent(hp = startHp, invincibilityTime = INVINCIBLE_DURATION)
    private val colliderComponent = ColliderComponent(width = 64f, height = 64f)

    // Position (Center offset)
    override var x = 0f
    override var y = 0f
    override var width = colliderComponent.width
    override var height = colliderComponent.height
    override var imageWidth = spriteSheet.imageSize.width
    override var imageHeight = spriteSheet.imageSize.height
    override var direction = setOf(Entity.Direction.LEFT, Entity.Direction.RIGHT).random()
    override var state: Entity.State = ActionState.IDLE

    override val components: MutableList<Component> = mutableListOf(
        SpriteComponent(spriteSheet = spriteSheet),
        ColorComponent(tintColor = color),
        colliderComponent,
        combatComponent,
    )

    val halfWidth get() = width / 2
    val halfHeight get() = height / 2

    var velocityX = 0f // 수평 속도
    var velocityY = 0f // 수직 속도
    private val gravity = 980f // 중력 가속도 (픽셀/초^2)

    // Viewport
    private val viewportWidth get() = gameEngine.world.viewportWidth
    private val viewportHeight get() = gameEngine.world.viewportHeight
    private val floorY: Float
        get() = viewportHeight - halfHeight

    // Behavior
    private val facingRight get() = direction == Entity.Direction.RIGHT
    private var attackState = AttackState.NONE
    private var attackTimer = 0f
    val attackReach get() = width * 2 // TODO : 가변 변수, 애니메이션과 적절히 맞도록 연산
    private var isSpeeching = false
    private var speechTimer = 0f
    private var selectedSpeechList = listOf("")
    private var speechIndex = 0
    private var cachedTextSize: Size? = null

    // Collision state
    private var onPlatform = false

    // Random
    private var idleTime = 1f // 1초 후 시작
    private var inAction = false

    // HP
    val hp: Double get() = combatComponent.hp

    // 생존 여부
    val isAlive: Boolean get() = combatComponent.isAlive

    // Event flags
    private var justSpeeched = ""

    // Public read-only views
    val isAttackingNow: Boolean get() = attackState == AttackState.EXECUTING
    val isFacingRight: Boolean get() = facingRight
    val isPreparingAttack: Boolean get() = attackState == AttackState.PREPARING

    // Point, Score
    var attackPoint = 0
    var killPoint = 0
    var deathTime = 0L

    // 적들과의 상대적 위치 정보 (매 프레임 업데이트)
    private var nearestEnemyDistance: Float = Float.MAX_VALUE
    private var nearestEnemyInFront: Boolean = false // 가장 가까운 적이 전방에 있는지
    private var enemiesInAttackRangeInFront: Int = 0 // 전방에 있는 적의 수
    private var enemiesPreparingAttackInRange: Int = 0 // 사정거리 내에서 공격 준비 중인 적의 수
    private var nearestEnemyDirection: Float = 0f // -1(왼쪽) ~ 1(오른쪽)

    // 주변 범위 (attackReach 기반으로 동적 계산)
    private val nearbyRange: Float
        get() = attackReach * NEARBY_RANGE_MULTIPLIER

    // Action weights
    private val validActionWeights: MutableMap<ActionType, Float> = mutableMapOf(
        ActionType.Valid.MOVE to 1f,
        ActionType.Valid.JUMP to 1f,
        ActionType.Valid.ATTACK to 1f,
        ActionType.Valid.MOVE_JUMP to 1f,
    )
    private val idleActionWeights: MutableMap<ActionType, Float> = mutableMapOf(
        ActionType.Idle.SPEECH to 1f,
    )

    /**
     * 특정 액션의 가중치를 설정
     */
    fun setActionWeight(actionType: ActionType, weight: Float) {
        require(weight >= 0f) { "Weight must be non-negative" }
        when (actionType) {
            is ActionType.Valid -> {
                validActionWeights[actionType] = weight
            }
            is ActionType.Idle -> {
                idleActionWeights[actionType] = weight
            }
        }
    }

    /**
     * 모든 액션의 가중치를 한번에 설정
     */
    fun setActionWeights(weights: Map<ActionType, Float>) {
        weights.forEach { (actionType, weight) ->
            setActionWeight(actionType, weight)
        }
    }

    /**
     * 가중치 기반 랜덤 액션 선택
     */
    private fun selectWeightedRandomAction(): ActionType {
        val actionWeights: Map<ActionType, Float>
        val entries: EnumEntries<*>
        // 1. valid / idle 결정
        if (Random.nextFloat() < ACTION_IDLE_PROBABILITY) { // 0 ... < 0.02
            actionWeights = idleActionWeights
            entries = ActionType.Idle.entries
        } else {
            actionWeights = validActionWeights
            entries = ActionType.Valid.entries
        }

        // 2. 결정된 type 내에서 행동 계산
        val totalWeight = actionWeights.values.sum()
        if (totalWeight <= 0f) return entries.random() // 비정상 이지만 계속 진행

        var random = Random.nextFloat() * totalWeight
        for ((actionType, weight) in actionWeights) {
            random -= weight
            if (random <= 0f) return actionType
        }
        return actionWeights.keys.first()
    }

    /**
     * 랜덤 확률을 기반으로 다음 액션을 결정
     */
    private fun randomAction() {
        when (selectWeightedRandomAction()) {
            ActionType.Valid.MOVE -> move()
            ActionType.Valid.JUMP -> jump()
            ActionType.Valid.ATTACK -> attack()
            ActionType.Valid.MOVE_JUMP -> moveJump()
            ActionType.Idle.SPEECH -> speech()
        }
    }

    /**
     * 적들의 위치 정보를 업데이트
     */
    private fun updateEnemyPositions() {
        val enemies = gameEngine.colosseumPlayers.filter { it != this && it.isAlive }

        if (enemies.isEmpty()) {
            nearestEnemyDistance = Float.MAX_VALUE
            nearestEnemyInFront = false
            enemiesInAttackRangeInFront = 0
            enemiesPreparingAttackInRange = 0
            nearestEnemyDirection = 0f
            return
        }

        // 가장 가까운 적 찾기
        var minDistance = Float.MAX_VALUE
        var nearestEnemy: ColosseumPlayer? = null

        enemies.forEach { enemy ->
            val dx = enemy.x - x
            val dy = enemy.y - y
            val distance = sqrt(dx * dx + dy * dy)

            if (distance < minDistance) {
                minDistance = distance
                nearestEnemy = enemy
            }
        }

        nearestEnemyDistance = minDistance
        nearestEnemy?.let { enemy ->
            nearestEnemyDirection = if (enemy.x > x) 1f else -1f
            // 적이 전방에 있는지 확인
            nearestEnemyInFront = (facingRight && enemy.x > x) || (!facingRight && enemy.x < x)
        }

        // 사정거리 내 적 수 계산
        val effectiveAttackRange = attackReach * ATTACK_RANGE_MULTIPLIER
        enemiesInAttackRangeInFront = 0
        enemiesPreparingAttackInRange = 0

        enemies.forEach { enemy ->
            val dx = enemy.x - x
            val dy = enemy.y - y
            val distance = sqrt(dx * dx + dy * dy)

            if (distance <= effectiveAttackRange) {
                // 전방에 있는 적만 카운트
                val isInFront = (facingRight && enemy.x > x) || (!facingRight && enemy.x < x)
                if (isInFront) {
                    enemiesInAttackRangeInFront++
                }
                // 공격 준비 중인 적 카운트
                if (enemy.isPreparingAttack) {
                    enemiesPreparingAttackInRange++
                }
            }
        }
    }

    /**
     * 적들의 위치를 고려하여 가중치를 동적으로 조정
     */
    private fun adjustWeightsBasedOnEnemies() {
        // 기본 가중치로 리셋
        validActionWeights[ActionType.Valid.MOVE] = 1f
        validActionWeights[ActionType.Valid.JUMP] = 1f
        validActionWeights[ActionType.Valid.ATTACK] = 1f
        validActionWeights[ActionType.Valid.MOVE_JUMP] = 1f

        // 0. 사정거리 내에 공격 준비 중인 적이 있으면 회피 우선
        if (enemiesPreparingAttackInRange > 0) {
            validActionWeights[ActionType.Valid.MOVE] = WEIGHT_EVADE
            validActionWeights[ActionType.Valid.JUMP] = WEIGHT_EVADE
            validActionWeights[ActionType.Valid.MOVE_JUMP] = WEIGHT_EVADE_MOVEJUMP // 회피 시 더 높은 가중치
            validActionWeights[ActionType.Valid.ATTACK] = WEIGHT_ATTACK_FAR
            return
        }

        // 1. 사정거리 내에 전방의 적이 있으면 공격 가중치 증가
        if (enemiesInAttackRangeInFront > 0) {
            validActionWeights[ActionType.Valid.ATTACK] =
                WEIGHT_ATTACK_IN_RANGE * enemiesInAttackRangeInFront
            return
        }

        // 2. 가장 가까운 적이 주변에 없거나 전방이 아니면 공격 가중치 감소
        if (nearestEnemyDistance > nearbyRange || !nearestEnemyInFront) {
            validActionWeights[ActionType.Valid.ATTACK] = WEIGHT_ATTACK_FAR
        } else {
            // 3. 가까운 적이 전방에 있으면 (사정거리 밖이지만 근처) 공격 가중치 증가
            validActionWeights[ActionType.Valid.ATTACK] = WEIGHT_ATTACK_NEARBY
        }
    }

    /**
     * 가까운 적의 방향을 고려하여 이동 방향 결정
     */
    private fun decideMovementDirection(): MoveDirection {
        // 가까운 적이 있으면 그쪽으로 이동
        return if (nearestEnemyDirection > 0) MoveDirection.RIGHT
               else MoveDirection.LEFT
    }

    override fun update(
        deltaTime: Double,
        world: World,
    ) {
        if (!isAlive) return
        super.update(deltaTime, world)

        val clampedDeltaTime = min(deltaTime, 0.03).toFloat()

        // 적들의 위치 정보 업데이트 (매 프레임)
        updateEnemyPositions()

        // 공격 타이머 처리
        when (attackState) {
            AttackState.PREPARING -> {
                attackTimer -= clampedDeltaTime
                if (attackTimer <= 0f) {
                    // 선딜 끝 -> 실제 공격 시작
                    attackState = AttackState.EXECUTING
                    attackTimer = ATTACK_EXECUTE_DURATION
                }
            }
            AttackState.EXECUTING -> {
                attackTimer -= clampedDeltaTime
                if (attackTimer <= 0f) {
                    attackState = AttackState.NONE
                }
            }
            AttackState.NONE -> {
                // 아무것도 안함
            }
        }

        // 대사 타이머 처리
        if (isSpeeching) {
            speechTimer -= clampedDeltaTime
            if (speechTimer <= 0f) {
                cachedTextSize = null
                if (speechIndex + 1 >= selectedSpeechList.size) {
                    isSpeeching = false
                    speechIndex = 0
                } else {
                    speechIndex++
                    justSpeeched = selectedSpeechList[speechIndex]
                    speechTimer = SPEECH_DURATION
                }
            }
        }

        // 수직 물리 작용
        val prevY = y
        velocityY += gravity * clampedDeltaTime
        y += velocityY * clampedDeltaTime

        // 플랫폼 landing
        onPlatform = false
        if (velocityY >= 0f || y >= prevY) {
            (world as? ColosseumWorld)?.getPlatforms()?.forEach { p ->
                // Treat as collision if the circle horizontally overlaps the platform span
                val overlapsX = right > p.left && left < p.right
                val wasAbove = prevY + halfHeight <= p.top
                val nowBelowTop = bottom >= p.top
                if (!onPlatform && overlapsX && wasAbove && nowBelowTop) {
                    y = p.top - halfHeight
                    velocityY = 0f
                    onPlatform = true
                }
            }
        }

        // 수평 이동 마찰계수 적용
        if (velocityX != 0f && onPlatform) {
            velocityX *= FRICTION
            if (velocityX in -1f..1f) velocityX = 0f
        }
        x += velocityX * clampedDeltaTime
        // 벽 충돌
        if (left < 0) {
            x = halfWidth
            velocityX = 0f
        } else if (right > viewportWidth) {
            x = viewportWidth - halfWidth
            velocityX = 0f
        }

        // 액션
        val wasInAction = inAction
        val onGround = onPlatform
        if (inAction) {
            val attackFinished = attackState == AttackState.NONE
            val moveFinished = velocityX == 0f
            val jumpFinished = onGround && velocityY == 0f
            val speechFinished = !isSpeeching
            if (attackFinished && moveFinished && jumpFinished && speechFinished) inAction = false
        }

        if (!inAction) {
            if (wasInAction) idleTime = Random.nextFloat() * IDLE_MAX_DURATION
            if (idleTime > 0f) {
                idleTime -= clampedDeltaTime
            } else {
                // 가중치 조정 후 액션 결정
                adjustWeightsBasedOnEnemies()
                randomAction()
            }
        }
    }

    override fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        super.render(context, textMeasurer, fontFamily)

        renderName(context, textMeasurer, fontFamily)

        if (isPreparingAttack) {
//            renderAttackPrepare(context)
            // TODO : temporary, refactor order between preparing and attacking
        }

        if (isAlive) {
            renderHP(context)
            // render speech
            if (isSpeeching) {
                renderSpeech(context, textMeasurer, fontFamily)
            }
        }
    }

    private fun setAction() {
        inAction = true
    }

    private fun move(
        direction: MoveDirection = decideMovementDirection(),
        power: Float = Random.nextFloat() * 800 + 200f, // 200f ~ 1000f
    ) {
        if (inAction) return
        setAction()

        when (direction) {
            MoveDirection.LEFT -> {
                velocityX = (velocityX - power).coerceAtLeast(-MAX_SPEED)
                this.direction = Entity.Direction.LEFT
            }
            MoveDirection.RIGHT -> {
                velocityX = (velocityX + power).coerceAtMost(MAX_SPEED)
                this.direction = Entity.Direction.RIGHT
            }
        }
    }

    private fun jump(
        power: Float = Random.nextFloat() * -500 - 500f, // -500f ~ -1000f
    ) {
        if (inAction) return
        // 점프는 바닥이나 플랫폼 위에서만 가능
        val canJump = (y >= floorY - 1f) || onPlatform
        if (!canJump) return
        setAction()

        velocityY = power
    }

    private fun moveJump(
        direction: MoveDirection = decideMovementDirection(),
        movePower: Float = Random.nextFloat() * 800 + 200f, // 200f ~ 1000f
        jumpPower: Float = Random.nextFloat() * -500 - 500f, // -500f ~ -1000f
    ) {
        if (inAction) return
        // 점프는 바닥이나 플랫폼 위에서만 가능
        val canJump = (y >= floorY - 1f) || onPlatform
        // 하지만 jump() 와 달리 조기종료 하지 않음
        setAction()

        // 이동
        when (direction) {
            MoveDirection.LEFT -> {
                velocityX = (velocityX - movePower).coerceAtLeast(-MAX_SPEED)
                this.direction = Entity.Direction.LEFT
            }
            MoveDirection.RIGHT -> {
                velocityX = (velocityX + movePower).coerceAtMost(MAX_SPEED)
                this.direction = Entity.Direction.RIGHT
            }
        }

        // 점프 (가능한 경우에만)
        if (canJump) {
            velocityY = jumpPower
        }
    }

    private fun attack() {
        if (inAction) return
        setAction()

        if (attackState == AttackState.NONE) {
            attackState = AttackState.PREPARING
            attackTimer = ATTACK_PREPARE_DURATION
            state = ActionState.ATTACK
        }
    }

    private fun speech() {
        if (inAction) return
        setAction()

        if (!isSpeeching) {
            isSpeeching = true
            selectedSpeechList = speechDocs.random()
            justSpeeched = selectedSpeechList[speechIndex]
            speechTimer = SPEECH_DURATION
        }
    }

    /**
     * render functions
     */
    private fun renderName(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        val textStyle = TextStyle(
            fontSize = (halfWidth * 0.25f).sp,
            color = Color.Black,
            fontFamily = fontFamily,
        )
        val measuredText = textMeasurer.measure(text = name, style = textStyle)
        context.drawText(
            textMeasurer = textMeasurer,
            text = name,
            topLeft = Offset(
                x - measuredText.size.width / 2f, // TODO : x, y 값이 화면 비율이 변함에도 그대로라 문제임
                y - measuredText.size.height / 2f - halfHeight * 1.5f
            ),
            style = textStyle,
        )
    }

    private fun renderSpeech(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        // text style
        val textStyle = TextStyle(fontSize = 14.sp, color = Color.Black, fontFamily = fontFamily)
        // text size
        val textSize = cachedTextSize
            ?: textMeasurer.measure(
                text = selectedSpeechList[speechIndex],
                style = textStyle,
            ).size.toSize().also { cachedTextSize = it }
        // speech offset
        val offsetDistance = halfHeight + 40f
        val centerY = y - offsetDistance
        val centerX = x

        context.drawText(
            textMeasurer = textMeasurer,
            text = selectedSpeechList[speechIndex],
            topLeft = Offset(
                x = centerX - textSize.width / 2,
                y = centerY - textSize.height / 2
            ),
            style = textStyle,
        )
    }

    private fun renderHP(context: GameDrawScope) {
        val max = startHp.toInt()
        val totalWidth = width * 2
        val totalX = x - width
        val totalY = y + halfHeight * 1.3f
        val dividerCount = max - 1
        val dividerWidth = if (dividerCount > 0) totalWidth / max / dividerCount / 2 else 0f
        val barWidth = (totalWidth - dividerWidth * dividerCount) / max
        val barHeight = height / 8

        // dividers
        val emptyPath = Path().apply {
            for (i in 0 until max) {
                val x = totalX + (barWidth + dividerWidth) * i
                addRect(Rect(x, totalY, x + barWidth, totalY + barHeight))
            }
        }
        context.drawPath(emptyPath, Color.LightGray)

        // hp
        if (hp > 0) {
            val filledPath = Path().apply {
                for (i in 0 until hp.toInt()) {
                    val x = totalX + (barWidth + dividerWidth) * i
                    addRect(Rect(x, totalY, x + barWidth, totalY + barHeight))
                }
            }
            context.drawPath(filledPath, Color.Green)
        }
    }

    fun pollJustSpeeched(): String {
        val j = justSpeeched
        justSpeeched = ""
        return j
    }

    // 디버깅/UI용 공개 메서드
    fun getNearestEnemyDistance(): Float = nearestEnemyDistance

    // damaged
    fun receiveDamage(attackerX: Float, power: Float = 600f): Boolean {
        // 데미지
        val damaged = combatComponent.takeDamage(1.0)
        if (!damaged) return false
        if (!isAlive) {
            state = ActionState.DIE
        }

        // 넉백
        val dir = if (attackerX < x) 1f else -1f
        velocityX = (velocityX + dir * power).coerceIn(-MAX_SPEED, MAX_SPEED)

        // 약간 점프
        velocityY = -200f
        onPlatform = false

        // 액션 취소
        attackState = AttackState.NONE
        attackTimer = 0f
        inAction = true

        return true
    }

    companion object Companion {
        private const val ACTION_IDLE_PROBABILITY = 0.02
        private const val ATTACK_PREPARE_DURATION = 1.0f   // 선딜
        private const val ATTACK_EXECUTE_DURATION = 0.3f   // 실제 공격
        private const val SPEECH_DURATION = 2.0f
        private const val IDLE_MAX_DURATION = 0.5f
        private const val MAX_SPEED = 2000f
        private const val FRICTION = 0.95f // 마찰력 계수
        private const val INVINCIBLE_DURATION = 0.5 // invincible time

        // 가중치 조정 파라미터
        private const val NEARBY_RANGE_MULTIPLIER = 4f // attackReach의 배수로 주변 범위 결정
        private const val ATTACK_RANGE_MULTIPLIER = 1.5f // attackReach의 배수로 공격 가능 범위 결정
        private const val WEIGHT_ATTACK_NEARBY = 1.5f // 적이 전방 근처에 있을 때 공격 가중치
        private const val WEIGHT_ATTACK_FAR = 0.5f // 적이 멀거나 후방에 있을 때 공격 가중치
        private const val WEIGHT_ATTACK_IN_RANGE = 2f // 사정거리 내 전방 공격 가중치
        private const val WEIGHT_EVADE = 1.5f // 적이 공격 준비 중일 때 회피(이동/점프) 가중치
        private const val WEIGHT_EVADE_MOVEJUMP = 2f // 적이 공격 준비 중일 때 회피(이동+점프) 가중치
    }
}

enum class MoveDirection {
    LEFT, RIGHT,
    ;
}

enum class AttackState {
    NONE,       // 공격 안함
    PREPARING,  // 선딜 (1초)
    EXECUTING   // 실제 공격 (0.3초)
}

/**
 * 플레이어가 수행할 수 있는 액션 타입
 * 새로운 액션을 추가할 때는 이 enum에 항목을 추가하고,
 * randomAction() 함수에 해당 케이스를 처리하는 로직을 추가하면 됨
 */
sealed interface ActionType {
    enum class Valid : ActionType {
        MOVE,
        JUMP,
        ATTACK,
        MOVE_JUMP, // 이동과 점프를 동시에 수행
    }

    enum class Idle : ActionType {
        SPEECH,
    }
}