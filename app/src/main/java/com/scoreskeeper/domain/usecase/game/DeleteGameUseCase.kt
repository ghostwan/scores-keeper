package com.scoreskeeper.domain.usecase.game

import com.scoreskeeper.domain.model.Game
import com.scoreskeeper.domain.repository.GameRepository
import javax.inject.Inject

class DeleteGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(game: Game) = gameRepository.deleteGame(game)
}
