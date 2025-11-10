package com.survivai.survivai.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

// GitHub 아이콘 생성
fun createGitHubIcon(): ImageVector {
    return ImageVector.Builder(
        name = "GitHub",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.White)) {
            // GitHub 로고 path (simplified version)
            moveTo(12f, 2f)
            curveTo(6.477f, 2f, 2f, 6.477f, 2f, 12f)
            curveTo(2f, 16.42f, 4.865f, 20.127f, 8.839f, 21.488f)
            curveTo(9.339f, 21.577f, 9.521f, 21.272f, 9.521f, 21.007f)
            curveTo(9.521f, 20.77f, 9.513f, 20.146f, 9.508f, 19.347f)
            curveTo(6.726f, 19.945f, 6.139f, 17.896f, 6.139f, 17.896f)
            curveTo(5.685f, 16.704f, 5.029f, 16.401f, 5.029f, 16.401f)
            curveTo(4.121f, 15.787f, 5.098f, 15.799f, 5.098f, 15.799f)
            curveTo(6.101f, 15.869f, 6.629f, 16.821f, 6.629f, 16.821f)
            curveTo(7.521f, 18.361f, 8.97f, 17.922f, 9.539f, 17.666f)
            curveTo(9.631f, 17.015f, 9.889f, 16.577f, 10.175f, 16.327f)
            curveTo(7.955f, 16.074f, 5.62f, 15.215f, 5.62f, 11.381f)
            curveTo(5.62f, 10.253f, 6.01f, 9.333f, 6.649f, 8.612f)
            curveTo(6.546f, 8.359f, 6.203f, 7.35f, 6.747f, 5.968f)
            curveTo(6.747f, 5.968f, 7.586f, 5.698f, 9.497f, 7.035f)
            curveTo(10.312f, 6.816f, 11.158f, 6.707f, 12f, 6.703f)
            curveTo(12.842f, 6.707f, 13.688f, 6.816f, 14.504f, 7.035f)
            curveTo(16.414f, 5.698f, 17.252f, 5.968f, 17.252f, 5.968f)
            curveTo(17.797f, 7.35f, 17.454f, 8.359f, 17.351f, 8.612f)
            curveTo(17.991f, 9.333f, 18.38f, 10.253f, 18.38f, 11.381f)
            curveTo(18.38f, 15.225f, 16.041f, 16.072f, 13.813f, 16.319f)
            curveTo(14.172f, 16.634f, 14.491f, 17.252f, 14.491f, 18.204f)
            curveTo(14.491f, 19.568f, 14.479f, 20.669f, 14.479f, 21.007f)
            curveTo(14.479f, 21.274f, 14.659f, 21.581f, 15.167f, 21.487f)
            curveTo(19.137f, 20.123f, 22f, 16.418f, 22f, 12f)
            curveTo(22f, 6.477f, 17.523f, 2f, 12f, 2f)
            close()
        }
    }.build()
}