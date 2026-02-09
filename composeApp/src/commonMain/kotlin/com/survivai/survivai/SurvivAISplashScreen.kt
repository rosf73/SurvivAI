package com.survivai.survivai

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivai.survivai.common.survivAIBackground
import org.jetbrains.compose.resources.painterResource
import survivai.composeapp.generated.resources.Res
import survivai.composeapp.generated.resources.icon_app

@Composable
fun SurvivAISplashScreen(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .survivAIBackground(),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(Res.drawable.icon_app),
                contentDescription = "App Icon",
                modifier = Modifier.size(50.dp),
            )

            Spacer(modifier = Modifier.size(10.dp))

            Text(
                text = "WAITING...",
                style = TextStyle(fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Black),
            )
        }
    }
}