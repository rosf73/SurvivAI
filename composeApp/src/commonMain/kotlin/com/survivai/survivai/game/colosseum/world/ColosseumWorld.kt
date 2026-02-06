package com.survivai.survivai.game.colosseum.world

import com.survivai.survivai.game.World
import com.survivai.survivai.game.GameDrawScope
import com.survivai.survivai.game.colosseum.PlatformRect

class ColosseumWorld : World {

    private val platforms = mutableListOf<PlatformRect>()

    private var viewportWidth = 0f
    private var viewportHeight = 0f

    override fun buildMap(width: Float, height: Float) {
        viewportWidth = width
        viewportHeight = height

        platforms.clear()
        if (viewportWidth <= 0f || viewportHeight <= 0f) return
        val floorH = 30f
        // floor
        platforms.add(PlatformRect(0f, viewportHeight - floorH, viewportWidth, floorH))
        // middle
        platforms.add(PlatformRect(viewportWidth * 0.45f, viewportHeight * 0.75f, viewportWidth * 0.1f))
        // left upper-mid
        platforms.add(PlatformRect(viewportWidth * 0.15f, viewportHeight * 0.75f, viewportWidth * 0.1f))
        // right top
        platforms.add(PlatformRect(viewportWidth * 0.75f, viewportHeight * 0.75f, viewportWidth * 0.1f))
    }

    override fun render(context: GameDrawScope) {
        // 맵 (플랫폼 렌더링)
        platforms.forEach { it.render(context) }
    }

    fun getFloor(): Float? {
        return platforms.firstOrNull()?.top
    }

    // TODO : checkCollision(left, top, right, bottom, onCollided) 함수로 변경
    fun getPlatforms() = platforms
}