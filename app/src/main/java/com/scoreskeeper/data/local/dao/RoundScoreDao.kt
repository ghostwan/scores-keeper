package com.scoreskeeper.data.local.dao

import androidx.room.*
import com.scoreskeeper.data.local.entity.RoundScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoundScoreDao {
    @Query("SELECT * FROM round_scores WHERE sessionId = :sessionId ORDER BY round ASC, playerId ASC")
    fun getRoundsForSession(sessionId: Long): Flow<List<RoundScoreEntity>>

    @Query("SELECT * FROM round_scores WHERE sessionId = :sessionId ORDER BY round ASC, playerId ASC")
    suspend fun getRoundsForSessionOnce(sessionId: Long): List<RoundScoreEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoundScore(roundScore: RoundScoreEntity): Long

    @Delete
    suspend fun deleteRoundScore(roundScore: RoundScoreEntity)

    @Query(
        """
        SELECT rs.playerId, SUM(rs.points) as totalPoints
        FROM round_scores rs
        INNER JOIN sessions s ON rs.sessionId = s.id
        WHERE s.gameId = :gameId AND s.status = 'FINISHED'
        GROUP BY rs.playerId
    """
    )
    suspend fun getTotalPointsPerPlayerForGame(gameId: Long): List<PlayerPointsResult>
}

data class PlayerPointsResult(
    val playerId: Long,
    val totalPoints: Int,
)
