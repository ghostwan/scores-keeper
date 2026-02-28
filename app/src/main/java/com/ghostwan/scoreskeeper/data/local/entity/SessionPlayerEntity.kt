package com.ghostwan.scoreskeeper.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Join table linking sessions to their participating players.
 */
@Entity(
    tableName = "session_players",
    primaryKeys = ["sessionId", "playerId"],
    foreignKeys = [
        ForeignKey(
            entity = SessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = PlayerEntity::class,
            parentColumns = ["id"],
            childColumns = ["playerId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("sessionId"), Index("playerId")]
)
data class SessionPlayerEntity(
    val sessionId: Long,
    val playerId: Long,
)
