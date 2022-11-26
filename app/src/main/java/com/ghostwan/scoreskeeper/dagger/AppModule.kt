package com.ghostwan.scoreskeeper.dagger

import android.content.Context
import androidx.room.Room
import com.ghostwan.scoreskeeper.dao.GameDao
import com.ghostwan.scoreskeeper.database.GameDb
import com.ghostwan.scoreskeeper.database.GameRepository
import com.ghostwan.scoreskeeper.database.GameRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun provideGameDb(@ApplicationContext context: Context): GameDb {
        return Room.databaseBuilder(context, GameDb::class.java, "ScoresKeeperDB").build()
    }

    @Provides
    fun provideGameDao(gameDb: GameDb) : GameDao {
        return gameDb.gameDao()
    }

    @Provides
    fun provideGameRepository(gameDao: GameDao) : GameRepository {
        return GameRepositoryImpl(gameDao)
    }
}