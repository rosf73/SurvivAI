package com.survivai.survivai.game.colosseum.entity

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.World
import com.survivai.survivai.game.colosseum.ColosseumInfo
import com.survivai.survivai.game.colosseum.GameDrawScope
import com.survivai.survivai.game.colosseum.world.ColosseumWorld
import kotlin.enums.EnumEntries
import kotlin.math.min
import kotlin.random.Random

data class Player(
    val name: String,
    val radius: Float = 30f,
    val color: Color = Color.Blue,
    private val startHp: Int = ColosseumInfo.defaultHp,
) : Entity {

    // Position (Center offset)
    var x = 0f
    var y = 0f

    var velocityX = 0f // 수평 속도

    var velocityY = 0f // 수직 속도
    private val gravity = 980f // 중력 가속도 (픽셀/초^2)

    // Viewport
    private var viewportWidth = 0f
    private var viewportHeight = 0f
    private val floorY: Float
        get() = viewportHeight - radius

    // Behavior
    private var facingRight = true // 바라보는 방향
    private var attackState = AttackState.NONE
    private var attackTimer = 0f
    val attackReach get() = radius * 2 + 5f
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
    private var hp = startHp
    val currentHp: Int get() = hp

    // 무적 시간
    private var isInvincible = false
    private var invincibleTimer = 0f

    // 생존 여부
    private var _isAlive = true
    val isAlive: Boolean get() = _isAlive

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
    var comboPoint = 0
    var maxComboPoint = 0

    // Action weights
    private val validActionWeights: MutableMap<ActionType, Float> = mutableMapOf(
        ActionType.Valid.MOVE to 1f,
        ActionType.Valid.JUMP to 1f,
        ActionType.Valid.ATTACK to 1f,
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
            ActionType.Valid.MOVE -> move(MoveDirection.entries.random())
            ActionType.Valid.JUMP -> jump()
            ActionType.Valid.ATTACK -> attack()
            ActionType.Idle.SPEECH -> speech()
        }
    }

    override fun update(
        deltaTime: Double,
        viewportWidth: Float,
        viewportHeight: Float,
        world: World,
    ) {
        this.viewportWidth = viewportWidth
        this.viewportHeight = viewportHeight

        val clampedDeltaTime = min(deltaTime, 0.03).toFloat()

        // 무적 타이머 처리
        if (isInvincible) {
            invincibleTimer -= clampedDeltaTime
            if (invincibleTimer <= 0f) {
                isInvincible = false
            }
        }

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
                val overlapsX = (x + radius) > p.left && (x - radius) < p.right
                val wasAbove = prevY + radius <= p.top
                val nowBelowTop = y + radius >= p.top
                if (!onPlatform && overlapsX && wasAbove && nowBelowTop) {
                    y = p.top - radius
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
        if (x - radius < 0) {
            x = radius
            velocityX = 0f
        } else if (x + radius > viewportWidth) {
            x = viewportWidth - radius
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
            if (wasInAction) idleTime = Random.nextFloat()
            if (idleTime > 0f) idleTime -= clampedDeltaTime else randomAction()
        }
    }

    override fun render(context: GameDrawScope, textMeasurer: TextMeasurer, fontFamily: FontFamily) {
        context.drawCircle(
            color = color,
            center = Offset(x, y),
            radius = radius
        )

        // 이름 표시 (앞 2글자)
        val displayName = name.take(2)
        val textStyle = TextStyle(fontSize = (radius * 0.5f).sp, color = Color.Black, fontFamily = fontFamily) // TODO : color
        val measuredText = textMeasurer.measure(text = displayName, style = textStyle)
        context.drawText(
            textMeasurer = textMeasurer,
            text = displayName,
            topLeft = Offset(
                x - measuredText.size.width / 2f,
                y - measuredText.size.height / 2f
            ),
            style = textStyle,
        )

        // Attack effect - 선딜 표시 (연한 색, 작은 크기) TODO : effect 개선 (칼 들었다 내려찍기)
        if (isPreparingAttack) {
            val progress = 1f - (attackTimer / ATTACK_PREPARE_DURATION)  // 0 -> 1
            val attackRadius = radius * (0.5f + progress * 0.3f)  // 점점 커짐
            val offsetDistance = radius + 5f
            val centerX = x + if (facingRight) offsetDistance else -offsetDistance
            val centerY = y

            // 바깥쪽 작은 원
            val outerRadius = attackRadius * 0.8f
            val outerRect = Rect(
                left = centerX - outerRadius,
                top = centerY - outerRadius,
                right = centerX + outerRadius,
                bottom = centerY + outerRadius,
            )

            // 안쪽 큰 원
            val innerRadius = attackRadius
            val innerOffsetX = if (facingRight) -attackRadius * 0.6f else attackRadius * 0.6f
            val innerRect = Rect(
                left = centerX + innerOffsetX - innerRadius,
                top = centerY - innerRadius,
                right = centerX + innerOffsetX + innerRadius,
                bottom = centerY + innerRadius,
            )

            val outerPath = Path().apply {
                if (facingRight) {
                    arcTo(outerRect, startAngleDegrees = -90f, sweepAngleDegrees = 180f, forceMoveTo = false)
                } else {
                    arcTo(outerRect, startAngleDegrees = 90f, sweepAngleDegrees = 180f, forceMoveTo = false)
                }
                close()
            }

            val innerPath = Path().apply {
                addOval(innerRect)
            }

            val crescentPath = Path().apply {
                op(outerPath, innerPath, PathOperation.Difference)
            }

            // 선딜은 반투명, 점점 진해짐
            val alpha = (100 + (progress * 120)).toInt()
            context.drawPath(
                path = crescentPath,
                color = Color(123, 30, 30, alpha),
            )
        }

        // Attack effect - 실제 공격 (진한 색, 원래 크기)
        if (isAttackingNow) {
            val attackRadius = radius
            val offsetDistance = radius + 5f
            val centerX = x + if (facingRight) offsetDistance else -offsetDistance
            val centerY = y

            // 바깥쪽 작은 원
            val outerRadius = attackRadius * 0.8f
            val outerRect = Rect(
                left = centerX - outerRadius,
                top = centerY - outerRadius,
                right = centerX + outerRadius,
                bottom = centerY + outerRadius,
            )

            // 안쪽 큰 원
            val innerRadius = attackRadius
            val innerOffsetX = if (facingRight) -attackRadius * 0.6f else attackRadius * 0.6f
            val innerRect = Rect(
                left = centerX + innerOffsetX - innerRadius,
                top = centerY - innerRadius,
                right = centerX + innerOffsetX + innerRadius,
                bottom = centerY + innerRadius,
            )

            val outerPath = Path().apply {
                if (facingRight) {
                    arcTo(outerRect, startAngleDegrees = -90f, sweepAngleDegrees = 180f, forceMoveTo = false)
                } else {
                    arcTo(outerRect, startAngleDegrees = 90f, sweepAngleDegrees = 180f, forceMoveTo = false)
                }
                close()
            }

            val innerPath = Path().apply {
                addOval(innerRect)
            }

            val crescentPath = Path().apply {
                op(outerPath, innerPath, PathOperation.Difference)
            }

            context.drawPath(
                path = crescentPath,
                color = Color(123, 30, 30, 220),
            )
        }

        // Speech effect
        if (isSpeeching) {
            // text style
            val textStyle = TextStyle(fontSize = 14.sp, color = Color.Black, fontFamily = fontFamily)
            // text size
            val textSize = cachedTextSize
                ?: textMeasurer.measure(
                    text = selectedSpeechList[speechIndex],
                    style = textStyle,
                ).size.toSize().also { cachedTextSize = it }
            // speech offset
            val offsetDistance = radius + 20f // TODO : magic number
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
    }

    override fun setViewportHeight(height: Float) {
        viewportHeight = height
    }

    private fun setAction() {
        inAction = true
    }

    private fun move(
        direction: MoveDirection,
        power: Float = Random.nextFloat() * 1500 + 500f, // 500f ~ 2000f
    ) {
        if (inAction) return
        setAction()

        when (direction) {
            MoveDirection.LEFT -> {
                velocityX = (velocityX - power).coerceAtLeast(-MAX_SPEED)
                facingRight = false
            }
            MoveDirection.RIGHT -> {
                velocityX = (velocityX + power).coerceAtMost(MAX_SPEED)
                facingRight = true
            }
        }
    }

    private fun jump() {
        if (inAction) return
        // 점프는 바닥이나 플랫폼 위에서만 가능
        val canJump = (y >= floorY - 1f) || onPlatform
        if (!canJump) return
        setAction()

        velocityY = Random.nextFloat() * -500 - 500f // -500f ~ -1000f
    }

    private fun attack() {
        if (inAction) return
        setAction()

        if (attackState == AttackState.NONE) {
            attackState = AttackState.PREPARING
            attackTimer = ATTACK_PREPARE_DURATION
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

    fun pollJustSpeeched(): String {
        val j = justSpeeched
        justSpeeched = ""
        return j
    }

    // damaged
    fun receiveDamage(attackerX: Float, power: Float = 600f): Boolean {
        // 무적 상태인 경우 return
        if (isInvincible) return false

        // 넉백
        val dir = if (attackerX < x) 1f else -1f
        velocityX = (velocityX + dir * power).coerceIn(-MAX_SPEED, MAX_SPEED)

        // 약간 점프
        velocityY = -200f // TODO : magic number
        onPlatform = false

        // 데미지
        hp = (hp - 1).coerceAtLeast(0)

        // 생존 체크
        if (hp <= 0) {
            _isAlive = false
        }

        // 무적 on
        isInvincible = true
        invincibleTimer = INVINCIBLE_DURATION

        // 액션 취소
        attackState = AttackState.NONE
        attackTimer = 0f
        inAction = true

        // 스탯 업데이트
        ColosseumInfo.resetPlayerComboPoint(name)

        return true
    }

    companion object {
        private const val ACTION_IDLE_PROBABILITY = 0.02
        private const val ATTACK_PREPARE_DURATION = 1.0f   // 선딜
        private const val ATTACK_EXECUTE_DURATION = 0.3f   // 실제 공격
        private const val SPEECH_DURATION = 2.0f
        private const val MAX_SPEED = 2000f
        private const val FRICTION = 0.95f // 마찰력 계수
        private const val INVINCIBLE_DURATION = 0.4f // 무적 시간
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
    }

    enum class Idle : ActionType {
        SPEECH,
    }
}