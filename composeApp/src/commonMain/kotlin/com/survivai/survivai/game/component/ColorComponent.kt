package com.survivai.survivai.game.component

import androidx.compose.ui.graphics.Color
import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.World

class ColorComponent : Component() {
    var tintColor: Color? = null

    override fun update(deltaTime: Double, owner: Entity, world: World) {
        // TODO : update tintColor according to changed status
    }
}