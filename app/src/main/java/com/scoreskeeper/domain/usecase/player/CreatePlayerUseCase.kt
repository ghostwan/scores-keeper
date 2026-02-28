package com.scoreskeeper.domain.usecase.player

import com.scoreskeeper.domain.model.Player
import com.scoreskeeper.domain.repository.PlayerRepository
import javax.inject.Inject

class CreatePlayerUseCase @Inject constructor(
    private val playerRepository: PlayerRepository
) {
    suspend operator fun invoke(player: Player): Long = playerRepository.insertPlayer(player)
}
