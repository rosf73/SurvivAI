package com.survivai.survivai.game

interface Engine {
    val world: World
    var players: List<Entity>

    fun update(deltaTime: Double)
}