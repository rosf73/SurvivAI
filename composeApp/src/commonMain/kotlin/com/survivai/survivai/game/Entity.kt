package com.survivai.survivai.game

import com.survivai.survivai.game.colosseum.GameDrawScope

interface Entity {
    fun update(deltaTime: Double, viewportWidth: Float, viewportHeight: Float)
    fun render(context: GameDrawScope)
    fun setViewportHeight(height: Float)
}