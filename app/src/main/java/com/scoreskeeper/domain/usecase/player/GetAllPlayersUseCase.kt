package com.scoreskeeper.domain.usecase.player

import com.scoreskeeper.domain.model.Player
import com.scoreskeeper.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllPlayersUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    operator fun invoke(): Flow<List<Player>> = playerRepository.getAllPlayers()
}
