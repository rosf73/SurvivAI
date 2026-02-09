package com.survivai.survivai.game.colosseum.entity

import androidx.compose.ui.graphics.Color
import com.survivai.survivai.game.colosseum.world.ColosseumWorld
import kotlin.math.abs
import kotlin.random.Random

/**
 * 랜덤 speech 리스트
 */
val speechDocs = listOf(
    listOf("나는 최강이다."),
    listOf("빌이 청소할 차례다를 영어로 하면?", "빌 클린턴 ㅋㅋ"),
    listOf("이번엔 조지가 씻을 차례다를 영어로 하면?", "조지 워싱턴 ㅋㅋ"),
    listOf("아 금붕어 산책 시켜야 하는데."),
    listOf("일본인이 대가족을 만들고 싶을 때 하는 말은?", "여러식구 오네가이시마스 ㅋㅋ"),
    listOf("경상도 사람이 홍차를 냉동실에 넣으며 하는 말은?", "얼그레이~ ㅋㅋ"),
    listOf("피자와 함께 먹으면 안되는 것은?", "구기자 ㅋㅋ"),
    listOf("화해합시다."),
)

/**
 * 사용 가능한 플레이어 이름 리스트
 */
private val playerNames = listOf(
    "홍길동", "김철수", "이영희", "박지성", "손흥민", "김연아", "봉준호", "페이커"
)

/**
 * 사용 가능한 플레이어 색상 팔레트
 */
private val playerColorPalette = listOf(
    Color(0xFFE77F3C), // Orange
    Color(0xFF3498DB), // Blue
    Color(0xFF2ECC71), // Green
    Color(0xFFF3CC12), // Yellow
    Color(0xFF9B59B6), // Purple
    Color(0xFF666666), // Dark Gray
    Color(0xFFE91E63), // Pink
    Color(0xFFBBBBBB), // Gray
)

data class PlayerInitPair(
    val name: String,
    val color: Color,
)
private var playerInitPairs: List<PlayerInitPair> = listOf()

/**
 * 랜덤 색상 생성 (선명한 색상 위주)
 */
fun generateRandomColor(): Color {
    return playerColorPalette[Random.nextInt(playerColorPalette.size)]
}

/**
 * 중복 없이 여러 개의 랜덤 이름과 색상 생성
 * @param count 필요한 플레이어 개수 (최대 8개)
 * @return 중복 없는 이름-색상 맵
 */
fun generateUniquePlayerPool(count: Int): List<PlayerInitPair> {
    // init on first time only
    if (playerInitPairs.isEmpty()) {
        val size = minOf(playerNames.size, playerColorPalette.size)
        val shuffledNames = playerNames.shuffled()
        val shuffledColors = playerColorPalette.shuffled()

        playerInitPairs = (0 until size).map { i ->
            PlayerInitPair(name = shuffledNames[i], color = shuffledColors[i])
        }
    }

    return playerInitPairs.take(count)
}

/**
 * 랜덤 포지션 생성
 */
fun List<ColosseumPlayer>.initializePositions(world: ColosseumWorld) {
    val margin = 10f
    val placed = mutableListOf<Pair<Float, Float>>()
    forEach { p ->
        val halfWidth = p.halfWidth
        val halfHeight = p.halfHeight

        val minX = halfWidth + margin
        val maxX = (world.viewportWidth - halfWidth - margin).coerceAtLeast(minX)
        val floorTop = world.getFloor() ?: world.viewportHeight
        val y = (floorTop - halfHeight).coerceAtLeast(halfHeight)

        var tries = 0
        var x: Float
        do {
            x = if (maxX > minX) Random.nextFloat() * (maxX - minX) + minX else minX
            tries++
        } while (
            placed.any { (ox, _) -> abs(ox - x) < (halfWidth * 2 + margin) }
            && tries < 50
        )

        p.x = x
        p.y = y
        placed.add(x to y)
    }
}

/**
 * 타격 성공 여부
 */
fun List<ColosseumPlayer>.detectAttackDamagedThisFrame(
    onDetected: (ColosseumPlayer, ColosseumPlayer) -> Unit,
) {
    val hitThisFrame = mutableSetOf<Pair<Int, Int>>()

    for (i in indices) {
        val attacker = get(i)
        if (!attacker.isAttackingNow) continue
        val reach = attacker.attackReach
        val heightTol = attacker.height * 0.6f
        for (j in indices) {
            if (i == j) continue
            val target = get(j)
            val dx = target.x - attacker.x
            val dy = target.y - attacker.y
            val inFront = if (attacker.isFacingRight) dx > 0f else dx < 0f
            if (inFront && abs(dx) <= reach && abs(dy) <= heightTol) {
                val key = i to j
                if (hitThisFrame.add(key)) {
                    val damaged = target.receiveDamage(attacker.x, power = 700f)
                    if (damaged) {
                        onDetected(attacker, target)
                    }
                }
            }
        }
    }
}