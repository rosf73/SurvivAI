package com.survivai.survivai.game.sprite

class SpriteLoader {
    private val cache = mutableMapOf<String, SpriteAnimation>()

    suspend fun load(
        fileName: String,
        data: SpriteAnimationData,
    ): SpriteAnimation {
        return cache[fileName] ?: SpriteAnimation.load(fileName, data).also {
            cache[fileName] = it
        }
    }
}