package com.ghostwan.scoreskeeper.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ghostwan.scoreskeeper.db.GameDao

@Entity(tableName = GameDao.GAME_TABLE)
data class Game(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    var classification: GameClassification = GameClassification.highest
//    val icon: String
)
