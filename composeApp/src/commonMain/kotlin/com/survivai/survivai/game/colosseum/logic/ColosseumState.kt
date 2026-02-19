package com.survivai.survivai.game.colosseum.logic

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

sealed interface ColosseumState {
    data object WaitingForPlayers : ColosseumState
    data class Playing(val startTime: Long) : ColosseumState
    data class Ended(val statsList: List<List<StatCell>>, val titleList: List<MVPTitleCard>) : ColosseumState
}

enum class DisasterOption(val label: String, val clickable: Boolean = false) {
    FALLING_ROCKS("Falling rocks", clickable = true),
    CAR_HIT_AND_RUN("Car hit-and-run"),
    ;
}

data class MVPTitleCard(
    val title: String,
    val desc: String,
    val players: List<StatCell>,
)

data class StatCell(
    val stat: String,
    val color: Color = Color.Unspecified,
    val weight: FontWeight = FontWeight.Normal,
) {
    companion object {
        // factory
        fun rowTitle(stat: String) = StatCell(stat, Color.Black, FontWeight.Bold)
        fun colLabel(stat: String, color: Color) = StatCell(stat, color, FontWeight.Bold)
    }
}