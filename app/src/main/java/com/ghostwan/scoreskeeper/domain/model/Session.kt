package com.ghostwan.scoreskeeper.domain.model

import java.time.LocalDateTime

enum class SessionStatus { IN_PROGRESS, FINISHED }

/**
 * Represents a single game session (a "partie").
 */
data class Session(
    val id: Long = 0,
    val gameId: Long,
    val gameName: String = "",
    val playerIds: List<Long> = emptyList(),
    val status: SessionStatus = SessionStatus.IN_PROGRESS,
    val startedAt: LocalDateTime = LocalDateTime.now(),
    val finishedAt: LocalDateTime? = null,
)
