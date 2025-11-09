package com.survivai.survivai.game.colosseum.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ColosseumStartScreen(
    modifier: Modifier = Modifier,
    onClickStart: (List<String>) -> Unit,
) {
    val players = remember { mutableStateListOf("홍길동", "김철수") } // 최소 2명으로 시작

    Box(
        modifier = modifier
            .background(color = Color(255, 255, 255, 127)),
    ) {
        // 타이틀
        Text(
            modifier = Modifier.align(Alignment.TopCenter),
            text = "플레이어 등록",
            style = TextStyle(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
            ),
        )

        // 플레이어 등록
        ColosseumInput(
            modifier = Modifier.align(Alignment.Center),
            players = players,
            addPlayer = {
                players.add("")
            },
            removePlayer = { index ->
                players.removeAt(index)
            },
            changePlayer = { index, newName ->
                players[index] = newName
            },
        )

        // 시작 버튼
        Button(
            modifier = Modifier.align(Alignment.BottomCenter),
            onClick = {
                onClickStart(players)
            }
        ) {
            Text("경기 시작")
        }
    }
}

@Composable
private fun ColosseumInput(
    players: List<String>,
    addPlayer: () -> Unit,
    removePlayer: (Int) -> Unit,
    changePlayer: (Int, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier,
    ) {
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
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 플레이어 추가 버튼
        item {
            Button(
                onClick = addPlayer,
                enabled = players.size < 10, // 최대 10명 제한
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("추가")
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
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${index + 1}.",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 8.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                placeholder = { Text("플레이어 이름") },
                modifier = Modifier.weight(1f),
                singleLine = true
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