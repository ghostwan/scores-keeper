package com.scoreskeeper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.scoreskeeper.data.local.dao.GameDao
import com.scoreskeeper.data.local.dao.PlayerDao
import com.scoreskeeper.data.local.dao.RoundScoreDao
import com.scoreskeeper.data.local.dao.SessionDao
import com.scoreskeeper.data.local.entity.GameEntity
import com.scoreskeeper.data.local.entity.PlayerEntity
import com.scoreskeeper.data.local.entity.RoundScoreEntity
import com.scoreskeeper.data.local.entity.SessionEntity
import com.scoreskeeper.data.local.entity.SessionPlayerEntity

@Database(
    entities = [
        GameEntity::class,
        PlayerEntity::class,
        SessionEntity::class,
        SessionPlayerEntity::class,
        RoundScoreEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class ScoresKeeperDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun playerDao(): PlayerDao
    abstract fun sessionDao(): SessionDao
    abstract fun roundScoreDao(): RoundScoreDao
}
