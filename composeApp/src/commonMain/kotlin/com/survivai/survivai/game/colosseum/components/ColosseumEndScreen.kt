package com.survivai.survivai.game.colosseum.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivai.survivai.common.squareVerticalScrollbar
import com.survivai.survivai.game.colosseum.logic.MVPTitleCard
import com.survivai.survivai.game.colosseum.logic.StatCell

@Composable
fun ColosseumEndScreen(
    statsList: List<List<StatCell>>,
    titles: List<MVPTitleCard>,
    isLandscape: Boolean,
    onClickRestart: () -> Unit,
    onClickReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color(0xFF0A0A0A))
            .drawBehind {
                // fine grid pattern
                val gridSize = 20.dp.toPx()
                for (x in 0..size.width.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(x.toFloat(), 0f),
                        end = Offset(x.toFloat(), size.height),
                        strokeWidth = 1f
                    )
                }
                for (y in 0..size.height.toInt() step gridSize.toInt()) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.05f),
                        start = Offset(0f, y.toFloat()),
                        end = Offset(size.width, y.toFloat()),
                        strokeWidth = 1f
                    )
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = Color(0xFF1A1A1A))
                .border(
                    BorderStroke(
                        2.dp,
                        Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B)))
                    )
                )
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "GAME RESULT",
                style = LocalTextStyle.current.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700),
                    shadow = Shadow(
                        color = Color(0xFFD32F2F),
                        offset = Offset(4f, 4f),
                        blurRadius = 2f
                    ),
                    letterSpacing = 4.sp
                )
            )
            Text(
                text = "SCORE DASHBOARD",
                style = LocalTextStyle.current.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 2.sp
                )
            )
        }

        Spacer(modifier = Modifier.size(20.dp))

        // Middle
        ResultArea(
            statsList = statsList,
            titles = titles,
            isLandscape = isLandscape,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.size(20.dp))

        // Footer
        Row(
            modifier = Modifier
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)))
                .padding(16.dp)
                .padding(bottom = 8.dp),
        ) {
            Button(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        BorderStroke(
                            3.dp,
                            Brush.verticalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00838F)))
                        ),
                        shape = CutCornerShape(8.dp),
                    ),
                shape = CutCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                onClick = {
                    onClickRestart()
                }
            ) {
                Text(
                    "REMATCH",
                    style = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f)
                        ),
                        letterSpacing = 4.sp
                    )
                )
            }

            Spacer(modifier = Modifier.size(30.dp))

            Button(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        BorderStroke(
                            3.dp,
                            Brush.verticalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00838F)))
                        ),
                        shape = CutCornerShape(8.dp)
                    ),
                shape = CutCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F),
                    contentColor = Color.White
                ),
                onClick = {
                    onClickReset()
                }
            ) {
                Text(
                    "MAIN MENU",
                    style = LocalTextStyle.current.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(2f, 2f)
                        ),
                        letterSpacing = 4.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun ResultArea(
    statsList: List<List<StatCell>>,
    titles: List<MVPTitleCard>,
    isLandscape: Boolean,
    modifier: Modifier = Modifier,
) {
    if (isLandscape) {
        Row(
            modifier = modifier,
        ) {
            Dashboard(
                statsList = statsList,
                modifier = Modifier
                    .weight(7f)
                    .fillMaxHeight(),
            )

            Spacer(modifier = Modifier.size(20.dp))

            TitlesVerticalCard(
                titles = titles,
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
            )
        }
    } else {
        Column(
            modifier = modifier,
        ) {
            Dashboard(
                statsList = statsList,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )

            Spacer(modifier = Modifier.size(20.dp))

            TitlesFixedCard(
                titles = titles,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
        }
    }
}

@Composable
private fun TitlesVerticalCard(
    titles: List<MVPTitleCard>,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .border(
                BorderStroke(
                    2.dp,
                    Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B)))
                ),
            ),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            // Header
            Text(
                text = "💎 MVP 전당 💎",
                style = LocalTextStyle.current.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                ),
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .align(Alignment.CenterHorizontally),
            )

            // Title list
            LazyColumn(
                modifier = Modifier.weight(1f)
            ) {
                items(titles) { playerTitle ->
                    // Title
                    Text(
                        text = playerTitle.title,
                        style = LocalTextStyle.current.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        ),
                        modifier = Modifier.padding(bottom = 2.dp)
                    )

                    // Description
                    Text(
                        text = playerTitle.desc,
                        style = LocalTextStyle.current.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray,
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    // Players
                    LazyRow(
                        modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
                    ) {
                        items(playerTitle.players) { p ->
                            Text(
                                text = p.stat,
                                style = LocalTextStyle.current.copy(
                                    fontSize = 15.sp,
                                ),
                                color = p.color ?: Color.Unspecified,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                        }
                    }

                    // 칭호 간 간격
                    Spacer(modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun TitlesFixedCard(
    titles: List<MVPTitleCard>,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier
            .border(
                BorderStroke(
                    2.dp,
                    Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B)))
                ),
            ),
        color = Color.Transparent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp)
                .squareVerticalScrollbar(scrollState, color = Color.White)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start,
        ) {
            // Header
            Text(
                text = "💎 MVP 전당 💎",
                style = LocalTextStyle.current.copy(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                ),
                modifier = Modifier
                    .padding(bottom = 20.dp)
            )

            // Title list
            titles.forEach { playerTitle ->
                // Title
                Text(
                    text = playerTitle.title,
                    style = LocalTextStyle.current.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    ),
                    modifier = Modifier.padding(bottom = 2.dp)
                )

                // Description
                Text(
                    text = playerTitle.desc,
                    style = LocalTextStyle.current.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Players
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp, start = 4.dp)
                ) {
                    playerTitle.players.forEach { p ->
                        Text(
                            text = p.stat,
                            style = LocalTextStyle.current.copy(
                                fontSize = 15.sp,
                            ),
                            color = p.color ?: Color.Unspecified,
                            modifier = Modifier.padding(end = 4.dp),
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