package com.survivai.survivai.game

interface World {
    fun buildMap(width: Float, height: Float)
    fun render(context: GameDrawScope)
}