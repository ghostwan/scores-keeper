package com.scoreskeeper

import android.app.Application
import com.scoreskeeper.data.backup.SyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ScoresKeeperApp : Application() {

    @Inject
    lateinit var syncManager: SyncManager

    override fun onCreate() {
        super.onCreate()
        syncManager.startAutoSync()
    }
}
