package com.survivai.survivai.game.component

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.font.FontFamily
import com.survivai.survivai.game.Entity
import com.survivai.survivai.game.World
import com.survivai.survivai.game.colosseum.GameDrawScope

abstract class Component {
    open fun update(deltaTime: Double, owner: Entity, world: World) {}
    open fun render(context: GameDrawScope, owner: Entity, textMeasurer: TextMeasurer, fontFamily: FontFamily) {}
}