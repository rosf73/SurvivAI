package com.survivai.survivai

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFontFamilyResolver
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.survivai.survivai.common.LocalFont
import com.survivai.survivai.common.createGitHubIcon
import com.survivai.survivai.common.withFontFamily
import com.survivai.survivai.config.BuildConfig
import com.survivai.survivai.game.colosseum.Colosseum
import org.jetbrains.compose.resources.Font
import survivai.composeapp.generated.resources.NotoEmojiColor
import survivai.composeapp.generated.resources.NotoSansKR
import survivai.composeapp.generated.resources.Res
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun App(
    openLink: (String) -> Unit, // TODO : hilt injection
) {
    val containerSize = LocalWindowInfo.current.containerSize
    val isLandscape = containerSize.width >= containerSize.height

    val fontFamily = FontFamily(
        Font(Res.font.NotoSansKR),
        Font(Res.font.NotoEmojiColor),
    )

    // font preload
    val fontFamilyResolver = LocalFontFamilyResolver.current
    val isFontLoaded = preloadEmojiFontForFallback(fontFamilyResolver)

    var showSplash by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val minSplashTime = 2000L
        val splashStart = Clock.System.now().toEpochMilliseconds()

        // Wait for font load
        snapshotFlow { isFontLoaded.value }.first { it }

        // Ensure minimum splash duration
        val elapsed = Clock.System.now().toEpochMilliseconds() - splashStart
        if (elapsed < minSplashTime) {
            delay(minSplashTime - elapsed)
        }

        showSplash = false
    }

    if (showSplash) {
        SurvivAISplashScreen(modifier = Modifier.fillMaxSize())
    } else {
        removePlatformSplashScreen()

        CompositionLocalProvider(LocalFont provides fontFamily) {
        MaterialTheme(
            typography = Typography().withFontFamily(fontFamily),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Colosseum
                Colosseum(
                    isLandscape = isLandscape,
                    modifier = Modifier.fillMaxSize(),
                )

                // Top right buttons
                if (isLandscape) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        VersionText()

                        GitHubButton(
                            openLink = openLink,
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        GitHubIconButton(
                            openLink = openLink,
                            modifier = Modifier.size(30.dp)
                        )

                        VersionText()
                    }
                }
            }
        }
    }
    }
}


@Composable
private fun VersionText(
    modifier: Modifier = Modifier,
) {
    Spacer(modifier = Modifier.size(2.dp))
    Text(
        modifier = modifier
            .background(
                color = Color.DarkGray,
                shape = RoundedCornerShape(4.dp),
            )
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)), RoundedCornerShape(4.dp))
            .padding(6.dp, 1.dp),
        text = "v${BuildConfig.VERSION_NAME}",
        style = TextStyle(fontSize = 12.sp, color = Color.White),
    )
    Spacer(modifier = Modifier.size(2.dp))
}

@Composable
private fun GitHubButton(
    openLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = {
            openLink("https://github.com/rosf73/SurvivAI")
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White,
        ),
        modifier = modifier,
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
            )
        }
    }
}

@Composable
private fun GitHubIconButton(
    openLink: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Button(
        onClick = {
            openLink("https://github.com/rosf73/SurvivAI")
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White,
        ),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(4.dp),
        modifier = modifier,
    ) {
        // GitHub 아이콘
        Icon(
            imageVector = createGitHubIcon(),
            contentDescription = "GitHub",
            modifier = Modifier.fillMaxSize(),
            tint = Color.White,
        )
    }
}
