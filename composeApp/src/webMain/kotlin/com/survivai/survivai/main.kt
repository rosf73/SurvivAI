package com.survivai.survivai

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "root") {
        App(
            openLink = { link ->
                window.open(link, "_blank")
            }
        )
    }
}