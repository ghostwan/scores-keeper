package com.ghostwan.scoreskeeper.domain.usecase.session

import com.ghostwan.scoreskeeper.domain.repository.SessionRepository
import javax.inject.Inject

class FinishSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    suspend operator fun invoke(sessionId: Long) =
        sessionRepository.finishSession(sessionId)
}
