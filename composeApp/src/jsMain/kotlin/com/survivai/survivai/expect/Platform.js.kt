package com.survivai.survivai.expect

actual val isDesktopPlatform: Boolean
    get() = js("!/Android|iPhone|iPad/i.test(navigator.userAgent)") as Boolean