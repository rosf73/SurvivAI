package com.survivai.survivai.game.colosseum.logic

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

sealed interface ColosseumState {
    data object WaitingForPlayers : ColosseumState  // 플레이어 등록 대기
    data class Playing(val startTime: Long) : ColosseumState  // 게임 진행 중
    data class Ended(val statsList: List<List<StatCell>>, val titleList: List<MVPTitleCard>) : ColosseumState  // 게임 종료
}

enum class DisasterOption {
    FALLING_ROCKS, CAR_HIT_AND_RUN,
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