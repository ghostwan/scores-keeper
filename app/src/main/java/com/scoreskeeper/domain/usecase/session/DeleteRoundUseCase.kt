package com.scoreskeeper.domain.usecase.session

import com.scoreskeeper.domain.repository.SessionRepository
import javax.inject.Inject

class DeleteRoundUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(sessionId: Long, round: Int) =
        sessionRepository.deleteRound(sessionId, round)
}
