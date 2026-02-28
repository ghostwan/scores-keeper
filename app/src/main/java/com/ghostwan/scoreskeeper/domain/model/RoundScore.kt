package com.ghostwan.scoreskeeper.domain.model

/**
 * A score entry for one player in one round of a session.
 */
data class RoundScore(
    val id: Long = 0,
    val sessionId: Long,
    val playerId: Long,
    val playerName: String = "",
    val round: Int,
    val points: Int,
)
