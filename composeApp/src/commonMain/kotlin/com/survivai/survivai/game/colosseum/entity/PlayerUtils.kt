package com.survivai.survivai.game.colosseum.entity

import androidx.compose.ui.graphics.Color
import com.survivai.survivai.game.colosseum.ColosseumInfo
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
 * 랜덤 색상 생성 (선명한 색상 위주)
 */
fun generateRandomColor(): Color {
    val colors = listOf(
        Color(0xFFE74C3C), // Red
        Color(0xFF3498DB), // Blue
        Color(0xFF2ECC71), // Green
        Color(0xFFF39C12), // Orange
        Color(0xFF9B59B6), // Purple
        Color(0xFF1ABC9C), // Turquoise
        Color(0xFFE91E63), // Pink
        Color(0xFFFF5722), // Deep Orange
        Color(0xFF00BCD4), // Cyan
        Color(0xFF4CAF50), // Light Green
        Color(0xFFFF9800), // Orange
        Color(0xFF673AB7), // Deep Purple
    )
    return colors[Random.nextInt(colors.size)]
}

/**
 * 랜덤 포지션 생성
 */
fun List<Player>.initializePositions(viewportWidth: Float, viewportHeight: Float) {
    val margin = 10f
    val placed = mutableListOf<Pair<Float, Float>>()
    forEach { p ->
        val radius = p.radius
        val minX = radius + margin
        val maxX = (viewportWidth - radius - margin).coerceAtLeast(minX)
        val floorTop = ColosseumInfo.world.getFloor() ?: viewportHeight
        val y = (floorTop - radius).coerceAtLeast(radius)

        var tries = 0
        var x: Float
        do {
            x = if (maxX > minX) Random.nextFloat() * (maxX - minX) + minX else minX
            tries++
        } while (
            placed.any { (ox, _) -> abs(ox - x) < (p.radius * 2 + margin) }
            && tries < 50
        )

        p.x = x
        p.y = y
        placed.add(x to y)
    }
}