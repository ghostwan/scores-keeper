package com.ghostwan.scoreskeeper.presentation.screens.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostwan.scoreskeeper.R
import com.ghostwan.scoreskeeper.data.backup.BackupError
import com.ghostwan.scoreskeeper.data.backup.BackupResult
import com.ghostwan.scoreskeeper.data.backup.GoogleAuthHelper
import com.ghostwan.scoreskeeper.data.backup.SyncManager
import com.ghostwan.scoreskeeper.data.backup.SyncState
import com.ghostwan.scoreskeeper.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val syncManager: SyncManager,
    private val googleAuthHelper: GoogleAuthHelper,
    private val appPreferences: AppPreferences,
    private val application: Application,
) : ViewModel() {

    val syncState: StateFlow<SyncState> = syncManager.syncState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncState())

    val chartAreaFill: StateFlow<Boolean> = appPreferences.chartAreaFill
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val chartStartFromZero: StateFlow<Boolean> = appPreferences.chartStartFromZero
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun launchSignIn(launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>) {
        launcher.launch(googleAuthHelper.getSignInIntent())
    }

    fun onSignedIn(email: String, displayName: String) {
        viewModelScope.launch {
            try {
                syncManager.enableSync(email, displayName)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to enable sync", e)
                _message.value = application.getString(R.string.error_prefix, e.message ?: "")
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            try {
                googleAuthHelper.signOut {
                    viewModelScope.launch {
                        try {
                            syncManager.disableSync()
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to disable sync", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sign out", e)
                _message.value = application.getString(R.string.error_prefix, e.message ?: "")
            }
        }
    }

    fun toggleChartAreaFill(enabled: Boolean) {
        viewModelScope.launch {
            try {
                appPreferences.setChartAreaFill(enabled)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle chart area fill", e)
            }
        }
    }

    fun toggleChartStartFromZero(enabled: Boolean) {
        viewModelScope.launch {
            try {
                appPreferences.setChartStartFromZero(enabled)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle chart start from zero", e)
            }
        }
    }

    fun manualBackup() {
        viewModelScope.launch {
            try {
                val result = syncManager.manualBackup()
                _message.value = when (result) {
                    is BackupResult.Success -> application.getString(R.string.backup_success)
                    is BackupResult.Error -> application.getString(R.string.error_prefix, result.error.toMessage())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to backup", e)
                _message.value = application.getString(R.string.error_prefix, e.message ?: "")
            }
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            try {
                val result = syncManager.restoreFromBackup()
                _message.value = when (result) {
                    is BackupResult.Success -> application.getString(R.string.restore_success)
                    is BackupResult.Error -> application.getString(R.string.error_prefix, result.error.toMessage())
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to restore", e)
                _message.value = application.getString(R.string.error_prefix, e.message ?: "")
            }
        }
    }

    private fun BackupError.toMessage(): String = when (this) {
        BackupError.DB_NOT_FOUND -> application.getString(R.string.backup_error_db_not_found)
        BackupError.NO_BACKUP_FOUND -> application.getString(R.string.backup_error_no_backup)
        BackupError.NOT_CONNECTED -> application.getString(R.string.backup_error_not_connected)
        BackupError.UNKNOWN -> application.getString(R.string.backup_error_unknown)
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        private const val TAG = "SettingsVM"
    }
}
