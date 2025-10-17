package com.survivai.survivai.game.colosseum

import androidx.compose.runtime.mutableStateListOf

/**
 * Shared combat/event log store observable by UI across platforms.
 */
object CombatLogStore {
    val entries = mutableStateListOf<String>()

    fun add(message: String) {
        entries.add(message)
        // Keep a reasonable cap
        if (entries.size > 200) {
            // remove oldest extra elements to keep list bounded
            repeat(entries.size - 200) { entries.removeAt(0) }
        }
    }
}