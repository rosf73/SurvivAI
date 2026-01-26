package com.survivai.survivai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivai.survivai.common.createGitHubIcon
import com.survivai.survivai.config.BuildConfig
import com.survivai.survivai.game.colosseum.Colosseum
import org.jetbrains.compose.resources.Font
import survivai.composeapp.generated.resources.NotoEmojiColor
import survivai.composeapp.generated.resources.NotoSansKR
import survivai.composeapp.generated.resources.Res

@Composable
fun App(
    openLink: (String) -> Unit,
) {
    val containerSize = LocalWindowInfo.current.containerSize
    val ratioV = containerSize.height.toDouble() / (containerSize.width + containerSize.height) * 10
    val ratioH = containerSize.width.toDouble() / (containerSize.width + containerSize.height) * 10

    val fontFamily = FontFamily(
        Font(Res.font.NotoSansKR),
        Font(Res.font.NotoEmojiColor),
    )

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            // Colosseum
            Colosseum(
                ratio = ratioH.toInt() to ratioV.toInt(),
                fontFamily = fontFamily,
                modifier = Modifier.fillMaxSize(),
            )

            // Top right buttons
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Version
                VersionText(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    fontFamily = fontFamily,
                )

                // GitHub Link Button
                Button(
                    onClick = {
                        openLink("https://github.com/rosf73/SurvivAI")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White,
                    ),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // GitHub 아이콘
                        Icon(
                            imageVector = createGitHubIcon(),
                            contentDescription = "GitHub",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White,
                        )
                        Text(
                            text = "GitHub",
                            fontFamily = fontFamily,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VersionText(
    modifier: Modifier = Modifier,
    fontFamily: FontFamily,
) {
    Spacer(modifier = Modifier.size(5.dp))
    Text(
        modifier = modifier,
        text = "v${BuildConfig.VERSION_NAME}",
        style = TextStyle(fontSize = 12.sp, color = Color.Gray, fontFamily = fontFamily),
    )
    Spacer(modifier = Modifier.size(5.dp))
}
