package com.scoreskeeper.presentation.screens.settings

import android.app.Application
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.scoreskeeper.R
import com.scoreskeeper.data.backup.BackupResult
import com.scoreskeeper.data.backup.GoogleAuthHelper
import com.scoreskeeper.data.backup.SyncManager
import com.scoreskeeper.data.backup.SyncState
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
    private val application: Application,
) : ViewModel() {

    val syncState: StateFlow<SyncState> = syncManager.syncState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncState())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun launchSignIn(launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>) {
        launcher.launch(googleAuthHelper.getSignInIntent())
    }

    fun onSignedIn(email: String, displayName: String) {
        viewModelScope.launch {
            syncManager.enableSync(email, displayName)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            googleAuthHelper.signOut {
                viewModelScope.launch {
                    syncManager.disableSync()
                }
            }
        }
    }

    fun manualBackup() {
        viewModelScope.launch {
            val result = syncManager.manualBackup()
            _message.value = when (result) {
                is BackupResult.Success -> application.getString(R.string.backup_success)
                is BackupResult.Error -> application.getString(R.string.error_prefix, result.message)
            }
        }
    }

    fun restoreBackup() {
        viewModelScope.launch {
            val result = syncManager.restoreFromBackup()
            _message.value = when (result) {
                is BackupResult.Success -> application.getString(R.string.restore_success)
                is BackupResult.Error -> application.getString(R.string.error_prefix, result.message)
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
