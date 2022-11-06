package com.ghostwan.scoreskeeper.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import com.ghostwan.scoreskeeper.model.Game
import kotlinx.coroutines.flow.Flow


typealias Games = List<Game>

@Database(entities = [Game::class], version = 1, exportSchema = false)
abstract class GameDb : RoomDatabase() {
    abstract fun gameDao(): GameDao
}

@Dao
interface GameDao {

    companion object {
        const val GAME_TABLE = "game"
    }

    @Query("SELECT * FROM $GAME_TABLE ORDER BY id ASC")
    fun getGames(): Flow<List<Game>>

    @Query("SELECT * FROM $GAME_TABLE WHERE id = :id ORDER BY id ASC")
    fun getGame(id: Int): Game

    @Insert
    fun addGame(game: Game)

    @Update
    fun updateGame(game: Game)

    @Delete
    fun deleteGame(game: Game)
}

interface GameRepository {
    fun getGames() : Flow<Games>

    fun getGame(id: Int): Game

    fun addGame(game: Game)

    fun updateGame(game: Game)

    fun deleteGame(game: Game)
}

class GameRepositoryImpl(private val gameDao: GameDao) : GameRepository {
    override fun getGames(): Flow<Games> = gameDao.getGames()

    override fun getGame(id: Int): Game = gameDao.getGame(id)

    override fun addGame(game: Game) = gameDao.addGame(game)

    override fun updateGame(game: Game) = gameDao.updateGame(game)

    override fun deleteGame(game: Game) = gameDao.deleteGame(game)

}