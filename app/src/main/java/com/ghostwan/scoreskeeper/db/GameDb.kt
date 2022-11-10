package com.ghostwan.scoreskeeper.db

import androidx.room.*
import com.ghostwan.scoreskeeper.model.Game
import com.ghostwan.scoreskeeper.model.Party
import kotlinx.coroutines.flow.Flow


typealias Games = List<Game>

@Database(entities = [Game::class, Party::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class GameDb : RoomDatabase() {
    abstract fun gameDao(): GameDao
}

@Dao
interface GameDao {

    companion object {
        const val GAME_TABLE = "game"
        const val PARTY_TABLE = "party"
    }

    @Query("SELECT * FROM $GAME_TABLE ORDER BY id ASC")
    fun getGames(): Flow<List<Game>>

    @Query("SELECT * FROM $GAME_TABLE WHERE id = :id ORDER BY id ASC")
    fun getGame(id: Long): Game

    @Insert
    fun addGame(game: Game)

    @Update
    fun updateGame(game: Game)

    @Delete
    fun deleteGame(game: Game)

    @Query(
        "SELECT * FROM party WHERE gameId = :id"
    )
    fun getPartiesForGame(id: Long): List<Party>
}

interface GameRepository {
    fun getGames() : Flow<Games>

    fun getGame(id: Long): Game

    fun addGame(game: Game)

    fun updateGame(game: Game)

    fun deleteGame(game: Game)

    fun getPartiesForGame(game: Game): List<Party>
}

class GameRepositoryImpl(private val gameDao: GameDao) : GameRepository {
    override fun getGames(): Flow<Games> = gameDao.getGames()

    override fun getGame(id: Long): Game = gameDao.getGame(id)

    override fun addGame(game: Game) = gameDao.addGame(game)

    override fun updateGame(game: Game) = gameDao.updateGame(game)

    override fun deleteGame(game: Game) = gameDao.deleteGame(game)

    override fun getPartiesForGame(game: Game) = gameDao.getPartiesForGame(game.id)


}