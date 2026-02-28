package com.ghostwan.scoreskeeper.domain.usecase.session

import com.ghostwan.scoreskeeper.domain.model.SessionDetail
import com.ghostwan.scoreskeeper.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionDetailUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(sessionId: Long): Flow<SessionDetail?> =
        sessionRepository.getSessionDetailFlow(sessionId)
}
