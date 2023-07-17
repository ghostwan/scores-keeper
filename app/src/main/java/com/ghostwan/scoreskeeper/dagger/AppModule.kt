package com.ghostwan.scoreskeeper.dagger

import com.ghostwan.scoreskeeper.dao.GameDao
import com.ghostwan.scoreskeeper.database.GameDb
import com.ghostwan.scoreskeeper.database.GameRepository
import com.ghostwan.scoreskeeper.database.GameRepositoryImpl
import com.ghostwan.scoreskeeper.model.Game
import com.ghostwan.scoreskeeper.model.GameClassification
import com.ghostwan.scoreskeeper.model.Party
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {
    @Provides
    fun provideGameRepository(realm: Realm): GameRepository {
        return GameRepositoryImpl(realm)
    }

    @Provides
    @Singleton
    fun providesRealmDatabase(): Realm {
        val configuration = RealmConfiguration.create(schema = setOf(Game::class, GameClassification::class, Party::class))
        return Realm.open(configuration)
    }
}

