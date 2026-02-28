package com.scoreskeeper.data.repository

import com.scoreskeeper.data.local.dao.GameDao
import com.scoreskeeper.data.local.dao.PlayerDao
import com.scoreskeeper.data.local.dao.RoundScoreDao
import com.scoreskeeper.data.local.dao.SessionDao
import com.scoreskeeper.data.local.entity.SessionPlayerEntity
import com.scoreskeeper.data.local.mapper.toDomain
import com.scoreskeeper.data.local.mapper.toEntity
import com.scoreskeeper.domain.model.PlayerStats
import com.scoreskeeper.domain.model.RoundScore
import com.scoreskeeper.domain.model.Session
import com.scoreskeeper.domain.model.SessionDetail
import com.scoreskeeper.domain.model.SessionStatus
import com.scoreskeeper.domain.repository.SessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class SessionRepositoryImpl @Inject constructor(
    private val sessionDao: SessionDao,
    private val playerDao: PlayerDao,
    private val gameDao: GameDao,
    private val roundScoreDao: RoundScoreDao,
) : SessionRepository {

    override fun getSessionsByGame(gameId: Long): Flow<List<Session>> =
        sessionDao.getSessionsByGame(gameId).map { sessions ->
            sessions.map { entity ->
                val playerIds = sessionDao.getPlayerIdsForSession(entity.id)
                entity.toDomain(playerIds)
            }
        }

    override fun getAllSessions(): Flow<List<Session>> =
        sessionDao.getAllSessions().map { sessions ->
            sessions.map { entity ->
                val playerIds = sessionDao.getPlayerIdsForSession(entity.id)
                entity.toDomain(playerIds)
            }
        }

    override suspend fun getSessionById(id: Long): Session? {
        val entity = sessionDao.getSessionById(id) ?: return null
        val playerIds = sessionDao.getPlayerIdsForSession(id)
        return entity.toDomain(playerIds)
    }

    override suspend fun getSessionDetail(sessionId: Long): SessionDetail? {
        val entity = sessionDao.getSessionById(sessionId) ?: return null
        val game = gameDao.getGameById(entity.gameId) ?: return null
        val playerIds = sessionDao.getPlayerIdsForSession(sessionId)
        val players = playerDao.getPlayersByIds(playerIds).map { it.toDomain() }
        val playerMap = players.associateBy { it.id }
        val rounds = roundScoreDao.getRoundsForSessionOnce(sessionId)
            .map { it.toDomain(playerMap[it.playerId]?.name ?: "") }
        val session = entity.toDomain(playerIds, game.name)
        return SessionDetail(
            session = session,
            players = players,
            rounds = rounds,
            lowestScoreWins = game.lowestScoreWins,
        )
    }

    override fun getSessionDetailFlow(sessionId: Long): Flow<SessionDetail?> {
        val roundsFlow = roundScoreDao.getRoundsForSession(sessionId)
        val sessionFlow = flow {
            val entity = sessionDao.getSessionById(sessionId)
            emit(entity)
        }
        return combine(
            roundsFlow,
            sessionDao.getAllSessions(), // triggers re-emit on session status change
        ) { rounds, _ ->
            val entity = sessionDao.getSessionById(sessionId) ?: return@combine null
            val game = gameDao.getGameById(entity.gameId) ?: return@combine null
            val playerIds = sessionDao.getPlayerIdsForSession(sessionId)
            val players = playerDao.getPlayersByIds(playerIds).map { it.toDomain() }
            val playerMap = players.associateBy { it.id }
            val domainRounds = rounds.map { it.toDomain(playerMap[it.playerId]?.name ?: "") }
            val session = entity.toDomain(playerIds, game.name)
            SessionDetail(
                session = session,
                players = players,
                rounds = domainRounds,
                lowestScoreWins = game.lowestScoreWins,
            )
        }
    }

    override suspend fun createSession(session: Session): Long {
        val sessionId = sessionDao.insertSession(session.toEntity())
        val sessionPlayers = session.playerIds.map { playerId ->
            SessionPlayerEntity(sessionId = sessionId, playerId = playerId)
        }
        sessionDao.insertSessionPlayers(sessionPlayers)
        return sessionId
    }

    override suspend fun finishSession(sessionId: Long) {
        val entity = sessionDao.getSessionById(sessionId) ?: return
        sessionDao.updateSession(
            entity.copy(
                status = SessionStatus.FINISHED.name,
                finishedAt = LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()
                    .toEpochMilli(),
            )
        )
    }

    override suspend fun deleteSession(session: Session) =
        sessionDao.deleteSession(session.toEntity())

    override suspend fun addRoundScore(roundScore: RoundScore) =
        roundScoreDao.insertRoundScore(roundScore.toEntity()).let { Unit }

    override suspend fun deleteRoundScore(roundScore: RoundScore) =
        roundScoreDao.deleteRoundScore(roundScore.toEntity())

    override suspend fun updateRoundScores(roundScores: List<RoundScore>) {
        roundScores.forEach { roundScoreDao.updateRoundScore(it.toEntity()) }
    }

    override suspend fun deleteRound(sessionId: Long, round: Int) {
        roundScoreDao.deleteRoundByNumber(sessionId, round)
        roundScoreDao.decrementRoundsAfter(sessionId, round)
    }

    override fun getRoundsForSession(sessionId: Long): Flow<List<RoundScore>> =
        roundScoreDao.getRoundsForSession(sessionId).map { list -> list.map { it.toDomain() } }

    override fun getPlayerStatsForGame(gameId: Long): Flow<List<PlayerStats>> =
        sessionDao.getSessionsByGame(gameId).map { sessions ->
            val finishedSessions = sessions.filter { it.status == SessionStatus.FINISHED.name }
            if (finishedSessions.isEmpty()) return@map emptyList()

            // Collect all playerIds across all finished sessions
            val allPlayerIds = finishedSessions
                .flatMap { sessionDao.getPlayerIdsForSession(it.id) }
                .distinct()

            val players = playerDao.getPlayersByIds(allPlayerIds).map { it.toDomain() }

            players.map { player ->
                var gamesPlayed = 0
                var gamesWon = 0
                var totalPoints = 0

                finishedSessions.forEach { sessionEntity ->
                    val playerIds = sessionDao.getPlayerIdsForSession(sessionEntity.id)
                    if (player.id !in playerIds) return@forEach

                    gamesPlayed++
                    val game = gameDao.getGameById(sessionEntity.gameId)
                    val rounds = roundScoreDao.getRoundsForSessionOnce(sessionEntity.id)
                    val scoreSums = playerIds.associateWith { pid ->
                        rounds.filter { it.playerId == pid }.sumOf { it.points }
                    }
                    val playerScore = scoreSums[player.id] ?: 0
                    totalPoints += playerScore

                    val bestScore = if (game?.lowestScoreWins == true)
                        scoreSums.values.minOrNull()
                    else
                        scoreSums.values.maxOrNull()

                    if (bestScore != null && playerScore == bestScore) gamesWon++
                }

                PlayerStats(
                    player = player,
                    gamesPlayed = gamesPlayed,
                    gamesWon = gamesWon,
                    totalPoints = totalPoints,
                )
            }.sortedByDescending { it.gamesWon }
        }
}
