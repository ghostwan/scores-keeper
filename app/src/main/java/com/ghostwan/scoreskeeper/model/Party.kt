package com.ghostwan.scoreskeeper.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ghostwan.scoreskeeper.dao.GameDao
import com.ghostwan.scoreskeeper.database.PartyListConverter
import java.util.*

@Entity(tableName = GameDao.PARTY_TABLE)
data class Party(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val date: Date,
    val players: MutableList<String>,
)
