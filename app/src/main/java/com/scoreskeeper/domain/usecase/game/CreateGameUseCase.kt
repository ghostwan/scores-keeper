package com.scoreskeeper.domain.usecase.game

import com.scoreskeeper.domain.model.Game
import com.scoreskeeper.domain.repository.GameRepository
import javax.inject.Inject

class CreateGameUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    suspend operator fun invoke(game: Game): Long = gameRepository.insertGame(game)
}
