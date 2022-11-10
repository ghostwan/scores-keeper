package com.ghostwan.scoreskeeper.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ghostwan.scoreskeeper.db.GameDao
import java.util.*

@Entity(tableName = GameDao.PARTY_TABLE)
data class Party(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    var gameId: Long,
    val date: Date
)
