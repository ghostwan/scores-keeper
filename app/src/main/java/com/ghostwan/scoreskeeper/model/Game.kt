package com.ghostwan.scoreskeeper.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ghostwan.scoreskeeper.dao.GameDao
import com.ghostwan.scoreskeeper.database.PartyListConverter

@Entity(tableName = GameDao.GAME_TABLE)
data class Game(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    var classification: GameClassification = GameClassification.HIGHEST,
    @TypeConverters(PartyListConverter::class)
    val parties: MutableList<Party> = arrayListOf()
//    val icon: String
)
