package com.survivai.survivai.game.colosseum.logic

import androidx.compose.ui.graphics.Color

sealed interface ColosseumState {
    data object WaitingForPlayers : ColosseumState  // 플레이어 등록 대기
    data class Playing(val startTime: Long) : ColosseumState  // 게임 진행 중
    data class Ended(val statsList: List<List<StatCell>>, val titleList: List<MVPTitleCard>) : ColosseumState  // 게임 종료
}

data class MVPTitleCard(
    val title: String,
    val desc: String,
    val players: List<StatCell>,
)

data class StatCell(
    val stat: String,
    val color: Color? = null,
)