package com.scoreskeeper.data.local.mapper

import com.scoreskeeper.data.local.entity.GameEntity
import com.scoreskeeper.data.local.entity.PlayerEntity
import com.scoreskeeper.data.local.entity.RoundScoreEntity
import com.scoreskeeper.data.local.entity.SessionEntity
import com.scoreskeeper.domain.model.Game
import com.scoreskeeper.domain.model.Player
import com.scoreskeeper.domain.model.RoundScore
import com.scoreskeeper.domain.model.Session
import com.scoreskeeper.domain.model.SessionStatus
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// ---- Game ----

fun GameEntity.toDomain(): Game = Game(
    id = id,
    name = name,
    description = description,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    lowestScoreWins = lowestScoreWins,
    createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(createdAt), ZoneId.systemDefault()),
)

fun Game.toEntity(): GameEntity = GameEntity(
    id = id,
    name = name,
    description = description,
    minPlayers = minPlayers,
    maxPlayers = maxPlayers,
    lowestScoreWins = lowestScoreWins,
    createdAt = createdAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
)

// ---- Player ----

fun PlayerEntity.toDomain(): Player = Player(
    id = id,
    name = name,
    avatarColor = avatarColor,
)

fun Player.toEntity(): PlayerEntity = PlayerEntity(
    id = id,
    name = name,
    avatarColor = avatarColor,
)

// ---- Session ----

fun SessionEntity.toDomain(playerIds: List<Long> = emptyList(), gameName: String = ""): Session =
    Session(
        id = id,
        gameId = gameId,
        gameName = gameName,
        playerIds = playerIds,
        status = SessionStatus.valueOf(status),
        startedAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(startedAt), ZoneId.systemDefault()),
        finishedAt = finishedAt?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        },
    )

fun Session.toEntity(): SessionEntity = SessionEntity(
    id = id,
    gameId = gameId,
    status = status.name,
    startedAt = startedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    finishedAt = finishedAt?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli(),
)

// ---- RoundScore ----

fun RoundScoreEntity.toDomain(playerName: String = ""): RoundScore = RoundScore(
    id = id,
    sessionId = sessionId,
    playerId = playerId,
    playerName = playerName,
    round = round,
    points = points,
)

fun RoundScore.toEntity(): RoundScoreEntity = RoundScoreEntity(
    id = id,
    sessionId = sessionId,
    playerId = playerId,
    round = round,
    points = points,
)
