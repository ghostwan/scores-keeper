package com.scoreskeeper.domain.repository

import com.scoreskeeper.domain.model.Game
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun getAllGames(): Flow<List<Game>>
    suspend fun getGameById(id: Long): Game?
    suspend fun insertGame(game: Game): Long
    suspend fun updateGame(game: Game)
    suspend fun deleteGame(game: Game)
}
