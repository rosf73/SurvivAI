package com.survivai.survivai.expect

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.text.font.FontFamily

@Composable
expect fun preloadEmojiFontForFallback(fontFamilyResolver: FontFamily.Resolver): State<Boolean>

expect fun removePlatformSplashScreen()
