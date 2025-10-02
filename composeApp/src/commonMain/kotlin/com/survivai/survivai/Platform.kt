package com.survivai.survivai

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform