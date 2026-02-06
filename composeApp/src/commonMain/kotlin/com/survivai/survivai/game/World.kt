package com.survivai.survivai.game

interface World {
    var viewportWidth: Float
    var viewportHeight: Float
    fun buildMap(width: Float, height: Float)
    fun render(context: GameDrawScope)
}