package com.survivai.survivai.game.colosseum.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivai.survivai.game.colosseum.state.ColosseumInfo

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
            items(ColosseumInfo.logEntries) { line ->
                Text(
                    text = line,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(vertical = 2.dp),
                    fontFamily = fontFamily,
                )
            }
        }
    }
}