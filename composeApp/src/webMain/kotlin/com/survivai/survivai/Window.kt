package com.survivai.survivai

// 브라우저 window 객체에 대한 external 선언
external object Window {
    fun open(url: String, target: String): Any?
}

external val window: Window