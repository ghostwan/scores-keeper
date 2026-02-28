package com.scoreskeeper.domain.usecase.session

import com.scoreskeeper.domain.model.Session
import com.scoreskeeper.domain.repository.SessionRepository
import javax.inject.Inject

class DeleteSessionUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(session: Session) =
        sessionRepository.deleteSession(session)
}
