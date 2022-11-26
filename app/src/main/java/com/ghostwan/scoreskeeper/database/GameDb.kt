package com.ghostwan.scoreskeeper.database

import androidx.room.*
import com.ghostwan.scoreskeeper.dao.GameDao
import com.ghostwan.scoreskeeper.model.Game
import com.ghostwan.scoreskeeper.model.Party
import kotlinx.coroutines.flow.Flow


typealias Games = List<Game>

@Database(entities = [Game::class, Party::class], version = 1, exportSchema = false)
@TypeConverters(TimestampConverter::class, PartyListConverter::class)
abstract class GameDb : RoomDatabase() {
    abstract fun gameDao(): GameDao
}


interface GameRepository {
    fun getGames() : Flow<Games>

    fun getGame(id: Long): Game

    fun addGame(game: Game)

    fun updateGame(game: Game)

    fun deleteGame(game: Game)
}

class GameRepositoryImpl(private val gameDao: GameDao) : GameRepository {
    override fun getGames(): Flow<Games> = gameDao.getGames()

    override fun getGame(id: Long): Game = gameDao.getGame(id)

    override fun addGame(game: Game) = gameDao.addGame(game)

    override fun updateGame(game: Game) = gameDao.updateGame(game)

    override fun deleteGame(game: Game) = gameDao.deleteGame(game)

}