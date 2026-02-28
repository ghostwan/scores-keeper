package com.scoreskeeper.domain.usecase.session

import com.scoreskeeper.domain.model.Player
import com.scoreskeeper.domain.model.Session
import com.scoreskeeper.domain.model.SessionStatus
import com.scoreskeeper.domain.repository.SessionRepository
import javax.inject.Inject

class CreateSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(gameId: Long, players: List<Player>): Long {
        val session = Session(
            gameId = gameId,
            playerIds = players.map { it.id },
            status = SessionStatus.IN_PROGRESS,
        )
        return sessionRepository.createSession(session)
    }
}
