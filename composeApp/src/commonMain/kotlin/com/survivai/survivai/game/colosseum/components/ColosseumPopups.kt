package com.survivai.survivai.game.colosseum.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivai.survivai.expect.isDesktopPlatform
import com.survivai.survivai.game.colosseum.logic.StatCell

enum class PopupType {
    REMATCH,
    MAIN_MENU,
    SCOREBOARD,
}

@Composable
fun RematchPopup(
    onClickYes: () -> Unit,
    onClickNo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    YesOrNoPopup(
        msg = "Are you sure you want to restart?",
        onClickYes = onClickYes,
        onClickNo = onClickNo,
        modifier = modifier,
    )
}

@Composable
fun MainMenuPopup(
    onClickYes: () -> Unit,
    onClickNo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    YesOrNoPopup(
        msg = "Are you sure you want to quit to the main menu?",
        onClickYes = onClickYes,
        onClickNo = onClickNo,
        modifier = modifier,
    )
}

@Composable
private fun YesOrNoPopup(
    msg: String,
    onClickYes: () -> Unit,
    onClickNo: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onClickNo),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
        ) {
            Text(
                msg,
                style = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            )
            Spacer(modifier = Modifier.size(20.dp))
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                PopupButton("NO", onClick = onClickNo)
                Spacer(modifier = Modifier.size(10.dp))
                PopupButton("YES", onClick = onClickYes)
            }
        }
    }
}

@Composable
private fun PopupButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        modifier = modifier,
        shape = CutCornerShape(6.dp),
        border = BorderStroke(
            2.dp,
            Brush.verticalGradient(listOf(Color(0xFF00E5FF), Color(0xFF00838F)))
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFD32F2F),
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(6.dp),
        onClick = onClick,
    ) {
        Text(
            label,
            style = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(2f, 2f)
                ),
                letterSpacing = 2.sp
            )
        )
    }
}

@Composable
fun ScoreboardPopup(
    statsList: List<List<StatCell>>,
    onClickOutside: () -> Unit,
    modifier: Modifier,
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.3f))
            .clickable(onClickLabel = "close", onClick = onClickOutside),
    ) {
        ColosseumScoreboard(
            statsList = statsList,
            modifier = Modifier
                .align(Alignment.Center)
                .widthIn(min = 0.dp, max = 1000.dp)
                .fillMaxSize()
                .padding(30.dp),
        )

        Text(
            if (isDesktopPlatform) "Press TAB key to open and close"
            else "Tap outside to close",
            style = LocalTextStyle.current.copy(
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
            ),
            modifier = Modifier.align(Alignment.BottomEnd).padding(10.dp),
        )
    }
}