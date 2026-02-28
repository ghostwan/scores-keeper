package com.scoreskeeper.domain.repository

import com.scoreskeeper.domain.model.PlayerStats
import com.scoreskeeper.domain.model.RoundScore
import com.scoreskeeper.domain.model.Session
import com.scoreskeeper.domain.model.SessionDetail
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    fun getSessionsByGame(gameId: Long): Flow<List<Session>>
    fun getAllSessions(): Flow<List<Session>>
    suspend fun getSessionById(id: Long): Session?
    suspend fun getSessionDetail(sessionId: Long): SessionDetail?
    fun getSessionDetailFlow(sessionId: Long): Flow<SessionDetail?>
    suspend fun createSession(session: Session): Long
    suspend fun finishSession(sessionId: Long)
    suspend fun deleteSession(session: Session)

    suspend fun addRoundScore(roundScore: RoundScore)
    suspend fun deleteRoundScore(roundScore: RoundScore)
    fun getRoundsForSession(sessionId: Long): Flow<List<RoundScore>>

    fun getPlayerStatsForGame(gameId: Long): Flow<List<PlayerStats>>
}
