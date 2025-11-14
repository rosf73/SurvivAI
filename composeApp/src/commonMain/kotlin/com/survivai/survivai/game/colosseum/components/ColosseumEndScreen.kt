package com.survivai.survivai.game.colosseum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun ColosseumEndScreen(
    statsList: List<List<String>>,
    onClickRestart: () -> Unit,
    onClickReset: () -> Unit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(color = Color(220, 255, 255, 180))
            .padding(horizontal = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 타이틀
        Text(
            modifier = Modifier
                .padding(top = 16.dp),
            text = "플레이어 등록",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
            ),
        )

        Spacer(modifier = Modifier.size(20.dp))

        // 대시 보드
        ColosseumDashboard(
            statsList = statsList,
            fontFamily = fontFamily,
            modifier = Modifier
                .weight(1f),
        )

        // 재시작 버튼
        Row(
            modifier = Modifier,
        ) {
            Button(
                modifier = Modifier
                    .padding(bottom = 16.dp),
                colors = ButtonColors(
                    containerColor = Color.Cyan,
                    contentColor = Color.Blue,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.Gray,
                ),
                onClick = {
                    onClickRestart()
                }
            ) {
                Text("바로 재시작", style = TextStyle(fontFamily = fontFamily))
            }

            Button(
                modifier = Modifier
                    .padding(bottom = 16.dp),
                colors = ButtonColors(
                    containerColor = Color.Cyan,
                    contentColor = Color.Blue,
                    disabledContainerColor = Color.LightGray,
                    disabledContentColor = Color.Gray,
                ),
                onClick = {
                    onClickReset()
                }
            ) {
                Text("경기 재설정", style = TextStyle(fontFamily = fontFamily))
            }
        }
    }
}

@Composable
private fun ColosseumDashboard(
    statsList: List<List<String>>,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    val borderColor = Color(0xFF666666)
    val borderWidth = 1.dp
    
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .border(width = borderWidth, color = borderColor)
    ) {
        itemsIndexed(statsList) { rowIndex, rowData ->
            val backgroundColor = when (rowIndex) {
                0 -> Color(0xFF4FC3F7) // 푸른색
                1 -> Color(0xFFFFF9C4) // 연노란색
                else -> Color(0x80FFFFFF) // 반투명 하얀색 (alpha 50%)
            }
            
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                rowData.forEachIndexed { colIndex, cellText ->
                    val cellModifier = if (colIndex == 0) {
                        // 첫 번째 column: max width 120.dp
                        Modifier.width(120.dp)
                    } else {
                        // 나머지 column들: 같은 간격
                        Modifier.weight(1f)
                    }
                    
                    Box(
                        modifier = cellModifier
                            .background(backgroundColor)
                            .border(
                                width = borderWidth,
                                color = borderColor
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cellText,
                            style = TextStyle(
                                fontFamily = fontFamily,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}