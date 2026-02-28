package com.scoreskeeper.domain.repository

import com.scoreskeeper.domain.model.Player
import kotlinx.coroutines.flow.Flow

interface PlayerRepository {
    fun getAllPlayers(): Flow<List<Player>>
    suspend fun getPlayerById(id: Long): Player?
    suspend fun insertPlayer(player: Player): Long
    suspend fun updatePlayer(player: Player)
    suspend fun deletePlayer(player: Player)
}
