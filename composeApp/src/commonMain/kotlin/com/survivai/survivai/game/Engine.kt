package com.survivai.survivai.game

interface Engine {
    val world: World
    // TODO : to MutableList
    var entities: List<Entity>

    fun update(deltaTime: Double)
}