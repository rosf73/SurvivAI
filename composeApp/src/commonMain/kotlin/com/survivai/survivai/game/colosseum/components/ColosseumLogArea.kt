package com.survivai.survivai.game.colosseum.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivai.survivai.game.colosseum.state.ColosseumInfo
import com.survivai.survivai.game.colosseum.state.Log

@Composable
fun ColosseumLogArea(
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "LOG",
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = fontFamily,
        )

        LazyColumn(
            reverseLayout = true,
            modifier = Modifier.fillMaxSize(),
        ) {
            val itemUpdater = ColosseumInfo.itemUpdateState.value
            items(ColosseumInfo.logEntries) { log ->
                LogLine(
                    log = log,
                    fontFamily = fontFamily,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun LogLine(
    log: Log,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (log) {
            is Log.System -> {
                Spacer(modifier = Modifier.size(30.dp, 1.dp))
                Text(
                    text = log.msg,
                    fontSize = 12.sp,
                    fontFamily = fontFamily,
                )
            }
            is Log.Solo -> {
                PlayerLabel(
                    color = log.player.color,
                    name = log.player.name,
                    fontFamily = fontFamily,
                )
                Text(
                    text = ": \"${log.msg}\"",
                    fontSize = 12.sp,
                    fontFamily = fontFamily,
                )
            }
            is Log.Duo -> {
                Spacer(modifier = Modifier.size(30.dp, 1.dp))
                PlayerLabel(
                    color = log.perpetrator.color,
                    name = log.perpetrator.name,
                    fontFamily = fontFamily,
                )
                Text(
                    text = " ${log.interaction} ",
                    fontSize = 12.sp,
                    fontFamily = fontFamily,
                )
                PlayerLabel(
                    color = log.victim.color,
                    name = log.victim.name,
                    fontFamily = fontFamily,
                )
                Text(
                    text = " ${log.additional}",
                    fontSize = 12.sp,
                    fontFamily = fontFamily,
                )
            }
        }
    }
}

@Composable
private fun PlayerLabel(
    color: Color,
    name: String,
    fontFamily: FontFamily,
) {
//    Box(
//        modifier = Modifier
//            .size(12.dp)
//            .background(color = color, shape = androidx.compose.foundation.shape.CircleShape)
//            .border(1.dp, Color.White.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape)
//    )
    Text(
        text = " $name",
        fontSize = 12.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        color = color,
    )
}