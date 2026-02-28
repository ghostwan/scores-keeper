package com.ghostwan.scoreskeeper.domain.usecase.game

import com.ghostwan.scoreskeeper.domain.model.Game
import com.ghostwan.scoreskeeper.domain.repository.GameRepository
import javax.inject.Inject

class CreateGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(game: Game): Long = gameRepository.insertGame(game)
}
