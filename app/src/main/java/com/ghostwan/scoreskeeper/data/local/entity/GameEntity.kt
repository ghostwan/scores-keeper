package com.ghostwan.scoreskeeper.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val minPlayers: Int,
    val maxPlayers: Int,
    val lowestScoreWins: Boolean,
    val createdAt: Long, // epoch millis
)
