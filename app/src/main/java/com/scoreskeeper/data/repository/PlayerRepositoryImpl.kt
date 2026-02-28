package com.scoreskeeper.data.repository

import com.scoreskeeper.data.local.dao.PlayerDao
import com.scoreskeeper.data.local.mapper.toDomain
import com.scoreskeeper.data.local.mapper.toEntity
import com.scoreskeeper.domain.model.Player
import com.scoreskeeper.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PlayerRepositoryImpl @Inject constructor(
    private val playerDao: PlayerDao
) : PlayerRepository {

    override fun getAllPlayers(): Flow<List<Player>> =
        playerDao.getAllPlayers().map { list -> list.map { it.toDomain() } }

    override suspend fun getPlayerById(id: Long): Player? =
        playerDao.getPlayerById(id)?.toDomain()

    override suspend fun insertPlayer(player: Player): Long =
        playerDao.insertPlayer(player.toEntity())

    override suspend fun updatePlayer(player: Player) =
        playerDao.updatePlayer(player.toEntity())

    override suspend fun deletePlayer(player: Player) =
        playerDao.deletePlayer(player.toEntity())
}
