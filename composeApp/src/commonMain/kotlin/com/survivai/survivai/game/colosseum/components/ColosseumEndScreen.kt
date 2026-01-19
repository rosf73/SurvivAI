package com.survivai.survivai.game.colosseum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivai.survivai.game.colosseum.state.MVPTitleCard
import com.survivai.survivai.game.colosseum.state.StatCell

@Composable
fun ColosseumEndScreen(
    statsList: List<List<StatCell>>,
    titles: List<MVPTitleCard>,
    onClickRestart: () -> Unit,
    onClickReset: () -> Unit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(color = Color(255, 220, 220, 180))
            .padding(horizontal = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 타이틀
        Text(
            modifier = Modifier
                .padding(top = 16.dp),
            text = "게임 결과",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
            ),
        )

        Spacer(modifier = Modifier.size(20.dp))

        // 대시보드(70%) + 칭호 목록(30%) Row
        Row(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            // 대시 보드
            Dashboard(
                statsList = statsList,
                fontFamily = fontFamily,
                modifier = Modifier
                    .weight(7f)
                    .fillMaxHeight(),
            )

            Spacer(modifier = Modifier.size(20.dp))

            // 칭호 목록
            TitlesList(
                titles = titles,
                fontFamily = fontFamily,
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
            )
        }

        Spacer(modifier = Modifier.size(20.dp))

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

            Spacer(modifier = Modifier.size(30.dp))

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
private fun TitlesList(
    titles: List<MVPTitleCard>,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start,
    ) {
        // 제목
        Text(
            text = "💎 MVP 전당 💎",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = fontFamily,
                color = Color.Black,
            ),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // 칭호 리스트
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(titles) { playerTitle ->
                // 칭호 제목
                Text(
                    text = playerTitle.title,
                    style = TextStyle(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                // 칭호 설명
                Text(
                    text = playerTitle.desc,
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        color = Color.Gray,
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // 플레이어들
                LazyRow(
                    modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                ) {
                    items(playerTitle.players) { p ->
                        Text(
                            text = p.stat,
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontFamily = fontFamily,
                            ),
                            color = p.color ?: Color.Unspecified,
                            modifier = Modifier.padding(end = 2.dp),
                        )
                    }
                }

                // 칭호 간 간격
                Spacer(modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun Dashboard(
    statsList: List<List<StatCell>>,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    val borderColor = Color(0xFF666666)
    val borderWidth = 1.dp

    LazyColumn(
        modifier = modifier
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
                            text = cellText.stat,
                            style = TextStyle(
                                fontFamily = fontFamily,
                                textAlign = TextAlign.Center
                            ),
                            color = cellText.color ?: Color.Unspecified,
                        )
                    }
                }
            }
        }
    }
}