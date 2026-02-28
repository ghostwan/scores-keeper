package com.scoreskeeper.data.di

import com.scoreskeeper.data.repository.GameRepositoryImpl
import com.scoreskeeper.data.repository.PlayerRepositoryImpl
import com.scoreskeeper.data.repository.SessionRepositoryImpl
import com.scoreskeeper.domain.repository.GameRepository
import com.scoreskeeper.domain.repository.PlayerRepository
import com.scoreskeeper.domain.repository.SessionRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindGameRepository(impl: GameRepositoryImpl): GameRepository

    @Binds
    @Singleton
    abstract fun bindPlayerRepository(impl: PlayerRepositoryImpl): PlayerRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(impl: SessionRepositoryImpl): SessionRepository
}
