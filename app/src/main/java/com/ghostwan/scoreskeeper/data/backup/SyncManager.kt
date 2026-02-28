package com.ghostwan.scoreskeeper.data.backup

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ghostwan.scoreskeeper.data.local.ScoresKeeperDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncDataStore by preferencesDataStore(name = "sync_settings")

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val driveBackupService: DriveBackupService,
    private val database: ScoresKeeperDatabase,
) {
    companion object {
        private const val TAG = "SyncManager"
        private const val SYNC_DEBOUNCE_MS = 5_000L // 5 seconds debounce after last change
        private val KEY_ACCOUNT_EMAIL = stringPreferencesKey("sync_account_email")
        private val KEY_ACCOUNT_NAME = stringPreferencesKey("sync_account_name")
        private val KEY_SYNC_ENABLED = booleanPreferencesKey("sync_enabled")
        private val KEY_LAST_SYNC = longPreferencesKey("last_sync_time")
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var syncJob: Job? = null
    private var observerJob: Job? = null

    init {
        // Load saved state
        scope.launch {
            context.syncDataStore.data.collect { prefs ->
                _syncState.update {
                    it.copy(
                        accountEmail = prefs[KEY_ACCOUNT_EMAIL],
                        accountName = prefs[KEY_ACCOUNT_NAME],
                        isSyncEnabled = prefs[KEY_SYNC_ENABLED] ?: false,
                        lastSyncTime = prefs[KEY_LAST_SYNC] ?: 0L,
                    )
                }
            }
        }
    }

    fun startAutoSync() {
        if (observerJob?.isActive == true) return

        observerJob = scope.launch {
            // Watch syncState to start/stop observer based on sync being enabled
            syncState.collect { state ->
                if (state.isSyncEnabled && state.accountEmail != null) {
                    startDatabaseObserver(state.accountEmail)
                }
            }
        }
    }

    private fun startDatabaseObserver(accountEmail: String) {
        database.invalidationTracker.addObserver(
            object : androidx.room.InvalidationTracker.Observer(
                "games", "players", "sessions", "session_players", "round_scores"
            ) {
                override fun onInvalidated(tables: Set<String>) {
                    Log.d(TAG, "Database changed: $tables")
                    triggerDebouncedSync(accountEmail)
                }
            }
        )
    }

    private fun triggerDebouncedSync(accountEmail: String) {
        syncJob?.cancel()
        syncJob = scope.launch {
            delay(SYNC_DEBOUNCE_MS)
            performSync(accountEmail)
        }
    }

    private suspend fun performSync(accountEmail: String) {
        _syncState.update { it.copy(isSyncing = true, lastError = null) }
        val result = driveBackupService.uploadBackup(accountEmail)
        when (result) {
            is BackupResult.Success -> {
                val now = System.currentTimeMillis()
                context.syncDataStore.edit { prefs ->
                    prefs[KEY_LAST_SYNC] = now
                }
                _syncState.update {
                    it.copy(isSyncing = false, lastSyncTime = now, lastError = null)
                }
                Log.d(TAG, "Auto-sync completed")
            }
            is BackupResult.Error -> {
                _syncState.update {
                    it.copy(isSyncing = false, lastError = result.message)
                }
                Log.e(TAG, "Auto-sync failed: ${result.message}")
            }
        }
    }

    suspend fun enableSync(email: String, displayName: String) {
        context.syncDataStore.edit { prefs ->
            prefs[KEY_ACCOUNT_EMAIL] = email
            prefs[KEY_ACCOUNT_NAME] = displayName
            prefs[KEY_SYNC_ENABLED] = true
        }
        // Trigger immediate backup
        performSync(email)
    }

    suspend fun disableSync() {
        syncJob?.cancel()
        context.syncDataStore.edit { prefs ->
            prefs.remove(KEY_ACCOUNT_EMAIL)
            prefs.remove(KEY_ACCOUNT_NAME)
            prefs[KEY_SYNC_ENABLED] = false
        }
        _syncState.update {
            SyncState()
        }
    }

    suspend fun manualBackup(): BackupResult {
        val email = syncState.value.accountEmail ?: return BackupResult.Error("Non connecté")
        _syncState.update { it.copy(isSyncing = true) }
        val result = driveBackupService.uploadBackup(email)
        if (result is BackupResult.Success) {
            val now = System.currentTimeMillis()
            context.syncDataStore.edit { prefs ->
                prefs[KEY_LAST_SYNC] = now
            }
            _syncState.update { it.copy(isSyncing = false, lastSyncTime = now, lastError = null) }
        } else if (result is BackupResult.Error) {
            _syncState.update { it.copy(isSyncing = false, lastError = result.message) }
        }
        return result
    }

    suspend fun restoreFromBackup(): BackupResult {
        val email = syncState.value.accountEmail ?: return BackupResult.Error("Non connecté")
        _syncState.update { it.copy(isSyncing = true) }
        val result = driveBackupService.restoreBackup(email)
        _syncState.update { it.copy(isSyncing = false) }
        return result
    }
}

data class SyncState(
    val accountEmail: String? = null,
    val accountName: String? = null,
    val isSyncEnabled: Boolean = false,
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = 0L,
    val lastError: String? = null,
)
