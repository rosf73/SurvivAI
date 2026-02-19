package com.survivai.survivai.expect

@OptIn(ExperimentalWasmJsInterop::class)
@JsFun("() => /Android|iPhone|iPad/i.test(navigator.userAgent)")
private external fun isMobileUserAgent(): Boolean
actual val isDesktopPlatform: Boolean
    get() = !isMobileUserAgent()