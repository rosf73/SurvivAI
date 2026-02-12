package com.survivai.survivai.game.colosseum.components

import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivai.survivai.game.colosseum.logic.ColosseumEngine
import com.survivai.survivai.game.colosseum.logic.Log

@Composable
fun ColosseumLogArea(
    gameEngine: ColosseumEngine, // DI
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .graphicsLayer {
                // set layer onto offscreen buffer
                compositingStrategy = CompositingStrategy.Offscreen
            }
            .drawWithContent {
                // 1. draw composable content
                drawContent()

                // 2. cover gradient
                val fadeBrush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color.Transparent,     // Top 0%: completely transparent
                        0.4f to Color.Black,           // Top 40%: gradient transparent
                        1.0f to Color.Black            // 100%: opaque
                    ),
                    startY = 0f,
                    endY = size.height // whole height
                )

                drawRect(
                    brush = fadeBrush,
                    blendMode = BlendMode.DstIn,
                )
            }
    ) {
        LazyColumn(
            reverseLayout = true,
            modifier = Modifier.fillMaxSize(),
        ) {
            val itemUpdater = gameEngine.logUpdateState.value
            items(gameEngine.logEntries) { log ->
                LogLine(
                    log = log,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                )
            }
        }
    }
}

@Composable
private fun LogLine(
    log: Log,
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
                    color = Color.Gray,
                )
            }
            is Log.Solo -> {
                PlayerLabel(
                    color = log.player.signatureColor,
                    name = log.player.name,
                )
                Text(
                    text = ": \"${log.msg}\"",
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
            is Log.Duo -> {
                Spacer(modifier = Modifier.size(30.dp, 1.dp))
                PlayerLabel(
                    color = log.perpetrator.signatureColor,
                    name = log.perpetrator.name,
                )
                Text(
                    text = " ${log.interaction} ",
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
                PlayerLabel(
                    color = log.victim.signatureColor,
                    name = log.victim.name,
                )
                Text(
                    text = " ${log.additional}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
            is Log.Accidental -> {
                PlayerLabel(
                    color = log.victim.signatureColor,
                    name = log.victim.name,
                )
                Text(
                    text = log.msg,
                    fontSize = 12.sp,
                    color = Color.Gray,
                )
            }
        }
    }
}

@Composable
private fun PlayerLabel(
    color: Color,
    name: String,
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
        fontWeight = FontWeight.Bold,
        color = color,
    )
}