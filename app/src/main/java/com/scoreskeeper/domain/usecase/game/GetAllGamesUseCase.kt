package com.scoreskeeper.domain.usecase.game

import com.scoreskeeper.domain.model.Game
import com.scoreskeeper.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllGamesUseCase @Inject constructor(
    private val gameRepository: GameRepository
) {
    operator fun invoke(): Flow<List<Game>> = gameRepository.getAllGames()
}
