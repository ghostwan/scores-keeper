package com.ghostwan.scoreskeeper.domain.model

import java.time.LocalDateTime

/**
 * Represents a game type (e.g. Uno, Tarot, custom game).
 */
data class Game(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val minPlayers: Int = 2,
    val maxPlayers: Int = Int.MAX_VALUE,
    val lowestScoreWins: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
