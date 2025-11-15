package com.survivai.survivai.common

fun Long.msToMMSS(): String {
    val sec = this / 1000
    val formattedSec = if (sec < 10) "0$sec" else "$sec"
    val min = sec / 60
    val formattedMin = if (min < 10) "0$min" else "$min"
    return "$formattedMin:$formattedSec"
}