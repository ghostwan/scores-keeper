package com.scoreskeeper.domain.usecase.session

import com.scoreskeeper.domain.model.RoundScore
import com.scoreskeeper.domain.repository.SessionRepository
import javax.inject.Inject

class UpdateRoundScoresUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(roundScores: List<RoundScore>) =
        sessionRepository.updateRoundScores(roundScores)
}
