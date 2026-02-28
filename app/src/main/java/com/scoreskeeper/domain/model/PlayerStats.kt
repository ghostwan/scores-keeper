package com.scoreskeeper.domain.model

/**
 * Aggregated stats for a player across all sessions of a given game.
 */
data class PlayerStats(
    val player: Player,
    val gamesPlayed: Int,
    val gamesWon: Int,
    val totalPoints: Int,
    val winRate: Float = if (gamesPlayed > 0) gamesWon.toFloat() / gamesPlayed else 0f,
)
