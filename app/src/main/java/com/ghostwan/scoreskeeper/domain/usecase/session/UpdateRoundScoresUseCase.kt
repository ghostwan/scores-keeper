package com.ghostwan.scoreskeeper.domain.usecase.session

import com.ghostwan.scoreskeeper.domain.model.RoundScore
import com.ghostwan.scoreskeeper.domain.repository.SessionRepository
import javax.inject.Inject

class UpdateRoundScoresUseCase @Inject constructor(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(roundScores: List<RoundScore>) =
        sessionRepository.updateRoundScores(roundScores)
}
