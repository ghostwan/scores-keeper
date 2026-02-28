package com.scoreskeeper.data.local.dao

import androidx.room.*
import com.scoreskeeper.data.local.entity.SessionEntity
import com.scoreskeeper.data.local.entity.SessionPlayerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE gameId = :gameId ORDER BY startedAt DESC")
    fun getSessionsByGame(gameId: Long): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions ORDER BY startedAt DESC")
    fun getAllSessions(): Flow<List<SessionEntity>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun getSessionById(id: Long): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: SessionEntity): Long

    @Update
    suspend fun updateSession(session: SessionEntity)

    @Delete
    suspend fun deleteSession(session: SessionEntity)

    // Session-Players join
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionPlayers(sessionPlayers: List<SessionPlayerEntity>)

    @Query("SELECT playerId FROM session_players WHERE sessionId = :sessionId")
    suspend fun getPlayerIdsForSession(sessionId: Long): List<Long>

    @Query("SELECT playerId FROM session_players WHERE sessionId = :sessionId")
    fun getPlayerIdsForSessionFlow(sessionId: Long): Flow<List<Long>>
}
