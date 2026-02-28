package com.ghostwan.scoreskeeper.domain.usecase.session

import com.ghostwan.scoreskeeper.domain.model.Session
import com.ghostwan.scoreskeeper.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSessionsByGameUseCase @Inject constructor(
    private val sessionRepository: SessionRepository
) {
    operator fun invoke(gameId: Long): Flow<List<Session>> =
        sessionRepository.getSessionsByGame(gameId)
}
