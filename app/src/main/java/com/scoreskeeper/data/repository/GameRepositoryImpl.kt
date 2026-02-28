package com.scoreskeeper.data.repository

import com.scoreskeeper.data.local.dao.GameDao
import com.scoreskeeper.data.local.mapper.toDomain
import com.scoreskeeper.data.local.mapper.toEntity
import com.scoreskeeper.domain.model.Game
import com.scoreskeeper.domain.repository.GameRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GameRepositoryImpl @Inject constructor(
    private val gameDao: GameDao
) : GameRepository {

    override fun getAllGames(): Flow<List<Game>> =
        gameDao.getAllGames().map { list -> list.map { it.toDomain() } }

    override suspend fun getGameById(id: Long): Game? =
        gameDao.getGameById(id)?.toDomain()

    override suspend fun insertGame(game: Game): Long =
        gameDao.insertGame(game.toEntity())

    override suspend fun updateGame(game: Game) =
        gameDao.updateGame(game.toEntity())

    override suspend fun deleteGame(game: Game) =
        gameDao.deleteGame(game.toEntity())
}
