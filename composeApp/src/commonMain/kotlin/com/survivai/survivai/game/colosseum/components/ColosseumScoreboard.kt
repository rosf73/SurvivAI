package com.survivai.survivai.game.colosseum.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivai.survivai.game.colosseum.logic.StatCell

@Composable
fun ColosseumScoreboard(
    statsList: List<List<StatCell>>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .border(
                BorderStroke(3.dp, Brush.verticalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00838F)))),
            ),
        colors = CardDefaults.cardColors(containerColor = Color(0x00FFFFFF)),
    ) {
        LazyColumn {
            itemsIndexed(statsList) { rowIndex, rowData ->
                val backgroundColor = when (rowIndex) {
                    0 -> Color(0xFF4FC3F7) // title color
                    else -> Color(0x00FFFFFF)
                }

                Row(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowData.forEachIndexed { colIndex, cellText ->
                        val cellModifier = if (colIndex == 0) {
                            Modifier.width(120.dp)
                        } else {
                            // same width
                            Modifier.weight(1f)
                        }

                        Box(
                            modifier = cellModifier
                                .background(backgroundColor)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFFCCCCCC),
                                )
                                .padding(8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = cellText.stat,
                                style = LocalTextStyle.current.copy(
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    fontWeight = cellText.weight,
                                ),
                                color = cellText.color,
                            )
                        }
                    }
                }
            }
        }
    }
}