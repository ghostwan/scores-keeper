package com.scoreskeeper.data.di

import android.content.Context
import androidx.room.Room
import com.scoreskeeper.data.local.ScoresKeeperDatabase
import com.scoreskeeper.data.local.dao.GameDao
import com.scoreskeeper.data.local.dao.PlayerDao
import com.scoreskeeper.data.local.dao.RoundScoreDao
import com.scoreskeeper.data.local.dao.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ScoresKeeperDatabase =
        Room.databaseBuilder(
            context,
            ScoresKeeperDatabase::class.java,
            "scores_keeper.db"
        ).build()

    @Provides
    fun provideGameDao(db: ScoresKeeperDatabase): GameDao = db.gameDao()

    @Provides
    fun providePlayerDao(db: ScoresKeeperDatabase): PlayerDao = db.playerDao()

    @Provides
    fun provideSessionDao(db: ScoresKeeperDatabase): SessionDao = db.sessionDao()

    @Provides
    fun provideRoundScoreDao(db: ScoresKeeperDatabase): RoundScoreDao = db.roundScoreDao()
}
