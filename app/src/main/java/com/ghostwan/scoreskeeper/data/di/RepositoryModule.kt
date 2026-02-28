package com.ghostwan.scoreskeeper.data.di

import com.ghostwan.scoreskeeper.data.repository.GameRepositoryImpl
import com.ghostwan.scoreskeeper.data.repository.PlayerRepositoryImpl
import com.ghostwan.scoreskeeper.data.repository.SessionRepositoryImpl
import com.ghostwan.scoreskeeper.domain.repository.GameRepository
import com.ghostwan.scoreskeeper.domain.repository.PlayerRepository
import com.ghostwan.scoreskeeper.domain.repository.SessionRepository
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
