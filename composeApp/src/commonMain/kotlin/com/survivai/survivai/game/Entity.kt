package com.survivai.survivai.game

interface Entity {
    fun update(deltaTime: Double, viewportWidth: Float, viewportHeight: Float)
    fun render(context: GameDrawScope)
    fun setViewportHeight(height: Float)
}