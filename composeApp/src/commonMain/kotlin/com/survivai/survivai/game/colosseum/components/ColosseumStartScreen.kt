package com.survivai.survivai.game.colosseum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun ColosseumStartScreen(
    onClickStart: (playerNames: List<String>, hp: Int) -> Unit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    val players = remember { mutableStateListOf("홍길동", "김철수") } // 최소 2명으로 시작
    var hpSliderValue by remember { mutableFloatStateOf(3f) }

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

        // 플레이어 등록
        ColosseumInput(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            players = players,
            hpValue = hpSliderValue.roundToInt(),
            onHpChange = { hpSliderValue = it },
            addPlayer = {
                players.add("")
            },
            removePlayer = { index ->
                players.removeAt(index)
            },
            changePlayer = { index, newName ->
                players[index] = newName
            },
            fontFamily = fontFamily,
        )

        // 시작 버튼
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
                onClickStart(players, hpSliderValue.roundToInt())
            }
        ) {
            Text("경기 시작", style = TextStyle(fontFamily = fontFamily))
        }
    }
}

@Composable
private fun ColosseumInput(
    players: List<String>,
    hpValue: Int,
    onHpChange: (Float) -> Unit,
    addPlayer: () -> Unit,
    removePlayer: (Int) -> Unit,
    changePlayer: (Int, String) -> Unit,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
    ) {
        // HP 설정 카드
        item {
            HpSettingCard(
                hpValue = hpValue,
                onHpChange = onHpChange,
                fontFamily = fontFamily,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        itemsIndexed(players) { index, name ->
            PlayerInputCard(
                index = index,
                name = name,
                onNameChange = { newName ->
                    changePlayer(index, newName)
                },
                onDelete = if (players.size > 2) {
                    { removePlayer(index) }
                } else null, // 최소 2명 유지
                fontFamily = fontFamily,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.size(10.dp))
        }

        // 플레이어 추가 버튼
        item {
            Button(
                onClick = addPlayer,
                enabled = players.size < 8, // 최대 8명 제한
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("추가", style = TextStyle(fontFamily = fontFamily))
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
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "시작 HP 설정",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Text(
                    text = "$hpValue",
                    style = TextStyle(
                        fontFamily = fontFamily,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Slider(
                value = hpValue.toFloat(),
                onValueChange = onHpChange,
                valueRange = 1f..10f,
                steps = 8, // 1부터 10까지 9개 값 (1,2,3,...,10)
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "1",
                    style = TextStyle(fontFamily = fontFamily, fontSize = 12.sp),
                    color = Color.Gray,
                )
                Text(
                    text = "10",
                    style = TextStyle(fontFamily = fontFamily, fontSize = 12.sp),
                    color = Color.Gray,
                )
            }
        }
    }
}

@Composable
private fun PlayerInputCard(
    index: Int,
    name: String,
    onNameChange: (String) -> Unit,
    onDelete: (() -> Unit)?,
    fontFamily: FontFamily,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${index + 1}.",
                style = TextStyle(fontFamily = fontFamily),
                modifier = Modifier.padding(end = 8.dp),
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("플레이어 이름", style = TextStyle(fontFamily = fontFamily)) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(fontFamily = fontFamily),
            )

            // 삭제 버튼 (3명 이상일 때만 표시)
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Close, "삭제")
                }
            }
        }
    }
}