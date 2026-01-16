package com.survivai.survivai.game.colosseum.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivai.survivai.game.colosseum.entity.PlayerInitPair
import com.survivai.survivai.game.colosseum.entity.generateUniquePlayerPool
import kotlin.math.roundToInt

@Composable
fun ColosseumStartScreen(
    onClickStart: (players: List<PlayerInitPair>, hp: Int) -> Unit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    val initialPool = remember { generateUniquePlayerPool(2).toList() }
    val players = remember { mutableStateListOf<PlayerInitPair>().apply { addAll(initialPool) } }
    var hpSliderValue by remember { mutableFloatStateOf(3f) }

    // 고전 게임 감성의 어두운 배경
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .drawBehind {
                // 미세한 격자 무늬 배경 효과
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
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header: 강렬한 게임 타이틀 스타일
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            2.dp,
                            Brush.verticalGradient(listOf(Color(0xFFFFD700), Color(0xFFB8860B)))
                        )
                    ),
                color = Color(0xFF1A1A1A),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "COLOSSEUM",
                        style = TextStyle(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = fontFamily,
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
                        text = "PLAYER REGISTRATION",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily,
                            color = Color(0xFF00E5FF),
                            letterSpacing = 2.sp
                        )
                    )
                }
            }

            // Scrollable Grid Content
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // HP Setting (Full Width)
                item(span = { GridItemSpan(2) }) {
                    HpSettingCard(
                        hpValue = hpSliderValue.roundToInt(),
                        onHpChange = { hpSliderValue = it },
                        fontFamily = fontFamily
                    )
                }

                // Players (2 Columns)
                itemsIndexed(players) { index, player ->
                    PlayerInputCard(
                        name = player.name,
                        color = player.color,
                        onNameChange = { newName -> players[index] = player.copy(name = newName) },
                        onDelete = if (players.size > 2) {
                            { players.removeAt(index) }
                        } else null,
                        fontFamily = fontFamily
                    )
                }

                // Add Player Button (Full Width)
                item(span = { GridItemSpan(2) }) {
                    OutlinedButton(
                        onClick = { 
                            if (players.size < 8) {
                                // find first pair not exist in players
                                generateUniquePlayerPool(8)
                                    .firstOrNull { new ->
                                        !players.any { it.color == new.color }
                                    }
                                    ?.let {
                                        players.add(it)
                                    }
                            }
                        },
                        enabled = players.size < 8,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = CutCornerShape(4.dp),
                        border = BorderStroke(1.dp, Color(0xFF00E5FF)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF00E5FF)
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "ADD CHALLENGER", 
                            style = TextStyle(
                                fontFamily = fontFamily, 
                                fontWeight = FontWeight.ExtraBold, 
                                fontSize = 14.sp,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                }

                item(span = { GridItemSpan(2) }) { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Footer / Start Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))),
                color = Color(0xFF0A0A0A),
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(bottom = 8.dp)
                ) {
                    Button(
                        onClick = {
                            val validPlayers = players.filter { it.name.isNotBlank() }
                            if (validPlayers.size >= 2) {
                                onClickStart(validPlayers, hpSliderValue.roundToInt())
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
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
                        )
                    ) {
                        Text(
                            "FIGHT!",
                            style = TextStyle(
                                fontFamily = fontFamily,
                                fontSize = 24.sp,
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
    }
}

@Composable
private fun HpSettingCard(
    hpValue: Int,
    onHpChange: (Float) -> Unit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(BorderStroke(1.dp, Color(0xFF333333)), CutCornerShape(4.dp)),
        shape = CutCornerShape(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "INITIAL HP",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFBBBBBB),
                        letterSpacing = 1.sp
                    ),
                )
                Text(
                    text = "$hpValue",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFFD700),
                        shadow = Shadow(color = Color(0xFFB8860B), offset = Offset(2f, 2f))
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = hpValue.toFloat(),
                onValueChange = onHpChange,
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = Color(0xFFFFD700),
                    activeTrackColor = Color(0xFFD32F2F),
                    inactiveTrackColor = Color(0xFF333333)
                )
            )
        }
    }
}

@Composable
private fun PlayerInputCard(
    name: String,
    color: Color,
    onNameChange: (String) -> Unit,
    onDelete: (() -> Unit)?,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .border(BorderStroke(1.dp, Color(0xFF444444)), CutCornerShape(2.dp)),
        shape = CutCornerShape(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF222222)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp), // 상하 패딩을 줄여 높이 감소
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. color circle
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .background(color = color, shape = androidx.compose.foundation.shape.CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.3f), androidx.compose.foundation.shape.CircleShape)
            )
            // 2. name input (weight 1)
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 10) onNameChange(it) },
                placeholder = {
                    Text(
                        "NAME",
                        style = TextStyle(fontFamily = fontFamily, fontSize = 10.sp, color = Color.Gray)
                    )
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(
                    fontFamily = fontFamily,
                    fontSize = 13.sp, // 폰트 크기 살짝 조절
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                shape = CutCornerShape(0.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF111111),
                    unfocusedContainerColor = Color(0xFF111111),
                    focusedIndicatorColor = Color(0xFF00E5FF),
                    unfocusedIndicatorColor = Color(0xFF333333),
                    cursorColor = Color(0xFF00E5FF)
                )
            )
            // 3. delete icon
            if (onDelete != null) {
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "REMOVE",
                        tint = Color(0xFFD32F2F).copy(alpha = 0.8f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(24.dp))
            }
        }
    }
}