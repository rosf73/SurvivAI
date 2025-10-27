package com.survivai.survivai.game

import com.survivai.survivai.game.colosseum.GameDrawScope

interface World {
    fun buildMap(width: Float, height: Float)
    fun render(context: GameDrawScope)
}