package com.ghostwan.scoreskeeper.database

import com.ghostwan.scoreskeeper.model.Game
import io.realm.kotlin.Realm
import kotlinx.coroutines.flow.Flow
import io.realm.query


typealias Games = List<Game>

interface GameRepository {
    fun getGames() : Flow<Games>

    fun getGame(id: Long): Game

    fun addGame(game: Game)

    fun updateGame(game: Game)

    fun deleteGame(game: Game)
}

class GameRepositoryImpl(private val realm: Realm) : GameRepository {
    override fun getGames(): Flow<Games>  {
        realm.query(Game::class)
    }

    override fun getGame(id: Long): Game = gameDao.getGame(id)

    override fun addGame(game: Game) {
        realm.writeBlocking {
            copyToRealm(game)
        }
    }

    override fun updateGame(game: Game) = gameDao.updateGame(game)

    override fun deleteGame(game: Game) = gameDao.deleteGame(game)

}