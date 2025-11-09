package com.survivai.survivai.game.colosseum.entity

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

class Player(
    val name: String,
    val radius: Float = 30f,
    val color: Color = Color.Blue,
) : Entity {

    // Position
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
    private var isAttacking = false
    private var attackTimer = 0f
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
    private var hp = START_HP
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
    val isAttackingNow: Boolean get() = isAttacking
    val isFacingRight: Boolean get() = facingRight

    /**
     * 랜덤 확률을 기반으로 다음 액션을 결정
     */
    private fun randomAction() {
        // 95 : 5 비율
        if (Random.nextFloat() < 0.98) {
            // 98% 확률로 BEHAVIOR
            when (Random.nextInt(3)) {
                0 -> move(MoveDirection.entries.random())
                1 -> jump()
                2 -> attack()
            }
        } else {
            // 2% 확률로 IDLE
            speech()
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
        if (isAttacking) {
            attackTimer -= clampedDeltaTime
            if (attackTimer <= 0f) {
                isAttacking = false
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
            val attackFinished = !isAttacking
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

        // Attack effect
        if (isAttacking) {
            // arc size
            val attackRadius = radius
            // arc offset
            val offsetDistance = radius + 5f
            val top = y
            val left = x + if (facingRight) offsetDistance else -offsetDistance
            // arc angle
            val startAngle = if (facingRight) -90f else 90f
            val sweepAngle = 180f

            context.drawArc(
                color = Color.Black,
                topLeft = Offset(left - attackRadius, top - attackRadius),
                width = attackRadius * 2f,
                height = attackRadius * 2f,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
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

    fun move(
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

    fun jump() {
        if (inAction) return
        // 점프는 바닥이나 플랫폼 위에서만 가능
        val canJump = (y >= floorY - 1f) || onPlatform
        if (!canJump) return
        setAction()

        velocityY = Random.nextFloat() * -500 - 500f // -500f ~ -1000f
    }

    fun attack() {
        if (inAction) return
        setAction()

        if (!isAttacking) {
            isAttacking = true
            attackTimer = ATTACK_DURATION
        }
    }

    fun speech() {
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
        isAttacking = false
        attackTimer = 0f
        inAction = true

        return true
    }

    companion object {
        private const val ATTACK_DURATION = 0.3f
        private const val SPEECH_DURATION = 2.0f
        private const val MAX_SPEED = 2000f
        private const val FRICTION = 0.95f // 마찰력 계수
        private const val START_HP = 3 // TODO : 시작 체력 지정 기능 추가
        private const val INVINCIBLE_DURATION = 0.4f // 무적 시간
    }
}

enum class MoveDirection {
    LEFT, RIGHT,
    ;
}