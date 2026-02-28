package com.scoreskeeper.domain.model

/**
 * Represents a player profile reusable across multiple sessions.
 */
data class Player(
    val id: Long = 0,
    val name: String,
    val avatarColor: Long = 0xFF6200EE,
)
