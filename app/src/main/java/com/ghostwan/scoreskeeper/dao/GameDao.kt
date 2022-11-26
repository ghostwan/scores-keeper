package com.ghostwan.scoreskeeper.dao

import androidx.room.*
import com.ghostwan.scoreskeeper.model.Game
import kotlinx.coroutines.flow.Flow

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
}